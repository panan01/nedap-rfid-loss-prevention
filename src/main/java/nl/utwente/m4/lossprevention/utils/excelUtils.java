package nl.utwente.m4.lossprevention.utils;

import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFSheet;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class excelUtils {

    public static void main(String[] args) {
        System.out.println(getRowCount(read("20210503_UTwente_Nedap_Stores.xlsx")));
    }

    public static XSSFSheet read(String sheetName) {
        try {
            //Get the Excel File
            String excelPath = "./data/" + sheetName;
            FileInputStream file = new FileInputStream(new File(excelPath));
            XSSFWorkbook workbook = new XSSFWorkbook(file);

            // Get the sheet, which is for now the first one
            XSSFSheet sheet = workbook.getSheetAt(0);
            file.close();
            return sheet;
        }

        // Possible exceptions related to accessing the file
        catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } catch (NoClassDefFoundError e) {
            e.printStackTrace();
            return null;
        }
    }

    public static int getRowCount(XSSFSheet sheet) {
        // Read rowcount
        return sheet.getPhysicalNumberOfRows();
    }

}
