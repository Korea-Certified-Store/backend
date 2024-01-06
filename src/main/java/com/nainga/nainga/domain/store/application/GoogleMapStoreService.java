package com.nainga.nainga.domain.store.application;

import com.google.gson.*;
import com.nainga.nainga.domain.store.dao.StoreRepository;
import com.nainga.nainga.domain.store.domain.Store;
import com.nainga.nainga.domain.store.dto.StoreDataByParser;
import lombok.RequiredArgsConstructor;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class GoogleMapStoreService {
    private final StoreRepository storeRepository;

    //이 메서드는 Mobeom Excel dataset 파싱을 통해 가게 이름과 주소를 얻고, 이 정보를 바탕으로 Google Map Place Id를 가져옵니다.
    //그 후 얻어진 Google Map Place Id를 가지고 가게 상세 정보를 Google Map API로부터 가져와 Store DB에 저장합니다.
    @Transactional
    public void createAllMobeomStores() {
        List<StoreDataByParser> allMobeomStores = MobeomDataParser.getAllMobeomStores("mobeom_test.xlsx");
        for (StoreDataByParser storeDataByParser : allMobeomStores) {
            String googleMapPlacesId = getGoogleMapPlacesId(storeDataByParser.getName(), storeDataByParser.getAddress());

            if(googleMapPlacesId == null)   //가져온 Google Map Place Id가 null이라는 것은 가게가 하나로 특정되지 않아 사용할 수 없다는 것을 의미
                continue;

            Optional<Store> resultByGooglePlaceId = storeRepository.findByGooglePlaceId(googleMapPlacesId);
            if (resultByGooglePlaceId.isEmpty()) {
                JsonObject googleMapPlacesDetail = getGoogleMapPlacesDetail(googleMapPlacesId);
                if (googleMapPlacesDetail == null)   //Google Map Place Detail을 제대로 불러오지 못했을 경우에는 skip
                    continue;

                //WKTReader Parse exception에 대한 처리를 위한 try-catch문
                try {
                    ArrayList<String> regularOpeningHoursList = new ArrayList<String>();
                    ArrayList<String> photosList = new ArrayList<>();
                    String phoneNumber = null;
                    String primaryTypeDisplayName = null;


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
                        primaryTypeDisplayName = googleMapPlacesDetail.getAsJsonObject("displayName").get("text").getAsString();
                    }

                    Store store = Store.builder()
                            .googlePlaceId(googleMapPlacesDetail.get("id").getAsString())
                            .internationalPhoneNumber(phoneNumber)
                            .formattedAddress(googleMapPlacesDetail.get("formattedAddress").getAsString())
                            .location((Point) new WKTReader().read(String.format("POINT(%s %s)", googleMapPlacesDetail.getAsJsonObject("location").get("latitude").getAsString(), googleMapPlacesDetail.getAsJsonObject("location").get("longitude").getAsString())))
                            .regularOpeningHours(regularOpeningHoursList)
                            .displayName(googleMapPlacesDetail.getAsJsonObject("displayName").get("text").getAsString())
                            .primaryType(primaryTypeDisplayName)
                            .photos(photosList)
                            .build();

                    storeRepository.save(store);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            } else {

            }

        }
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
