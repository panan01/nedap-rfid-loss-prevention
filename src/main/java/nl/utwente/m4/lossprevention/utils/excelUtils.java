package nl.utwente.m4.lossprevention.utils;

import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFSheet;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class excelUtils {

    public static void main(String[] args) {
        System.out.println(getRowCount(read("20210503_UTwente_Nedap_Stores.xlsx")));
        System.out.println(getCellData(read("20210503_UTwente_Nedap_Stores.xlsx"),0,0));
        System.out.println(getCellData(read("20210503_UTwente_Nedap_Stores.xlsx"),1,1));
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

    /**
     * Function for retrieving the amount of rows (including labels) in the .xlsx file
     * @param sheet
     * @return amount of rows as an int
     */
    public static int getRowCount(XSSFSheet sheet) {
        return sheet.getPhysicalNumberOfRows();
    }

    /**
     * Function for retrieving the content of a specific cell in the .xlsx file given the row and column indexes
     * @param sheet
     * @param row
     * @param column
     * @return cell contents as a String
     */
    public static String getCellData(XSSFSheet sheet, int row, int column){
        // As just retrieving the cell content trough a getNumeric/getString value requires multiple functions and checks a dataFormatter is the optimal solution as it returns a string from any datainput from the cell.
        DataFormatter dataFormatter = new DataFormatter();
        return dataFormatter.formatCellValue(sheet.getRow(row).getCell(column));
    }

}
