package com.nainga.nainga.domain.store.application;

import com.google.gson.JsonObject;
import com.nainga.nainga.domain.certification.dao.CertificationRepository;
import com.nainga.nainga.domain.certification.domain.Certification;
import com.nainga.nainga.domain.gcsguide.GcsService;
import com.nainga.nainga.domain.store.dao.StoreRepository;
import com.nainga.nainga.domain.store.domain.Store;
import com.nainga.nainga.domain.store.domain.StoreDay;
import com.nainga.nainga.domain.store.domain.StoreOpenCloseDay;
import com.nainga.nainga.domain.store.domain.StoreRegularOpeningHours;
import com.nainga.nainga.domain.store.dto.CreateDividedSafeStoresResponse;
import com.nainga.nainga.domain.store.dto.StoreDataByParser;
import com.nainga.nainga.domain.storecertification.dao.StoreCertificationRepository;
import com.nainga.nainga.domain.storecertification.domain.StoreCertification;
import com.nainga.nainga.global.logs.CreateStoresLogger;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.FilenameUtils;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

import static com.nainga.nainga.domain.store.application.GoogleMapMethods.*;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class SafeGoogleMapStoreService {
    @Value("${GOOGLE_API_KEY}")
    private String googleApiKey;    //Spring bean 내에서만 @Value로 프로퍼티를 가져올 수 있어서 Service 단에서 받고 GoogleMapMethods에는 파라미터로 넘겨줌.
    @Value("${CURRENT_PROFILE}")
    private String currentProfile;  //현재 Active Profile을 조회
    private final StoreRepository storeRepository;
    private final CertificationRepository certificationRepository;
    private final StoreCertificationRepository storeCertificationRepository;
    private final GcsService gcsService;

    //아래 생성자 주입을 별도로 작성한 이유는 특정 Profile에서만 의존성 주입이 필요한 GcsService에 @Autowired(required = false)를 적용해주기 위해
    @Autowired
    public SafeGoogleMapStoreService(@Value("${GOOGLE_API_KEY}") String googleApiKey,@Value("${CURRENT_PROFILE}") String currentProfile, StoreRepository storeRepository, CertificationRepository certificationRepository, StoreCertificationRepository storeCertificationRepository, @Autowired(required = false) GcsService gcsService) {
        this.googleApiKey = googleApiKey;
        this.currentProfile = currentProfile;
        this.storeRepository = storeRepository;
        this.certificationRepository = certificationRepository;
        this.storeCertificationRepository = storeCertificationRepository;
        this.gcsService = gcsService;
    }

    //이 메서드는 Safe Excel dataset 파싱을 통해 가게 이름과 주소를 얻고, 이 정보를 바탕으로 Google Map Place Id를 가져옵니다.
    //그 후 얻어진 Google Map Place Id를 가지고 가게 상세 정보를 Google Map API로부터 가져와 Store DB에 저장합니다.
    @Transactional
    public void createAllSafeStores(String fileName) {
        List<StoreDataByParser> allSafeStores = SafeDataParser.getAllSafeStores(fileName);
        String fileNameWithoutExtension = FilenameUtils.removeExtension(fileName);  //Filename에서 확장자 제거
        String logFilePath = CreateStoresLogger.createLogFile("safe", "_" + fileNameWithoutExtension);//정해진 경로에 log file 생성

        for (StoreDataByParser storeDataByParser : allSafeStores) {
            String googleMapPlacesId = getGoogleMapPlacesId(storeDataByParser.getName(), storeDataByParser.getAddress(), googleApiKey);

            if(googleMapPlacesId == null){   //가져온 Google Map Place Id가 null이라는 것은 가게가 하나로 특정되지 않아 사용할 수 없다는 것을 의미
                CreateStoresLogger.writeToLogFile(logFilePath, storeDataByParser.getName());    //로그 파일에 기록
                continue;
            }

            Optional<Store> resultByGooglePlaceId = storeRepository.findByGooglePlaceId(googleMapPlacesId); //Google Map API에서 가져온 place id와 동일한 정보가 디비에 있으면 중복 가게!
            if (resultByGooglePlaceId.isEmpty()) {  //아직 DB에 존재하지 않는 가게인 경우!
                JsonObject googleMapPlacesDetail = getGoogleMapPlacesDetail(googleMapPlacesId, googleApiKey); //Google Map API를 통해 해당 가게의 상세 정보를 가져옴
                if (googleMapPlacesDetail == null)   //Google Map Place Detail을 제대로 불러오지 못했을 경우에는 skip
                    continue;

                //WKTReader Parse exception에 대한 처리를 위한 try-catch문
                try {
                    Set<StoreRegularOpeningHours> regularOpeningHours;
                    Set<String> weekdayDescriptions = new LinkedHashSet<>();
                    Set<String> localPhotosList = new LinkedHashSet<>();
                    Set<String> googlePhotosList = new LinkedHashSet<>();
                    String phoneNumber = null;
                    String primaryTypeDisplayName = null;

                    //이 아래 4개의 if문들은 해당 값들이 없는 가게가 존재하기 때문에 예외처리 목적으로 작성
                    if (googleMapPlacesDetail.getAsJsonObject("regularOpeningHours") != null && googleMapPlacesDetail.getAsJsonObject("regularOpeningHours").getAsJsonArray("weekdayDescriptions") != null) {
                        googleMapPlacesDetail.getAsJsonObject("regularOpeningHours").getAsJsonArray("weekdayDescriptions").forEach(weekdayDescription -> {
                            weekdayDescriptions.add(weekdayDescription.getAsString());
                        });
                    }

                    regularOpeningHours = parseWeekdayDescriptions(weekdayDescriptions);

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
                        if (currentProfile.equals("dev") || currentProfile.equals("prod")) {
                            byte[] googleMapPlacesImageAsBytes = getGoogleMapPlacesImageAsBytes(googlePhotosList.stream().findFirst().get(), googleApiKey);
                            if (googleMapPlacesImageAsBytes != null) {
                                String gcsPath = gcsService.uploadImage(googleMapPlacesImageAsBytes);
                                localPhotosList.add(gcsPath);
                                Iterator<String> iterator = googlePhotosList.iterator();
                                if (iterator.hasNext()) {   //googlePhotosList에서 가장 첫 번째 원소를 제거
                                    iterator.next();
                                    iterator.remove();
                                }
                            }
                        } else {    //local이나 test 시
                            localPhotosList.add(getGoogleMapPlacesImageToLocal(googlePhotosList.stream().findFirst().get(), googleApiKey));
                            Iterator<String> iterator = googlePhotosList.iterator();
                            if (iterator.hasNext()) {   //googlePhotosList에서 가장 첫 번째 원소를 제거
                                iterator.next();
                                iterator.remove();
                            }
                        }
                    }

                    //얻어온 가게 상세 정보를 바탕으로 DB에 저장할 객체를 생성
                    Store store = Store.builder()
                            .googlePlaceId(googleMapPlacesDetail.get("id").getAsString())
                            .phoneNumber(phoneNumber)
                            .formattedAddress(googleMapPlacesDetail.get("formattedAddress").getAsString())
                            .location((Point) new WKTReader().read(String.format("POINT(%s %s)", googleMapPlacesDetail.getAsJsonObject("location").get("longitude").getAsString(), googleMapPlacesDetail.getAsJsonObject("location").get("latitude").getAsString())))
                            .regularOpeningHours(regularOpeningHours)
                            .weekdayDescriptions(weekdayDescriptions)
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
        String fileNameWithoutExtension = FilenameUtils.removeExtension(fileName);  //Filename에서 확장자 제거
        String logFilePath = CreateStoresLogger.createLogFile("safe", "_" + fileNameWithoutExtension + "_dollars=" + String.valueOf(dollars) + "_startIndex=" + String.valueOf(startIndex));//정해진 경로에 log file 생성

        for (int i=startIndex; i< allSafeStores.size(); ++i) {


            String googleMapPlacesId = getGoogleMapPlacesId(allSafeStores.get(i).getName(), allSafeStores.get(i).getAddress(), googleApiKey);

            if(googleMapPlacesId == null){   //가져온 Google Map Place Id가 null이라는 것은 가게가 하나로 특정되지 않아 사용할 수 없다는 것을 의미
                CreateStoresLogger.writeToLogFile(logFilePath, allSafeStores.get(i).getName());    //로그 파일에 기록
                continue;
            }

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
                    Set<StoreRegularOpeningHours> regularOpeningHours;
                    Set<String> weekdayDescriptions = new LinkedHashSet<>();
                    Set<String> localPhotosList = new LinkedHashSet<>();
                    Set<String> googlePhotosList = new LinkedHashSet<>();
                    String phoneNumber = null;
                    String primaryTypeDisplayName = null;

                    //이 아래 4개의 if문들은 해당 값들이 없는 가게가 존재하기 때문에 예외처리 목적으로 작성
                    if (googleMapPlacesDetail.getAsJsonObject("regularOpeningHours") != null && googleMapPlacesDetail.getAsJsonObject("regularOpeningHours").getAsJsonArray("weekdayDescriptions") != null) {
                        googleMapPlacesDetail.getAsJsonObject("regularOpeningHours").getAsJsonArray("weekdayDescriptions").forEach(weekdayDescription -> {
                            weekdayDescriptions.add(weekdayDescription.getAsString());
                        });
                    }

                    regularOpeningHours = parseWeekdayDescriptions(weekdayDescriptions);

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
                        if (currentProfile.equals("dev") || currentProfile.equals("prod")) {
                            byte[] googleMapPlacesImageAsBytes = getGoogleMapPlacesImageAsBytes(googlePhotosList.stream().findFirst().get(), googleApiKey);
                            if (googleMapPlacesImageAsBytes != null) {
                                String gcsPath = gcsService.uploadImage(googleMapPlacesImageAsBytes);
                                localPhotosList.add(gcsPath);
                                Iterator<String> iterator = googlePhotosList.iterator();
                                if (iterator.hasNext()) {   //googlePhotosList에서 가장 첫 번째 원소를 제거
                                    iterator.next();
                                    iterator.remove();
                                }
                                //소비한 비용 반영
                                dollars -= 0.007;
                            }
                        } else {    //local이나 test 시
                            localPhotosList.add(getGoogleMapPlacesImageToLocal(googlePhotosList.stream().findFirst().get(), googleApiKey)); //가장 첫 번째 사진만 실제로 다운로드까지 진행하고 나머지는 나중에 쓸 용도로 googlePhotosList에 저장
                            Iterator<String> iterator = googlePhotosList.iterator();
                            if (iterator.hasNext()) {   //googlePhotosList에서 가장 첫 번째 원소를 제거
                                iterator.next();
                                iterator.remove();
                            }
                            //소비한 비용 반영
                            dollars -= 0.007;
                        }
                    }

                    //얻어온 가게 상세 정보를 바탕으로 DB에 저장할 객체를 생성
                    Store store = Store.builder()
                            .googlePlaceId(googleMapPlacesDetail.get("id").getAsString())
                            .phoneNumber(phoneNumber)
                            .formattedAddress(googleMapPlacesDetail.get("formattedAddress").getAsString())
                            .location((Point) new WKTReader().read(String.format("POINT(%s %s)", googleMapPlacesDetail.getAsJsonObject("location").get("longitude").getAsString(), googleMapPlacesDetail.getAsJsonObject("location").get("latitude").getAsString())))
                            .regularOpeningHours(regularOpeningHours)
                            .weekdayDescriptions(weekdayDescriptions)
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

    //Google Map API에서 제공해주는 영업 시간 정보가 periods와 weekdayDescriptions 방식이 있는데, 기존에 구조화된 periods를 사용했었지만 데이터가 깨져있는 경우가 종종 있어서,
    //조금 더 안정적인 weekdayDescriptions으로 데이터를 받아오고 직접 파싱해서 periods 식으로 변환하여 저장하기 위함
    public Set<StoreRegularOpeningHours> parseWeekdayDescriptions(Set<String> weekdayDescriptions) {
        Set<StoreRegularOpeningHours> storeRegularOpeningHoursList = new LinkedHashSet<>();

        for (String weekdayDescription : weekdayDescriptions) {
            weekdayDescription = weekdayDescription.replace(",", " ");   //,는 제거
            weekdayDescription = weekdayDescription.replace("~", " ~ "); //~은 확실히 포함될 수 있게 앞뒤 공백 추가
            String[] words = weekdayDescription.split("\\s+");   //공백 단위로 단어 분리

            StoreDay storeDay = null;
            boolean isOpenDay = true;
            boolean isBeforeNoon = true;   //오전과 오후를 구분하기 위해 사용
            if(words[0].equals("일요일:")) {
                storeDay = StoreDay.SUN;
            } else if (words[0].equals("월요일:")) {
                storeDay = StoreDay.MON;
            } else if (words[0].equals("화요일:")) {
                storeDay = StoreDay.TUE;
            } else if (words[0].equals("수요일:")) {
                storeDay = StoreDay.WED;
            } else if (words[0].equals("목요일:")) {
                storeDay = StoreDay.THU;
            } else if (words[0].equals("금요일:")) {
                storeDay = StoreDay.FRI;
            } else if (words[0].equals("토요일:")) {
                storeDay = StoreDay.SAT;
            }

            if (words[1].equals("24시간")) {  //24시간 영업일 때
                StoreRegularOpeningHours storeRegularOpeningHours = new StoreRegularOpeningHours();
                StoreOpenCloseDay storeOpenDay = new StoreOpenCloseDay();
                StoreOpenCloseDay storeCloseDay = new StoreOpenCloseDay();
                storeOpenDay.setDay(storeDay);
                storeOpenDay.setHour(0);
                storeOpenDay.setMinute(0);
                storeCloseDay.setDay(storeDay);
                storeCloseDay.setHour(0);
                storeCloseDay.setMinute(0);
                storeRegularOpeningHours.setOpen(storeOpenDay);
                storeRegularOpeningHours.setClose(storeCloseDay);
                storeRegularOpeningHoursList.add(storeRegularOpeningHours);
                continue;
            } else if (words[1].equals("휴무일")) {    //휴무인경우
                continue;
            }

            StoreOpenCloseDay storeOpenDay = new StoreOpenCloseDay();
            StoreOpenCloseDay storeCloseDay = new StoreOpenCloseDay();
            StoreRegularOpeningHours storeRegularOpeningHours = new StoreRegularOpeningHours();
            for (int i = 1; i < words.length; ++i) {

                if (words[i].equals("오전")) {
                    isBeforeNoon = true;
                    continue;
                } else if (words[i].equals("오후")) {
                    isBeforeNoon = false;
                    continue;
                } else if (words[i].equals("~")) {
                    isOpenDay = false;
                    continue;
                }

                if (isOpenDay) {
                    String[] time = words[i].split(":");
                    int hours = Integer.parseInt(time[0]);
                    int minutes = Integer.parseInt(time[1]);

                    if(hours == 12 && isBeforeNoon) {   //오전 12시 XX분의 경우는 0시 XX분으로 표현
                        hours = 0;
                    }

                    if (!isBeforeNoon && hours != 12) { //오후 12시 XX분의 경우에는 추가로 12시를 더해주면 안됨
                        hours += 12;
                    }

                    storeOpenDay.setDay(storeDay);
                    storeOpenDay.setHour(hours);
                    storeOpenDay.setMinute(minutes);
                } else if (!isOpenDay) {
                    String[] time = words[i].split(":");
                    int hours = Integer.parseInt(time[0]);
                    int minutes = Integer.parseInt(time[1]);

                    if(hours == 12 && isBeforeNoon) {   //오전 12시 XX분의 경우는 0시 XX분으로 표현
                        hours = 0;
                    }

                    if (!isBeforeNoon && hours != 12) { //오후 12시 XX분의 경우에는 추가로 12시를 더해주면 안됨
                        hours += 12;
                    }

                    storeCloseDay.setDay(storeDay);
                    storeCloseDay.setHour(hours);
                    storeCloseDay.setMinute(minutes);

                    storeRegularOpeningHours.setOpen(storeOpenDay);
                    storeRegularOpeningHours.setClose(storeCloseDay);
                    storeRegularOpeningHoursList.add(storeRegularOpeningHours);
                    storeRegularOpeningHours = new StoreRegularOpeningHours();    //안전하게 재활용하기 위해 명시적으로 재할당
                    storeOpenDay = new StoreOpenCloseDay();
                    storeCloseDay = new StoreOpenCloseDay();

                    isOpenDay = true;   //OpenDay와 CloseDay가 한짝이 맞았으므로 이제 다시 OpenDay 시기가 된 것
                }
            }
        }
        return storeRegularOpeningHoursList;
    }
}
