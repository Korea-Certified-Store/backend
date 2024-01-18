package com.nainga.nainga.domain.store.application;

import com.nainga.nainga.domain.store.dto.StoreDataByParser;
import org.apache.commons.io.FilenameUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/*
착한가격업소 관련 데이터들을 파싱하기 위한 클래스
 */
public class GoodPriceDataParser {
    //착한가격업소 엑셀 파일로부터 가게 이름과 주소지만을 파싱해서 모두 가져오는 메서드
    //테스트에서 번거롭게 Mocking 파일을 쓰지 않고 Test 파일 이름을 지정해줄 수 있도록 fileName을 파라미터에 지정
    public static List<StoreDataByParser> getAllGoodPriceStores(String fileName) {
        List<StoreDataByParser> storeDataByParserList = new ArrayList<>();

        //프로젝트 폴더 내 resources/data 폴더에 접근하기 위해 절대 경로를 설정
        //Windows와 Linux의 경로 표기법이 \와 /로 다르므로 이를 모두 호환하기 위해 File.separator 사용
        String absolutePath = new File("").getAbsolutePath() + File.separator + "src" + File.separator + "main" + File.separator + "resources" + File.separator + "data" + File.separator + "goodPrice" + File.separator;
//        fileName = "goodPrice_20230428.xlsx";   //데이터를 추출한 데이터셋 Excel 파일명

        String filePath = absolutePath + fileName;
        String extension = FilenameUtils.getExtension(fileName);

        try (FileInputStream fileInputStream = new FileInputStream(new File(filePath))) {
            Workbook workbook = null;
            if (extension.equals("xlsx")) { //파일 확장자를 읽어들여서 xlsx 파일이면 XSSFWorkBook 포맷으로 생성
                workbook = new XSSFWorkbook(fileInputStream);
            } else if (extension.equals("xls")) {   //xls 파일이면 HSSFWorkBook 포맷으로 생성
                workbook = new HSSFWorkbook(fileInputStream);
            } else {
                throw new IOException("올바르지 않은 파일 확장자입니다. Excel 파일 확장자만 허용됩니다.");
            }

            Sheet sheet = workbook.getSheetAt(0);   //데이터를 읽어들일 Excel 파일의 sheet number

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {   //0번째 Row는 column title이라 제외하고 1번부터 마지막 Row까지 조회
                Row row = sheet.getRow(i);

                StoreDataByParser storeDataByParser = new StoreDataByParser();

                Cell nameCell = row.getCell(3); //가게 이름이 저장되어있는 3번 컬럼 조회
                if(isCellEmpty(nameCell))   //Empty cell, Blank cell Empty string 여부를 검사
                    continue;

                if (nameCell.getCellType() == CellType.STRING) {
                    storeDataByParser.setName(nameCell.getStringCellValue());
                }

                Cell addressCell = row.getCell(5);  //주소가 적힌 컬럼을 조회!
                if (!isCellEmpty(addressCell)) {
                    if(addressCell.getCellType() == CellType.STRING) {
                        storeDataByParser.setAddress(addressCell.getStringCellValue());
                    }
                } else {
                    continue;   //주소가 없는 가게면 skip!
                }

                storeDataByParserList.add(storeDataByParser);   //위에서 파싱한 각 가게별 데이터를 List에 담기
            }
        } catch (IOException e) {   //입출력 예외처리
            e.printStackTrace();
        }
        return storeDataByParserList;
    }

    public static boolean isCellEmpty(final Cell cell) {    //POI에서 Empty cell이나 Blank cell을 처리하는 로직이 까다로워서 따로 메서드로 추출
        if (cell == null) {
            return true;
        } else if (cell.getCellType() == CellType.BLANK) {
            return true;
        } else if (cell.getCellType() == CellType.STRING && cell.getStringCellValue().trim().isEmpty()) {
            return true;
        }

        return false;
    }
}
