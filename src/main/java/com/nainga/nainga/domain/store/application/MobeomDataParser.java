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
모범 음식점 관련 데이터들을 파싱하기 위한 클래스
 */
public class MobeomDataParser {
    //모범 음식점 엑셀 파일로부터 가게 이름과 주소지만을 파싱해서 모두 가져오는 메서드
    //테스트에서 번거롭게 Mocking 파일을 쓰지 않고 Test 파일 이름을 지정해줄 수 있도록 fileName을 파라미터에 지정
    public static List<StoreDataByParser> getAllMobeomStores(String fileName) {
        List<StoreDataByParser> storeDataByParserList = new ArrayList<>();

        //프로젝트 폴더 내 resources/data 폴더에 접근하기 위해 절대 경로를 설정
        //Windows와 Linux의 경로 표기법이 \와 /로 다르므로 이를 모두 호환하기 위해 File.separator 사용
        String absolutePath = new File("").getAbsolutePath() + File.separator + "src" + File.separator + "main" + File.separator + "resources" + File.separator + "data" + File.separator + "mobeom" + File.separator;
//        fileName = "mobeom_20240104.xlsx";   //데이터를 추출한 데이터셋 Excel 파일명

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

                Cell statusCell = row.getCell(9);   //영업중인지 폐업중인지를 나타내는 9번 컬럼에 대해 검사
                if (isCellEmpty(statusCell)) {  //이렇게 isCellEmpty() Method처럼 다중으로 검증해주지 않으면 POI에서 제대로 Blank cell이나 Empty cell에 대해서 처리를 잘 못한다.
                    continue;
                }

                if (statusCell.getCellType() == CellType.STRING) {  //빡빡하게 타입 검사! String 일때만 비교하기
                    if(statusCell.getStringCellValue().equals("폐업"))    //영업 상태가 폐업이면 skip
                        continue;
                }

                Cell unregisteredCell = row.getCell(13);    //모범 음식점 지정 취소가되면 해당 컬럼에 날짜가 기입된다. 따라서, 해당 Cell 값이 비어있어야 현재도 모범 음식점으로 지정된 곳이다.
                if(!isCellEmpty(unregisteredCell))
                    continue;

                StoreDataByParser storeDataByParser = new StoreDataByParser();

                Cell nameCell = row.getCell(4); //가개 이름이 저장되어있는 4번 컬럼 조회
                if(isCellEmpty(nameCell))   //Empty cell, Blank cell Empty string 여부를 검사
                    continue;

                if (nameCell.getCellType() == CellType.STRING) {
                    storeDataByParser.setName(nameCell.getStringCellValue());
                }

                Cell streetNumberAddressCell = row.getCell(7);  //지번 주소가 적힌 컬럼을 먼저 조회!
                if (!isCellEmpty(streetNumberAddressCell)) {
                    if(streetNumberAddressCell.getCellType() == CellType.STRING) {
                        storeDataByParser.setAddress(streetNumberAddressCell.getStringCellValue());
                    }
                } else {
                    Cell streetNameAddressCell = row.getCell(6);
                    if (!isCellEmpty(streetNameAddressCell)) {  //만약에 지번 주소가 없는 가게면 도로명 주소를 조회
                        if(streetNameAddressCell.getCellType() == CellType.STRING) {
                            storeDataByParser.setAddress(streetNameAddressCell.getStringCellValue());
                        }
                    } else {    //지번 주소와 도로명 주소가 모두 없는 가게면 데이터를 사용할 수 없으므로 skip
                        continue;
                    }
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
