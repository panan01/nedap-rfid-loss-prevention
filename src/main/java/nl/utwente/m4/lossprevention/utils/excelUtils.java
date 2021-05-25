package nl.utwente.m4.lossprevention.utils;

import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFSheet;

import java.io.*;
import java.util.ArrayList;

public class excelUtils {

    /**
     * For testing purposes
     */
    public static void main(String[] args) {
        System.out.println(getRowCount(read("20210503_UTwente_Nedap_Stores.xlsx")));
        System.out.println(getCellData(read("20210503_UTwente_Nedap_Stores.xlsx"), 0, 5));
        System.out.println(getCellData(read("20210503_UTwente_Nedap_Stores.xlsx"), 1, 1));

    }

    /**
     * Function for retrieving the excel file from the data folder
     *
     * @param sheetName name of the sheet
     * @return null if something went wrong otherwise an XSSFSheet of the excel file
     */
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
        catch (NoClassDefFoundError | IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Function for retrieving the excel file from the data folder
     * @param sheetInputStream InputStream object of the excel-sheet
     * @return null if something went wrong otherwise an XSSFSheet of the excel file
     */
    public static XSSFSheet read(InputStream sheetInputStream) {
        try {
            //Get the Excel File
            XSSFWorkbook workbook = new XSSFWorkbook(sheetInputStream);

            // Get the sheet, which is for now the first one
            return workbook.getSheetAt(0);
        }
        // Possible exceptions related to accessing the file
        catch (NoClassDefFoundError | IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Function for retrieving the amount of rows (including labels) in the .xlsx file
     *
     * @param sheet
     * @return amount of rows as an int
     */
    public static int getRowCount(XSSFSheet sheet) {
        return sheet.getPhysicalNumberOfRows();
    }

    /**
     * Function for retrieving the content of a specific cell in the .xlsx file given the row and column indexes
     *
     * @param sheet
     * @param row
     * @param column
     * @return cell contents as a String
     */
    public static String getCellData(XSSFSheet sheet, int row, int column) {
        // As just retrieving the cell content trough a getNumeric/getString value requires multiple functions and checks a dataFormatter is the optimal solution as it returns a string from any datainput from the cell.
        DataFormatter dataFormatter = new DataFormatter();
        try {
            return dataFormatter.formatCellValue(sheet.getRow(row).getCell(column));
        } catch (NullPointerException e) {
            return "null";
        }
    }

    /**
     * Function for retrieving the content of the column labels
     *
     * @param sheet
     * @return returns Arraylist with the column labels, returns "Empty file" if no labels are found
     */
    public static ArrayList<String> getColumnLabels(XSSFSheet sheet) {
        int row = 0;
        int column = 0;
        ArrayList<String> columnLabels = new ArrayList<String>();

        while (!getCellData(sheet, row, column).equals("null")) { //TODO Apparently infinite loop check tomorrow am too tired now, apparently never returns null even with large column
            System.out.println("Col "+column);
            System.out.println("Row "+row);
            columnLabels.add(getCellData(sheet, row, column));
            column++;
        }

        if (columnLabels.size() < 1) {
            columnLabels.add("Empty file");
        }

        return columnLabels;
    }

}
