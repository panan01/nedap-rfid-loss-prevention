package nl.utwente.m4.lossprevention.utils;

import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFSheet;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class excelUtils {

    public static void main(String[] args) {
        System.out.println(getRowCount("20210503_UTwente_Nedap_Stores.xlsx")); 
    }

    public static int getRowCount(String sheetName) {
        try {
            //Get the Excel File
            String excelPath = "./data/" + sheetName;
            FileInputStream file = new FileInputStream(new File(excelPath));
            XSSFWorkbook workbook = new XSSFWorkbook(file);

            // Get the sheet, which is for now the first one
            XSSFSheet sheet = workbook.getSheetAt(0);

            // Read rowcount
            int rowCount = sheet.getPhysicalNumberOfRows();
            file.close();
            return rowCount;
        }

        catch (FileNotFoundException e) {
            e.printStackTrace();
            return -1;
        } catch (IOException e) {
            e.printStackTrace();
            return -1;
        }


    }

}
