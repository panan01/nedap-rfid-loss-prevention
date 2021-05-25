package nl.utwente.m4.lossprevention.utils;

import java.sql.*;

import org.apache.poi.xssf.usermodel.XSSFSheet;

import java.util.ArrayList;

import static nl.utwente.m4.lossprevention.utils.excelUtils.*;

public class sqlUtils {
    /**
     * For testing purposes
     */
    public static void main(String[] args) {
        System.out.println(XSSFSheet_to_DB(read("20210503_UTwente_Nedap_Stores.xlsx")));
    }

    public static void driverLoader() {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException cnfE) {
            System.err.println("Error loading driver: " + cnfE);
        }

    }

    /**
     * Creates connection with database
     *
     * @return
     */
    public static Connection getConnection() {
        try {
            // Sets basis for connection
            String host = "bronto.ewi.utwente.nl";
            String dbName = "dab_di20212b_225";
            String url = "jdbc:postgresql://"
                    + host + ":5432/" + dbName + "?currentSchema=nedap";

            // Sets credentials
            String username = "dab_di20212b_225";
            String password = "4gPNr326lyRQcR1J";
            Connection connection =
                    DriverManager.getConnection(url, username, password);
            return connection;


        } catch (SQLException sqlE) {
            System.err.println("Error connecting: " + sqlE);
            return null;
        }
    }

    /**
     * Basic function to execute queries to respective connection, if the query has a return then it's returned as a colon separated String
     *
     * @param connection
     * @param query
     * @return
     */
    public static String executeQuery(Connection connection, String query) {
        try {
            PreparedStatement st =
                    connection.prepareStatement(query);

            // Check if query needs input for prepared statement.
            if (query.contains("?")) {
                //st.setString(); //TODO make conventions for our queries
            }

            ResultSet resultSet =
                    st.executeQuery();

            String result = "";
            // prints query results
            while (resultSet.next()) {
                result += resultSet.getString(1) + ":";
            }

            // A wise man once said that if you open a door you should also close it
            connection.close();
            return result;
        } catch (SQLException sqlE) {
            System.err.println("Error connecting: " + sqlE);
            return "sqlException";
        }
    }

    //==============================================  Mixed utils ===================================================\\

    /**
     * For importing data from the excel file and adding this to the database
     *
     * @param sheet excel file to be imported into the DB
     * @return returns status-code
     * "Status-0" if successfully executed
     * "Status-1" if empty file was used
     * "Status-2" if columns don't all match any of the required labels
     */
    public static String XSSFSheet_to_DB(XSSFSheet sheet) {

        //Check file contents and the correct method for parsing
        ArrayList<String> columnLabels = getColumnLabels(sheet);
        if (!columnLabels.get(0).equals("Empty file")) {
            int fileType = -1;

            //TODO clean code below up so it's not all in here

            // Type one is of alarm type
            ArrayList<String> requiredLabelsType1 = new ArrayList<>();
            requiredLabelsType1.add("Store ID (UT)");
            requiredLabelsType1.add("Timestamp");
            requiredLabelsType1.add("EPC (UT)");
            requiredLabelsType1.add("Article ID (UT)");

            // Type two is of article type
            ArrayList<String> requiredLabelsType2 = new ArrayList<>();
            requiredLabelsType2.add("Article ID (UT)");
            requiredLabelsType2.add("Category (UT)");
            requiredLabelsType2.add("Article (UT)");
            requiredLabelsType2.add("Color");
            requiredLabelsType2.add("Size");
            requiredLabelsType2.add("Price (EUR)");

            // Type three is of store type
            ArrayList<String> requiredLabelsType3 = new ArrayList<>();
            requiredLabelsType3.add("Store ID (UT)");
            requiredLabelsType3.add("Latitude (UT)");
            requiredLabelsType3.add("Longitude (UT)");

            if (checkLabels(columnLabels, requiredLabelsType1)) {
                parsePushToDB(sheet, requiredLabelsType1, 1);
            } else if (checkLabels(columnLabels, requiredLabelsType2)) {
                parsePushToDB(sheet, requiredLabelsType2, 2);
            } else if (checkLabels(columnLabels, requiredLabelsType3)) {
                parsePushToDB(sheet, requiredLabelsType3, 3);
            } else {
                return "Status-2";
            }

            return "Status-0";
        } else {
            return "Status-1";
        }


    }

    /**
     * Function for checking if the labels correspond to the required labels of a given type
     *
     * @param columnLabels
     * @param requiredLabels
     * @return
     */
    public static boolean checkLabels(ArrayList<String> columnLabels, ArrayList<String> requiredLabels) {
        int requiredFoundCount = 0;
        for (String label : columnLabels) {
            for (String requiredLabel : requiredLabels) {
                if (requiredLabel.equals(label)) {
                    //TODO make more efficient
                    requiredFoundCount++;
                }
            }
        }
        if (requiredFoundCount == requiredLabels.size()) {
            return true;
        } else {
            return false;
        }
    }

    public static void parsePushToDB(XSSFSheet sheet, ArrayList<String> requiredLabels, int type) {
        int row = 0;
        int column = 0;
        int requiredLabelIterator = 0;
        ArrayList<Integer> indexArray = new ArrayList<>();

        // Get's the indexes of the required labels. //TODO decide if separate function
        while (!getCellData(sheet, row, column).equals("null")) {

            ArrayList<String> columnLabel = new ArrayList<String>();
            columnLabel.add(getCellData(sheet, row, column));

            ArrayList<String> requiredLabel = new ArrayList<String>();
            requiredLabel.add(requiredLabels.get(requiredLabelIterator));

            if (checkLabels(columnLabel, requiredLabel)) {
                indexArray.add(column);
                requiredLabelIterator++;
            }

            column++;
        }

        ArrayList<String> parsedSheetRowStrings = new ArrayList<>();
        String sheetRow = "";
        row = 1;
        column = indexArray.get(0); //start with primary key column

        // While a next row is not empty we put all rows into separate strings


        while (!getCellData(sheet, row, column).equals("null")) {
            for (Integer index : indexArray) {
                sheetRow += getCellData(sheet, row, index) + ", ";
            }
            parsedSheetRowStrings.add(sheetRow);
            sheetRow = "";
            row++;
        }

        for (String parsedRow : parsedSheetRowStrings) {
            driverLoader();
            String insertQuery = "INSERT INTO ";
            switch (type) {
                case 1:
                    insertQuery += "alarm ";
                    break;
                case 2:
                    insertQuery += "article ";
                    break;
                case 3:
                    insertQuery += "store ";
                    break;
            }
            insertQuery += "VALUES (" + parsedRow + ");";
            executeQuery(getConnection(), insertQuery);
        }
    }

}
