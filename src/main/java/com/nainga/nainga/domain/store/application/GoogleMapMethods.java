package com.nainga.nainga.domain.store.application;

import com.google.gson.*;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

public class GoogleMapMethods {
    //아래 메서드는 Google Map API 내 사진 요청 API를 트리거하여 API call
    public static String getGoogleMapPlacesImage(String photosName, String googleApiKey) {
        String maxWidthPx = "400";
        String maxHeightPx = "400";

        String reqURL = "https://places.googleapis.com/v1/" + photosName + "/media?maxHeightPx=" + maxHeightPx + "&maxWidthPx=" + maxWidthPx + "&key=" + googleApiKey;

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
    public static String getGoogleMapPlacesId(String name, String address, String googleApiKey) {
        String reqURL = "https://places.googleapis.com/v1/places:searchText";

        // 정규표현식을 사용하여 처음 "(", "," "."가 나타나는 곳부터 문자열 끝까지 삭제! 이렇게 해야 상세 주소의 동,호수,층 등이 없어져서 검색 정확도가 높아진다.
        address = address.replaceAll("[,(\\.].*", "");
        String textQuery = address + name;

        try {
            URL url = new URL(reqURL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            conn.setRequestMethod("POST");  //Google Map API의 정해진 헤더
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("X-Goog-Api-Key", googleApiKey);
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
    public static JsonObject getGoogleMapPlacesDetail(String googleMapPlacesId, String googleApiKey) {
        String reqURL = "https://places.googleapis.com/v1/places/" + googleMapPlacesId + "?languageCode=ko";

        try {
            URL url = new URL(reqURL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            conn.setRequestMethod("GET");  //Google Map API의 정해진 헤더
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("X-Goog-Api-Key", googleApiKey);
            conn.setRequestProperty("X-Goog-FieldMask", "id,displayName,primaryTypeDisplayName,formattedAddress,regularOpeningHours.periods,location,internationalPhoneNumber,photos.name,photos.widthPx,photos.heightPx");

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
