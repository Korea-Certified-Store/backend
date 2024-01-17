package com.nainga.nainga.global.logs;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class CreateStoresLogger {
    //가게 데이터를 생성하는 과정에서 발생하는 로그들을 기록할 로그 파일 생성
    public static String createLogFile(String directory, String fileName) { //directory는 현재까지는 mobeom, goodPrice, safe가 있다.
        String absolutePath = new File("").getAbsolutePath() + File.separator + "src" + File.separator + "main" + File.separator + "resources" + File.separator + "logs" + File.separator + directory + File.separator;

        File logDirectory = new File(absolutePath);

        if (!logDirectory.exists()) {   //해당 경로에 디렉토리들이 존재하지 않으면 생성
            logDirectory.mkdirs();
        }

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss");
        String logFileName = simpleDateFormat.format(new Date()) + fileName + ".txt";   //현재 시각을 파일 이름에 함께 기록

        File logFile = new File(logDirectory, logFileName);

        try {
            logFile.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return absolutePath + logFileName;  //생성한 파일의 경로 리턴
    }

    //이미 생성된 로그 파일의 경로와 기록할 message를 전달받아 해당 파일에 기록
    public static void writeToLogFile(String path, String message) {    //메시지 포맷 => [WARNING] Google Map에서 끝내 찾지 못한 가게 이름: 김밥천국
        File logFile = new File(path);

        try (FileWriter fileWriter = new FileWriter(logFile, true)) {
            fileWriter.write(message + System.lineSeparator());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
