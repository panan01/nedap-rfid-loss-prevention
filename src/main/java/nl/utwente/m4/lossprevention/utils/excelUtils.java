package nl.utwente.m4.lossprevention.utils;

import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;

import org.json.*;

import java.io.*;
import java.sql.Connection;
import java.util.ArrayList;

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import static nl.utwente.m4.lossprevention.utils.sqlUtils.*;

public class excelUtils {

    /**
     * For testing purposes
     */
    public static void main(String[] args) {
        //System.out.println(getRowCount(read("20210503_UTwente_Nedap_Stores.xlsx")));
        //System.out.println(getCellData(read("20210503_UTwente_Nedap_Stores.xlsx"), 0, 5));
        //System.out.println(getCellData(read("20210503_UTwente_Nedap_Stores.xlsx"), 1, 1));

        /*System.out.println(getRowCount(exportSheet(2)));*/
        XSSFSheet sheet = exportSheet(0);
        for (int i = 0; i < 53; i++) {
            String row = "";
            for (int k = 0; k < getColumnLabels(sheet).size(); k++) {
                /*System.out.println("size "+ getColumnLabels(exportSheet(2)).size());*/
                row += getCellData(sheet, i, k) + " | ";
            }
            System.out.println(row);

        }

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
     * Function for retrieving the excel file from an inputstream
     *
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

    public static XSSFSheet exportSheet(int sheetType) {
        //Create a new Workbook
        XSSFWorkbook workbook = new XSSFWorkbook();

        //Create a blank sheet and add name
        String sheetName = "exportedsheet-";

        //ArrayList<String> subQueryList = new ArrayList<>();

        switch (sheetType) {
            case 0:
                sheetName += "alarm";
                break;
            case 1:
                sheetName += "article";
                break;
            case 2:
                sheetName += "store";
                break;
            default:
        }

        XSSFSheet sheet = workbook.createSheet(sheetName);

        //Get and assign the data for the excel sheet
        JSONArray jsonarray = getTableJsonList(sheetType);

        //Iterate over data and write it to the sheet

        int rowNumber = 0;
        int columnNumber = 0;
        // Because the labels aren't exported from the database we add them back in here:
        Row labels = sheet.createRow(rowNumber);

        for (String label : getRequiredLabels(sheetType)) {
            Cell cellLabel = labels.createCell(columnNumber);
            cellLabel.setCellValue(getRequiredLabels(sheetType).get(columnNumber));
            columnNumber++;
        }

        rowNumber++;
        columnNumber = 0;

        // Now add the rest of the rows containing the data
        for (Object object : jsonarray) {
            //make JSON object from object
            JSONObject jsonObject = new JSONObject(object.toString());

            //Create empty row at
            Row row = sheet.createRow(rowNumber);
             columnNumber = 0;
            // create cells

            for (String label : getRequiredLabels(sheetType)) {

                Cell cell = row.createCell(columnNumber);

                String key = getRequiredLabels(sheetType).get(columnNumber).toLowerCase();

                switch (key) {
                    case "store id (ut)":
                    case "article id (ut)":
                        key = "id";
                        break;
                    case "latitude (ut)":
                        key = "latitude";
                        break;
                    case "longitude (ut)":
                        key = "longitude";
                        break;
                    case "category (ut)":
                        key = "category";
                        break;
                    case "article (ut)":
                        key = "product";
                        break;
                    case "price (eur)":
                        key = "price";
                        break;
                    case "epc (ut)":
                        key = "epc";
                        break;
                }

                cell.setCellValue(jsonObject.get(key).toString());
                columnNumber++;
            }

            rowNumber++;
        }


        return sheet;
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

        while (!(getCellData(sheet, row, column).equals("null") || getCellData(sheet, row, column).equals(""))) {

            columnLabels.add(getCellData(sheet, row, column));
            column++;
        }

        if (columnLabels.size() < 1) {
            columnLabels.add("Empty file");
        }

        return columnLabels;
    }

}
