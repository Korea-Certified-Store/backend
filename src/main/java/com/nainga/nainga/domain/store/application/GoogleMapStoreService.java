package com.nainga.nainga.domain.store.application;

import com.google.gson.*;
import com.nainga.nainga.domain.certification.dao.CertificationRepository;
import com.nainga.nainga.domain.certification.domain.Certification;
import com.nainga.nainga.domain.store.dao.StoreRepository;
import com.nainga.nainga.domain.store.domain.Store;
import com.nainga.nainga.domain.store.dto.CreateDividedMobeomStoresResponse;
import com.nainga.nainga.domain.store.dto.StoreDataByParser;
import com.nainga.nainga.domain.storecertification.dao.StoreCertificationRepository;
import com.nainga.nainga.domain.storecertification.domain.StoreCertification;
import lombok.RequiredArgsConstructor;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class GoogleMapStoreService {
    private final StoreRepository storeRepository;
    private final CertificationRepository certificationRepository;
    private final StoreCertificationRepository storeCertificationRepository;

    //이 메서드는 Mobeom Excel dataset 파싱을 통해 가게 이름과 주소를 얻고, 이 정보를 바탕으로 Google Map Place Id를 가져옵니다.
    //그 후 얻어진 Google Map Place Id를 가지고 가게 상세 정보를 Google Map API로부터 가져와 Store DB에 저장합니다.
    @Transactional
    public void createAllMobeomStores() {
        List<StoreDataByParser> allMobeomStores = MobeomDataParser.getAllMobeomStores("mobeom_test300.xlsx");
        for (StoreDataByParser storeDataByParser : allMobeomStores) {
            String googleMapPlacesId = getGoogleMapPlacesId(storeDataByParser.getName(), storeDataByParser.getAddress());

            if(googleMapPlacesId == null)   //가져온 Google Map Place Id가 null이라는 것은 가게가 하나로 특정되지 않아 사용할 수 없다는 것을 의미
                continue;

            Optional<Store> resultByGooglePlaceId = storeRepository.findByGooglePlaceId(googleMapPlacesId); //Google Map API에서 가져온 place id와 동일한 정보가 디비에 있으면 중복 가게!
            if (resultByGooglePlaceId.isEmpty()) {  //아직 DB에 존재하지 않는 가게인 경우!
                JsonObject googleMapPlacesDetail = getGoogleMapPlacesDetail(googleMapPlacesId); //Google Map API를 통해 해당 가게의 상세 정보를 가져옴
                if (googleMapPlacesDetail == null)   //Google Map Place Detail을 제대로 불러오지 못했을 경우에는 skip
                    continue;

                //WKTReader Parse exception에 대한 처리를 위한 try-catch문
                try {
                    ArrayList<String> regularOpeningHoursList = new ArrayList<String>();
                    ArrayList<String> photosList = new ArrayList<>();
                    String phoneNumber = null;
                    String primaryTypeDisplayName = null;

                    //이 아래 4개의 if문들은 해당 값들이 없는 가게가 존재하기 때문에 예외처리 목적으로 작성
                    if (googleMapPlacesDetail.getAsJsonObject("regularOpeningHours") != null && googleMapPlacesDetail.getAsJsonObject("regularOpeningHours").getAsJsonArray("weekdayDescriptions") != null) {
                        googleMapPlacesDetail.getAsJsonObject("regularOpeningHours").getAsJsonArray("weekdayDescriptions").forEach(weekdayDescription -> regularOpeningHoursList.add(weekdayDescription.getAsString()));
                    }

                    if (googleMapPlacesDetail.getAsJsonArray("photos") != null) {
                        googleMapPlacesDetail.getAsJsonArray("photos").forEach(photo -> photosList.add(photo.getAsJsonObject().get("name").getAsString()));
                    }

                    if (googleMapPlacesDetail.get("internationalPhoneNumber") != null) {
                        phoneNumber = googleMapPlacesDetail.get("internationalPhoneNumber").getAsString();
                    } else if (googleMapPlacesDetail.get("nationalPhoneNumber") != null) {
                        phoneNumber = googleMapPlacesDetail.get("nationalPhoneNumber").getAsString();
                    }

                    if (googleMapPlacesDetail.getAsJsonObject("primaryTypeDisplayName") != null && googleMapPlacesDetail.getAsJsonObject("primaryTypeDisplayName").get("text") != null) {
                        primaryTypeDisplayName = googleMapPlacesDetail.getAsJsonObject("primaryTypeDisplayName").get("text").getAsString();
                    }

                    if(phoneNumber == null && primaryTypeDisplayName == null)
                        continue;

                    if (!photosList.isEmpty()) {
                        String localFilePath = getGoogleMapPlacesImage(photosList.get(0));
                        photosList.clear();
                        photosList.add(localFilePath);
                    }

                    //얻어온 가게 상세 정보를 바탕으로 DB에 저장할 객체를 생성
                    Store store = Store.builder()
                            .googlePlaceId(googleMapPlacesDetail.get("id").getAsString())
                            .phoneNumber(phoneNumber)
                            .formattedAddress(googleMapPlacesDetail.get("formattedAddress").getAsString())
                            .location((Point) new WKTReader().read(String.format("POINT(%s %s)", googleMapPlacesDetail.getAsJsonObject("location").get("latitude").getAsString(), googleMapPlacesDetail.getAsJsonObject("location").get("longitude").getAsString())))
                            .regularOpeningHours(regularOpeningHoursList)
                            .displayName(googleMapPlacesDetail.getAsJsonObject("displayName").get("text").getAsString())
                            .primaryTypeDisplayName(primaryTypeDisplayName)
                            .photos(photosList)
                            .build();

                    Optional<Certification> mobeom = certificationRepository.findByName("모범음식점");   //Certification 테이블에 이미 모범음식점 데이터가 있는지 조회
                    if (mobeom.isPresent()) {   //만약 존재하는 경우면, Certification은 새로 만들어줄 필요가 없음
                        StoreCertification storeCertification = StoreCertification.builder()
                                .store(store)
                                .certification(mobeom.get())
                                .build();

                        storeCertificationRepository.save(storeCertification);
                        storeRepository.save(store);
                    } else {    //만약 존재하지 않는 경우라면, Certification도 새로 만들어줘야 함
                        Certification mobeomReal = Certification.builder()
                                .name("모범음식점")
                                .build();

                        StoreCertification storeCertification = StoreCertification.builder()
                                .store(store)
                                .certification(mobeomReal)
                                .build();

                        certificationRepository.save(mobeomReal);
                        storeCertificationRepository.save(storeCertification);
                        storeRepository.save(store);
                    }

                } catch (ParseException e) {
                    e.printStackTrace();
                }
            } else {    //이미 중복된 Google Place Id가 있을 때! 즉, 동일한 가게가 있을 때는 Certification만 새로 맺어주면 된다.
                Store store = resultByGooglePlaceId.get();  //위에서 조회한 중복된 가게
                Optional<Certification> mobeom = certificationRepository.findByName("모범음식점");   //여기도 마찬가지로 Certification 테이블에 이미 모범음식점 데이터가 있는지 조회

                if (mobeom.isPresent()) {   //만약에 존재한다면, Certification은 새로 만들어줄 필요가 없다
                    StoreCertification storeCertification = StoreCertification.builder()
                            .store(store)
                            .certification(mobeom.get())
                            .build();

                    storeCertificationRepository.save(storeCertification);
                } else {    //만약에 존재하지 않는다면, Certification도 새로 만들어줘야 한다.
                    Certification mobeomReal = Certification.builder()
                            .name("모범음식점")
                            .build();

                    StoreCertification storeCertification = StoreCertification.builder()
                            .store(store)
                            .certification(mobeomReal)
                            .build();

                    certificationRepository.save(mobeomReal);
                    storeCertificationRepository.save(storeCertification);
                }
            }

        }
    }

    //이 메서드는 createAllMobeomStore() 메서드를 개선하기 위해 구현된 메서드입니다.
    //createAllMobeomStore()는 대용량 데이터를 한번에 모두 조회하는 반면에 아래 메서드는 API call 비용을 제한하여 해당 비용까지만 조회합니다.
    //파라미터로 몇 달러까지 API call을 허용할 것인지 정하고, 조회할 시작 위치를 지정합니다.
    //만약 도중에 남은 달러가 없어질 시점이 되면, 그때까지 남은 달러와 다음부터 탐색해야할 인덱스 번호를 리턴하며 만약 모두 조회된 경우에는 남은 달러와 -1을 리턴합니다.
    @Transactional
    public CreateDividedMobeomStoresResponse createDividedMobeomStores(double dollars, int startIndex) {
        List<StoreDataByParser> allMobeomStores = MobeomDataParser.getAllMobeomStores("mobeom_test300.xlsx");
        for (int i=startIndex; i< allMobeomStores.size(); ++i) {


            String googleMapPlacesId = getGoogleMapPlacesId(allMobeomStores.get(i).getName(), allMobeomStores.get(i).getAddress());

            if(googleMapPlacesId == null)   //가져온 Google Map Place Id가 null이라는 것은 가게가 하나로 특정되지 않아 사용할 수 없다는 것을 의미
                continue;

            Optional<Store> resultByGooglePlaceId = storeRepository.findByGooglePlaceId(googleMapPlacesId); //Google Map API에서 가져온 place id와 동일한 정보가 디비에 있으면 중복 가게!
            if (resultByGooglePlaceId.isEmpty()) {  //아직 DB에 존재하지 않는 가게인 경우!
                if (dollars - 0.02 < 0) {   //남은 달러와 비교하여 API 호출 비용이 없으면 그 상태에서 중단
                    CreateDividedMobeomStoresResponse createDividedMobeomStoresResponse = new CreateDividedMobeomStoresResponse();
                    createDividedMobeomStoresResponse.setDollars(dollars);
                    createDividedMobeomStoresResponse.setNextIndex(i);
                    return createDividedMobeomStoresResponse;
                }
                JsonObject googleMapPlacesDetail = getGoogleMapPlacesDetail(googleMapPlacesId); //Google Map API를 통해 해당 가게의 상세 정보를 가져옴, 0.02달러 소비

                //여기선 API call이 진행된 시점이므로, 남은 달러에 반영
                dollars -= 0.02;

                if (googleMapPlacesDetail == null)   //Google Map Place Detail을 제대로 불러오지 못했을 경우에는 skip
                    continue;

                //WKTReader Parse exception에 대한 처리를 위한 try-catch문
                try {
                    ArrayList<String> regularOpeningHoursList = new ArrayList<String>();
                    ArrayList<String> photosList = new ArrayList<>();
                    String phoneNumber = null;
                    String primaryTypeDisplayName = null;

                    //이 아래 4개의 if문들은 해당 값들이 없는 가게가 존재하기 때문에 예외처리 목적으로 작성
                    if (googleMapPlacesDetail.getAsJsonObject("regularOpeningHours") != null && googleMapPlacesDetail.getAsJsonObject("regularOpeningHours").getAsJsonArray("weekdayDescriptions") != null) {
                        googleMapPlacesDetail.getAsJsonObject("regularOpeningHours").getAsJsonArray("weekdayDescriptions").forEach(weekdayDescription -> regularOpeningHoursList.add(weekdayDescription.getAsString()));
                    }

                    if (googleMapPlacesDetail.getAsJsonArray("photos") != null) {
                        googleMapPlacesDetail.getAsJsonArray("photos").forEach(photo -> photosList.add(photo.getAsJsonObject().get("name").getAsString()));
                    }

                    if (googleMapPlacesDetail.get("internationalPhoneNumber") != null) {
                        phoneNumber = googleMapPlacesDetail.get("internationalPhoneNumber").getAsString();
                    } else if (googleMapPlacesDetail.get("nationalPhoneNumber") != null) {
                        phoneNumber = googleMapPlacesDetail.get("nationalPhoneNumber").getAsString();
                    }

                    if (googleMapPlacesDetail.getAsJsonObject("primaryTypeDisplayName") != null && googleMapPlacesDetail.getAsJsonObject("primaryTypeDisplayName").get("text") != null) {
                        primaryTypeDisplayName = googleMapPlacesDetail.getAsJsonObject("primaryTypeDisplayName").get("text").getAsString();
                    }

                    if(phoneNumber == null && primaryTypeDisplayName == null)
                        continue;

                    if (!photosList.isEmpty()) {
                        if (dollars - 0.007 < 0) { //다음 API를 호출할 돈이 없으면 중단
                            CreateDividedMobeomStoresResponse createDividedMobeomStoresResponse = new CreateDividedMobeomStoresResponse();
                            createDividedMobeomStoresResponse.setDollars(dollars);
                            createDividedMobeomStoresResponse.setNextIndex(i);
                            return createDividedMobeomStoresResponse;
                        }
                        //돈이 충분히 있으면,
                        String localFilePath = getGoogleMapPlacesImage(photosList.get(0));  //0.007달러 소비

                        //소비한 비용 반영
                        dollars -= 0.007;

                        photosList.clear();
                        photosList.add(localFilePath);
                    }

                    //얻어온 가게 상세 정보를 바탕으로 DB에 저장할 객체를 생성
                    Store store = Store.builder()
                            .googlePlaceId(googleMapPlacesDetail.get("id").getAsString())
                            .phoneNumber(phoneNumber)
                            .formattedAddress(googleMapPlacesDetail.get("formattedAddress").getAsString())
                            .location((Point) new WKTReader().read(String.format("POINT(%s %s)", googleMapPlacesDetail.getAsJsonObject("location").get("latitude").getAsString(), googleMapPlacesDetail.getAsJsonObject("location").get("longitude").getAsString())))
                            .regularOpeningHours(regularOpeningHoursList)
                            .displayName(googleMapPlacesDetail.getAsJsonObject("displayName").get("text").getAsString())
                            .primaryTypeDisplayName(primaryTypeDisplayName)
                            .photos(photosList)
                            .build();

                    Optional<Certification> mobeom = certificationRepository.findByName("모범음식점");   //Certification 테이블에 이미 모범음식점 데이터가 있는지 조회
                    if (mobeom.isPresent()) {   //만약 존재하는 경우면, Certification은 새로 만들어줄 필요가 없음
                        StoreCertification storeCertification = StoreCertification.builder()
                                .store(store)
                                .certification(mobeom.get())
                                .build();

                        storeCertificationRepository.save(storeCertification);
                        storeRepository.save(store);
                    } else {    //만약 존재하지 않는 경우라면, Certification도 새로 만들어줘야 함
                        Certification mobeomReal = Certification.builder()
                                .name("모범음식점")
                                .build();

                        StoreCertification storeCertification = StoreCertification.builder()
                                .store(store)
                                .certification(mobeomReal)
                                .build();

                        certificationRepository.save(mobeomReal);
                        storeCertificationRepository.save(storeCertification);
                        storeRepository.save(store);
                    }

                } catch (ParseException e) {
                    e.printStackTrace();
                }
            } else {    //이미 중복된 Google Place Id가 있을 때! 즉, 동일한 가게가 있을 때는 Certification만 새로 맺어주면 된다.
                Store store = resultByGooglePlaceId.get();  //위에서 조회한 중복된 가게
                Optional<Certification> mobeom = certificationRepository.findByName("모범음식점");   //여기도 마찬가지로 Certification 테이블에 이미 모범음식점 데이터가 있는지 조회

                if (mobeom.isPresent()) {   //만약에 존재한다면, Certification은 새로 만들어줄 필요가 없다
                    StoreCertification storeCertification = StoreCertification.builder()
                            .store(store)
                            .certification(mobeom.get())
                            .build();

                    storeCertificationRepository.save(storeCertification);
                } else {    //만약에 존재하지 않는다면, Certification도 새로 만들어줘야 한다.
                    Certification mobeomReal = Certification.builder()
                            .name("모범음식점")
                            .build();

                    StoreCertification storeCertification = StoreCertification.builder()
                            .store(store)
                            .certification(mobeomReal)
                            .build();

                    certificationRepository.save(mobeomReal);
                    storeCertificationRepository.save(storeCertification);
                }
            }

        }
        //만약 모범 음식점 리스트 끝까지 잘 돌았다면,
        CreateDividedMobeomStoresResponse createDividedMobeomStoresResponse = new CreateDividedMobeomStoresResponse();
        createDividedMobeomStoresResponse.setDollars(dollars);
        createDividedMobeomStoresResponse.setNextIndex(-1);
        return createDividedMobeomStoresResponse;
    }

    //아래 메서드는 Google Map API 내 사진 요청 API를 트리거하여 API call
    public String getGoogleMapPlacesImage(String photosName) {
        String maxWidthPx = "400";
        String maxHeightPx = "400";
        String GoogleApiKey = System.getenv("GOOGLE_API_KEY");
        String reqURL = "https://places.googleapis.com/v1/" + photosName + "/media?maxHeightPx=" + maxHeightPx + "&maxWidthPx=" + maxWidthPx + "&key=" + GoogleApiKey;

        String uuid = UUID.randomUUID().toString().replace("-", "");
        String fileExtension = ".jpg";
        String randomFilename = uuid + fileExtension;
        String directoryPath = new File("").getAbsolutePath() + File.separator + "src" + File.separator + "main" + File.separator + "resources" + File.separator + "images" + File.separator;
        String localFilePath = directoryPath + randomFilename;

        Path localPath = Path.of(localFilePath);

        try {
            URL url = new URL(reqURL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            InputStream inputStream = conn.getInputStream();

            Files.copy(inputStream, localPath, StandardCopyOption.REPLACE_EXISTING);

            conn.disconnect();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return localFilePath;
    }

    //이 메서드는 Google Map API 내 "텍스트 검색(신규)"를 활용하여 텍스트 기반으로 검색하고 매칭되는 가게의 places.id를 가져옵니다.
    //주소나 이름이 잘못됐거나, 폐업을했거나하면 장소가 특정되지 않을 수 있습니다. 이땐, places.id가 반환되지 않습니다.
    //혹은 흔한 이름이고 해당 주소지 근방에 비슷한 이름들을 가진 가게가 많을 경우 여러 개의 places.id가 반환될 수 있습니다.
    //따라서, 모호함을 없애기 위해 저희 가게가 정말 잘 찾아진 경우, 즉 places.id가 딱 1개만 반환되었을 때만 DB에 반영합니다.
    //이건 Google Map API에 텍스트 검색 (ID 전용) SKU를 호출하는 거라 호출 횟수 관계없이 호출 비용이 평생 무료이다.
    //https://developers.google.com/maps/documentation/places/web-service/usage-and-billing?hl=ko
    public String getGoogleMapPlacesId(String name, String address) {
        String reqURL = "https://places.googleapis.com/v1/places:searchText";
        String textQuery = address + name;
        String GoogleApiKey = System.getenv("GOOGLE_API_KEY");  //Secrets 보호를 위해 환경 변수 사용

        try {
            URL url = new URL(reqURL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            conn.setRequestMethod("POST");  //Google Map API의 정해진 헤더
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("X-Goog-Api-Key", GoogleApiKey);
            conn.setRequestProperty("X-Goog-FieldMask", "places.id");

            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("textQuery", textQuery);
            String jsonData = new Gson().toJson(jsonObject);    //GSON을 활용해서 JSON 포맷의 request body 작성

            OutputStream outputStream = conn.getOutputStream(); //Google Map API로 작성해둔 헤더와 request body를 보냄
            byte[] bytes = jsonData.getBytes(StandardCharsets.UTF_8);
            outputStream.write(bytes, 0, bytes.length);

            int responseCode = conn.getResponseCode();

            if (responseCode != 200) {
                throw new IllegalStateException("Google Map API 요청 중 오류 발생");
            }

            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line = "";
            String result = "";

            while ((line = br.readLine()) != null) {    //Response string을 수집
                result += line;
            }

            JsonParser parser = new JsonParser();
            JsonElement jsonElement = parser.parse(result);

            if (jsonElement.isJsonObject()) {   //꼼꼼하게 예외처리해주지 않으면 에러 발생
                JsonObject resultJsonObject = jsonElement.getAsJsonObject();
                JsonArray placesJsonArray = resultJsonObject.getAsJsonArray("places");  //Response body에서 원하는 데이터를 파싱

                if (placesJsonArray != null) {  //꼼꼼하게 예외처리해주지 않으면 에러 발생
                    if (placesJsonArray.size() == 1) {  //검색된 가게가 1개로 정확히 특정되었을 때만 해당 가게의 place id를 리턴
                        return placesJsonArray.get(0).getAsJsonObject().get("id").getAsString();
                    }
                }
            }
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;    //검색된 가게가 1개일 때를 제외하고는 null을 리턴
    }

    //아래 메서드는 SKU: Place Details (Advanced)를 트리거해서 api 콜 1개당 0.02달러씩 결제된다.
    //https://developers.google.com/maps/documentation/places/web-service/usage-and-billing?hl=ko#advanced-placedetails
    public JsonObject getGoogleMapPlacesDetail(String googleMapPlacesId) {
        String reqURL = "https://places.googleapis.com/v1/places/" + googleMapPlacesId + "?languageCode=ko";
        String GoogleApiKey = System.getenv("GOOGLE_API_KEY");  //Secrets 보호를 위해 환경 변수 사용

        try {
            URL url = new URL(reqURL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            conn.setRequestMethod("GET");  //Google Map API의 정해진 헤더
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("X-Goog-Api-Key", GoogleApiKey);
            conn.setRequestProperty("X-Goog-FieldMask", "id,displayName,primaryTypeDisplayName,formattedAddress,regularOpeningHours.weekdayDescriptions,location,internationalPhoneNumber,photos.name,photos.widthPx,photos.heightPx");

            int responseCode = conn.getResponseCode();

            if (responseCode != 200) {
                throw new IllegalStateException("Google Map API 요청 중 오류 발생");
            }

            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line = "";
            String result = "";

            while ((line = br.readLine()) != null) {    //Response string을 수집
                result += line;
            }

            JsonParser parser = new JsonParser();
            JsonElement jsonElement = parser.parse(result);

            if (jsonElement.isJsonObject()) {
                return jsonElement.getAsJsonObject();
            }
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;    //Google Map API로 부터 얻은 Response JSON이 올바르지 않을 때 null을 리턴
    }
}
