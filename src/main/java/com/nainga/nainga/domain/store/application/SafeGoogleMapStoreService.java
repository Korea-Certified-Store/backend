package com.nainga.nainga.domain.store.application;

import com.google.gson.*;
import com.nainga.nainga.domain.certification.dao.CertificationRepository;
import com.nainga.nainga.domain.certification.domain.Certification;
import com.nainga.nainga.domain.store.dao.StoreRepository;
import com.nainga.nainga.domain.store.domain.Store;
import com.nainga.nainga.domain.store.domain.StoreDay;
import com.nainga.nainga.domain.store.domain.StoreOpenCloseDay;
import com.nainga.nainga.domain.store.domain.StoreRegularOpeningHours;
import com.nainga.nainga.domain.store.dto.CreateDividedSafeStoresResponse;
import com.nainga.nainga.domain.store.dto.StoreDataByParser;
import com.nainga.nainga.domain.storecertification.dao.StoreCertificationRepository;
import com.nainga.nainga.domain.storecertification.domain.StoreCertification;
import lombok.RequiredArgsConstructor;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;
import org.springframework.beans.factory.annotation.Value;
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

import static com.nainga.nainga.domain.store.application.GoogleMapMethods.*;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class SafeGoogleMapStoreService {
    @Value("${GOOGLE_API_KEY}")
    private String googleApiKey;    //Spring bean 내에서만 @Value로 프로퍼티를 가져올 수 있어서 Service 단에서 받고 GoogleMapMethods에는 파라미터로 넘겨줌.
    private final StoreRepository storeRepository;
    private final CertificationRepository certificationRepository;
    private final StoreCertificationRepository storeCertificationRepository;

    //이 메서드는 Safe Excel dataset 파싱을 통해 가게 이름과 주소를 얻고, 이 정보를 바탕으로 Google Map Place Id를 가져옵니다.
    //그 후 얻어진 Google Map Place Id를 가지고 가게 상세 정보를 Google Map API로부터 가져와 Store DB에 저장합니다.
    @Transactional
    public void createAllSafeStores(String fileName) {
        List<StoreDataByParser> allSafeStores = SafeDataParser.getAllSafeStores(fileName);
        for (StoreDataByParser storeDataByParser : allSafeStores) {
            String googleMapPlacesId = getGoogleMapPlacesId(storeDataByParser.getName(), storeDataByParser.getAddress(), googleApiKey);

            if(googleMapPlacesId == null)   //가져온 Google Map Place Id가 null이라는 것은 가게가 하나로 특정되지 않아 사용할 수 없다는 것을 의미
                continue;

            Optional<Store> resultByGooglePlaceId = storeRepository.findByGooglePlaceId(googleMapPlacesId); //Google Map API에서 가져온 place id와 동일한 정보가 디비에 있으면 중복 가게!
            if (resultByGooglePlaceId.isEmpty()) {  //아직 DB에 존재하지 않는 가게인 경우!
                JsonObject googleMapPlacesDetail = getGoogleMapPlacesDetail(googleMapPlacesId, googleApiKey); //Google Map API를 통해 해당 가게의 상세 정보를 가져옴
                if (googleMapPlacesDetail == null)   //Google Map Place Detail을 제대로 불러오지 못했을 경우에는 skip
                    continue;

                //WKTReader Parse exception에 대한 처리를 위한 try-catch문
                try {
                    List<JsonObject> openList = new ArrayList<JsonObject>();
                    List<JsonObject> closeList = new ArrayList<JsonObject>();
                    List<StoreRegularOpeningHours> regularOpeningHours = new ArrayList<StoreRegularOpeningHours>();
                    List<String> localPhotosList = new ArrayList<>();
                    List<String> googlePhotosList = new ArrayList<>();
                    String phoneNumber = null;
                    String primaryTypeDisplayName = null;

                    //이 아래 4개의 if문들은 해당 값들이 없는 가게가 존재하기 때문에 예외처리 목적으로 작성
                    if (googleMapPlacesDetail.getAsJsonObject("regularOpeningHours") != null && googleMapPlacesDetail.getAsJsonObject("regularOpeningHours").getAsJsonArray("periods") != null) {
                        googleMapPlacesDetail.getAsJsonObject("regularOpeningHours").getAsJsonArray("periods").forEach(period -> {
                            openList.add(period.getAsJsonObject().getAsJsonObject("open"));
                            closeList.add(period.getAsJsonObject().getAsJsonObject("close"));
                        });

                        for (int i = 0; i < openList.size(); ++i) { //Google API에서 가져온 가게 Open, 가게 Close 시간 정보를 파싱하는 과정
                            StoreRegularOpeningHours storeRegularOpeningHours = new StoreRegularOpeningHours();
                            StoreOpenCloseDay storeOpenDay = new StoreOpenCloseDay();
                            StoreOpenCloseDay storeCloseDay = new StoreOpenCloseDay();

                            storeOpenDay.setDay(StoreDay.values()[openList.get(i).get("day").getAsInt()]);
                            storeOpenDay.setHour(openList.get(i).get("hour").getAsInt());
                            storeOpenDay.setMinute(openList.get(i).get("minute").getAsInt());

                            storeCloseDay.setDay(StoreDay.values()[closeList.get(i).get("day").getAsInt()]);
                            storeCloseDay.setHour(closeList.get(i).get("hour").getAsInt());
                            storeCloseDay.setMinute(closeList.get(i).get("minute").getAsInt());

                            storeRegularOpeningHours.setOpen(storeOpenDay);
                            storeRegularOpeningHours.setClose(storeCloseDay);
                            regularOpeningHours.add(storeRegularOpeningHours);
                        }
                    }

                    if (googleMapPlacesDetail.getAsJsonArray("photos") != null) {
                        googleMapPlacesDetail.getAsJsonArray("photos").forEach(photo -> googlePhotosList.add(photo.getAsJsonObject().get("name").getAsString()));
                    }

                    if (googleMapPlacesDetail.get("nationalPhoneNumber") != null) {
                        phoneNumber = googleMapPlacesDetail.get("nationalPhoneNumber").getAsString();
                    } else if (googleMapPlacesDetail.get("internationalPhoneNumber") != null) {
                        phoneNumber = googleMapPlacesDetail.get("internationalPhoneNumber").getAsString();
                    }

                    if (googleMapPlacesDetail.getAsJsonObject("primaryTypeDisplayName") != null && googleMapPlacesDetail.getAsJsonObject("primaryTypeDisplayName").get("text") != null) {
                        primaryTypeDisplayName = googleMapPlacesDetail.getAsJsonObject("primaryTypeDisplayName").get("text").getAsString();
                    }

                    if(phoneNumber == null && primaryTypeDisplayName == null)
                        continue;

                    if (!googlePhotosList.isEmpty()) {  //가장 첫 번째 사진만 실제로 다운로드까지 진행하고 나머지는 나중에 쓸 용도로 googlePhotosList에 저장
                        localPhotosList.add(getGoogleMapPlacesImage(googlePhotosList.get(0), googleApiKey));
                        googlePhotosList.remove(0);
                    }

                    //얻어온 가게 상세 정보를 바탕으로 DB에 저장할 객체를 생성
                    Store store = Store.builder()
                            .googlePlaceId(googleMapPlacesDetail.get("id").getAsString())
                            .phoneNumber(phoneNumber)
                            .formattedAddress(googleMapPlacesDetail.get("formattedAddress").getAsString())
                            .location((Point) new WKTReader().read(String.format("POINT(%s %s)", googleMapPlacesDetail.getAsJsonObject("location").get("longitude").getAsString(), googleMapPlacesDetail.getAsJsonObject("location").get("latitude").getAsString())))
                            .regularOpeningHours(regularOpeningHours)
                            .displayName(googleMapPlacesDetail.getAsJsonObject("displayName").get("text").getAsString())
                            .primaryTypeDisplayName(primaryTypeDisplayName)
                            .localPhotos(localPhotosList)
                            .googlePhotos(googlePhotosList)
                            .build();

                    Optional<Certification> safe = certificationRepository.findByName("안심식당");   //Certification 테이블에 이미 안심식당 데이터가 있는지 조회
                    if (safe.isPresent()) {   //만약 존재하는 경우면, Certification은 새로 만들어줄 필요가 없음
                        Optional<StoreCertification> byStoreIdCertificationId = storeCertificationRepository.findByStoreIdCertificationId(store.getId(), safe.get().getId());
                        if (byStoreIdCertificationId.isPresent()) {   //이미 해당 가게가 안심식당으로 StoreCertification에 등록되어있는 경우
                            storeRepository.save(store);
                        } else {
                            StoreCertification storeCertification = StoreCertification.builder()
                                    .store(store)
                                    .certification(safe.get())
                                    .build();
                            storeCertificationRepository.save(storeCertification);
                            storeRepository.save(store);
                        }
                    } else {    //만약 존재하지 않는 경우라면, Certification도 새로 만들어줘야 함
                        Certification safeReal = Certification.builder()
                                .name("안심식당")
                                .build();

                        StoreCertification storeCertification = StoreCertification.builder()
                                .store(store)
                                .certification(safeReal)
                                .build();

                        certificationRepository.save(safeReal);
                        storeCertificationRepository.save(storeCertification);
                        storeRepository.save(store);
                    }

                } catch (ParseException e) {
                    e.printStackTrace();
                }
            } else {    //이미 중복된 Google Place Id가 있을 때! 즉, 동일한 가게가 있을 때는 Certification만 새로 맺어주면 된다.
                Store store = resultByGooglePlaceId.get();  //위에서 조회한 중복된 가게
                Optional<Certification> safe = certificationRepository.findByName("안심식당");   //여기도 마찬가지로 Certification 테이블에 이미 안심식당 데이터가 있는지 조회

                if (safe.isPresent()) {   //만약에 존재한다면, Certification은 새로 만들어줄 필요가 없다
                    Optional<StoreCertification> byStoreIdCertificationId = storeCertificationRepository.findByStoreIdCertificationId(store.getId(), safe.get().getId());
                    if (!byStoreIdCertificationId.isPresent()) {
                        StoreCertification storeCertification = StoreCertification.builder()
                                .store(store)
                                .certification(safe.get())
                                .build();
                        storeCertificationRepository.save(storeCertification);
                    }
                } else {    //만약에 존재하지 않는다면, Certification도 새로 만들어줘야 한다.
                    Certification safeReal = Certification.builder()
                            .name("안심식당")
                            .build();

                    StoreCertification storeCertification = StoreCertification.builder()
                            .store(store)
                            .certification(safeReal)
                            .build();

                    certificationRepository.save(safeReal);
                    storeCertificationRepository.save(storeCertification);
                }
            }

        }
    }

    //이 메서드는 createAllSafeStore() 메서드를 개선하기 위해 구현된 메서드입니다.
    //createAllSafeStore()는 대용량 데이터를 한번에 모두 조회하는 반면에 아래 메서드는 API call 비용을 제한하여 해당 비용까지만 조회합니다.
    //파라미터로 몇 달러까지 API call을 허용할 것인지 정하고, 조회할 시작 위치를 지정합니다.
    //만약 도중에 남은 달러가 없어질 시점이 되면, 그때까지 남은 달러와 다음부터 탐색해야할 인덱스 번호를 리턴하며 만약 모두 조회된 경우에는 남은 달러와 -1을 리턴합니다.
    @Transactional
    public CreateDividedSafeStoresResponse createDividedSafeStores(String fileName, double dollars, int startIndex) {
        List<StoreDataByParser> allSafeStores = SafeDataParser.getAllSafeStores(fileName);
        for (int i=startIndex; i< allSafeStores.size(); ++i) {


            String googleMapPlacesId = getGoogleMapPlacesId(allSafeStores.get(i).getName(), allSafeStores.get(i).getAddress(), googleApiKey);

            if(googleMapPlacesId == null)   //가져온 Google Map Place Id가 null이라는 것은 가게가 하나로 특정되지 않아 사용할 수 없다는 것을 의미
                continue;

            Optional<Store> resultByGooglePlaceId = storeRepository.findByGooglePlaceId(googleMapPlacesId); //Google Map API에서 가져온 place id와 동일한 정보가 디비에 있으면 중복 가게!
            if (resultByGooglePlaceId.isEmpty()) {  //아직 DB에 존재하지 않는 가게인 경우!
                if (dollars - 0.02 < 0) {   //남은 달러와 비교하여 API 호출 비용이 없으면 그 상태에서 중단
                    CreateDividedSafeStoresResponse createDividedSafeStoresResponse = new CreateDividedSafeStoresResponse();
                    createDividedSafeStoresResponse.setDollars(dollars);
                    createDividedSafeStoresResponse.setNextIndex(i);
                    return createDividedSafeStoresResponse;
                }
                JsonObject googleMapPlacesDetail = getGoogleMapPlacesDetail(googleMapPlacesId, googleApiKey); //Google Map API를 통해 해당 가게의 상세 정보를 가져옴, 0.02달러 소비

                //여기선 API call이 진행된 시점이므로, 남은 달러에 반영
                dollars -= 0.02;

                if (googleMapPlacesDetail == null)   //Google Map Place Detail을 제대로 불러오지 못했을 경우에는 skip
                    continue;

                //WKTReader Parse exception에 대한 처리를 위한 try-catch문
                try {
                    List<JsonObject> openList = new ArrayList<JsonObject>();
                    List<JsonObject> closeList = new ArrayList<JsonObject>();
                    List<StoreRegularOpeningHours> regularOpeningHours = new ArrayList<StoreRegularOpeningHours>();
                    List<String> localPhotosList = new ArrayList<>();
                    List<String> googlePhotosList = new ArrayList<>();
                    String phoneNumber = null;
                    String primaryTypeDisplayName = null;

                    //이 아래 4개의 if문들은 해당 값들이 없는 가게가 존재하기 때문에 예외처리 목적으로 작성
                    if (googleMapPlacesDetail.getAsJsonObject("regularOpeningHours") != null && googleMapPlacesDetail.getAsJsonObject("regularOpeningHours").getAsJsonArray("periods") != null) {
                        googleMapPlacesDetail.getAsJsonObject("regularOpeningHours").getAsJsonArray("periods").forEach(period -> {
                            openList.add(period.getAsJsonObject().getAsJsonObject("open"));
                            closeList.add(period.getAsJsonObject().getAsJsonObject("close"));
                        });

                        for (int j = 0; j < openList.size(); ++j) { //Google API에서 가져온 가게 Open, 가게 Close 시간 정보를 파싱하는 과정
                            StoreRegularOpeningHours storeRegularOpeningHours = new StoreRegularOpeningHours();
                            StoreOpenCloseDay storeOpenDay = new StoreOpenCloseDay();
                            StoreOpenCloseDay storeCloseDay = new StoreOpenCloseDay();

                            storeOpenDay.setDay(StoreDay.values()[openList.get(j).get("day").getAsInt()]);
                            storeOpenDay.setHour(openList.get(j).get("hour").getAsInt());
                            storeOpenDay.setMinute(openList.get(j).get("minute").getAsInt());

                            storeCloseDay.setDay(StoreDay.values()[closeList.get(j).get("day").getAsInt()]);
                            storeCloseDay.setHour(closeList.get(j).get("hour").getAsInt());
                            storeCloseDay.setMinute(closeList.get(j).get("minute").getAsInt());

                            storeRegularOpeningHours.setOpen(storeOpenDay);
                            storeRegularOpeningHours.setClose(storeCloseDay);
                            regularOpeningHours.add(storeRegularOpeningHours);
                        }
                    }

                    if (googleMapPlacesDetail.getAsJsonArray("photos") != null) {
                        googleMapPlacesDetail.getAsJsonArray("photos").forEach(photo -> googlePhotosList.add(photo.getAsJsonObject().get("name").getAsString()));
                    }

                    if (googleMapPlacesDetail.get("nationalPhoneNumber") != null) {
                        phoneNumber = googleMapPlacesDetail.get("nationalPhoneNumber").getAsString();
                    } else if (googleMapPlacesDetail.get("internationalPhoneNumber") != null) {
                        phoneNumber = googleMapPlacesDetail.get("internationalPhoneNumber").getAsString();
                    }

                    if (googleMapPlacesDetail.getAsJsonObject("primaryTypeDisplayName") != null && googleMapPlacesDetail.getAsJsonObject("primaryTypeDisplayName").get("text") != null) {
                        primaryTypeDisplayName = googleMapPlacesDetail.getAsJsonObject("primaryTypeDisplayName").get("text").getAsString();
                    }

                    if(phoneNumber == null && primaryTypeDisplayName == null)
                        continue;

                    if (!googlePhotosList.isEmpty()) {
                        if (dollars - 0.007 < 0) { //다음 API를 호출할 돈이 없으면 중단
                            CreateDividedSafeStoresResponse createDividedSafeStoresResponse = new CreateDividedSafeStoresResponse();
                            createDividedSafeStoresResponse.setDollars(dollars);
                            createDividedSafeStoresResponse.setNextIndex(i);
                            return createDividedSafeStoresResponse;
                        }
                        //돈이 충분히 있으면,
                        localPhotosList.add(getGoogleMapPlacesImage(googlePhotosList.get(0), googleApiKey)); //가장 첫 번째 사진만 실제로 다운로드까지 진행하고 나머지는 나중에 쓸 용도로 googlePhotosList에 저장
                        googlePhotosList.remove(0);

                        //소비한 비용 반영
                        dollars -= 0.007;
                    }

                    //얻어온 가게 상세 정보를 바탕으로 DB에 저장할 객체를 생성
                    Store store = Store.builder()
                            .googlePlaceId(googleMapPlacesDetail.get("id").getAsString())
                            .phoneNumber(phoneNumber)
                            .formattedAddress(googleMapPlacesDetail.get("formattedAddress").getAsString())
                            .location((Point) new WKTReader().read(String.format("POINT(%s %s)", googleMapPlacesDetail.getAsJsonObject("location").get("longitude").getAsString(), googleMapPlacesDetail.getAsJsonObject("location").get("latitude").getAsString())))
                            .regularOpeningHours(regularOpeningHours)
                            .displayName(googleMapPlacesDetail.getAsJsonObject("displayName").get("text").getAsString())
                            .primaryTypeDisplayName(primaryTypeDisplayName)
                            .localPhotos(localPhotosList)
                            .googlePhotos(googlePhotosList)
                            .build();

                    Optional<Certification> safe = certificationRepository.findByName("안심식당");   //Certification 테이블에 이미 안심식당 데이터가 있는지 조회
                    if (safe.isPresent()) {   //만약 존재하는 경우면, Certification은 새로 만들어줄 필요가 없음
                        Optional<StoreCertification> byStoreIdCertificationId = storeCertificationRepository.findByStoreIdCertificationId(store.getId(), safe.get().getId());
                        if (byStoreIdCertificationId.isPresent()) {   //이미 해당 가게가 안심식당으로 StoreCertification에 등록되어있는 경우
                            storeRepository.save(store);
                        } else {
                            StoreCertification storeCertification = StoreCertification.builder()
                                    .store(store)
                                    .certification(safe.get())
                                    .build();
                            storeCertificationRepository.save(storeCertification);
                            storeRepository.save(store);
                        }
                    } else {    //만약 존재하지 않는 경우라면, Certification도 새로 만들어줘야 함
                        Certification safeReal = Certification.builder()
                                .name("안심식당")
                                .build();

                        StoreCertification storeCertification = StoreCertification.builder()
                                .store(store)
                                .certification(safeReal)
                                .build();

                        certificationRepository.save(safeReal);
                        storeCertificationRepository.save(storeCertification);
                        storeRepository.save(store);
                    }

                } catch (ParseException e) {
                    e.printStackTrace();
                }
            } else {    //이미 중복된 Google Place Id가 있을 때! 즉, 동일한 가게가 있을 때는 Certification만 새로 맺어주면 된다.
                Store store = resultByGooglePlaceId.get();  //위에서 조회한 중복된 가게
                Optional<Certification> safe = certificationRepository.findByName("안심식당");   //여기도 마찬가지로 Certification 테이블에 이미 안심식당 데이터가 있는지 조회

                if (safe.isPresent()) {   //만약에 존재한다면, Certification은 새로 만들어줄 필요가 없다
                    Optional<StoreCertification> byStoreIdCertificationId = storeCertificationRepository.findByStoreIdCertificationId(store.getId(), safe.get().getId());
                    if (!byStoreIdCertificationId.isPresent()) {
                        StoreCertification storeCertification = StoreCertification.builder()
                                .store(store)
                                .certification(safe.get())
                                .build();
                        storeCertificationRepository.save(storeCertification);
                    }
                } else {    //만약에 존재하지 않는다면, Certification도 새로 만들어줘야 한다.
                    Certification safeReal = Certification.builder()
                            .name("안심식당")
                            .build();

                    StoreCertification storeCertification = StoreCertification.builder()
                            .store(store)
                            .certification(safeReal)
                            .build();

                    certificationRepository.save(safeReal);
                    storeCertificationRepository.save(storeCertification);
                }
            }

        }
        //만약 안심식당 리스트 끝까지 잘 돌았다면,
        CreateDividedSafeStoresResponse createDividedSafeStoresResponse = new CreateDividedSafeStoresResponse();
        createDividedSafeStoresResponse.setDollars(dollars);
        createDividedSafeStoresResponse.setNextIndex(-1);
        return createDividedSafeStoresResponse;
    }
}
