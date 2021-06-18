package nl.utwente.m4.lossprevention.utils;

import java.sql.*;

import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.util.ArrayList;


import static nl.utwente.m4.lossprevention.utils.excelUtils.*;

public class sqlUtils {
    private int type;

    /**
     * For testing purposes
     */
    public static void main(String[] args) {
        // System.out.println(XSSFSheet_to_DB(read("20210503_UTwente_Nedap_Stores.xlsx")));
        // System.out.println(XSSFSheet_to_DB(read("20210503_UTwente_Nedap_Articles.xlsx")));
        // System.out.println(XSSFSheet_to_DB(read("20210503_UTwente_Nedap_Alarms.xlsx")));

        String query = "SELECT array_to_json(array_agg(t)) FROM (?) AS t;1-1|size|-1-1-1-1-1";
        String realQuery = "1-1|size|-1-1-1-1-1-1";

        System.out.println(generateSetStringInputs(realQuery));


    }

    //============================================== Database Utils  ===============================================\\
    private static ArrayList<String> requiredLabelsType1 = new ArrayList<>();
    private static ArrayList<String> requiredLabelsType2 = new ArrayList<>();
    private static ArrayList<String> requiredLabelsType3 = new ArrayList<>();


    /**
     * Function for filling the required labels
     */
    private static void fillRequiredLabels() {
        // Type one is of alarm type

        requiredLabelsType1.add(0, "EPC (UT)");
        requiredLabelsType1.add(1, "Timestamp");
        requiredLabelsType1.add(2, "Store ID (UT)");
        requiredLabelsType1.add(3, "Article ID (UT)");

        // Type two is of article type

        requiredLabelsType2.add(0, "Article ID (UT)");
        requiredLabelsType2.add(1, "Category (UT)");
        requiredLabelsType2.add(2, "Article (UT)");
        requiredLabelsType2.add(3, "Color");
        requiredLabelsType2.add(4, "Size");
        requiredLabelsType2.add(5, "Price (EUR)");

        // Type three is of store type

        requiredLabelsType3.add(0, "Store ID (UT)");
        requiredLabelsType3.add(1, "Latitude (UT)");
        requiredLabelsType3.add(2, "Longitude (UT)");
    }

    /**
     * Function for getting the required labels
     *
     * @param i labeltype
     * @return Labels
     */
    public static ArrayList<String> getRequiredLabels(int i) {
        switch (i) {
            case 0:
                return requiredLabelsType1;

            case 1:
                return requiredLabelsType2;

            case 2:
                return requiredLabelsType3;

            default:
                return null;
        }
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
        driverLoader();
        try {
            // Sets basis for connection
            String host = "bronto.ewi.utwente.nl";
            String dbName = "dab_di20212b_225";
            String url = "jdbc:postgresql://" + host + ":5432/" + dbName + "?currentSchema=nedap";

            // Sets credentials
            String username = "dab_di20212b_225";  // TODO: make these system variables or something
            String password = "4gPNr326lyRQcR1J";
            return DriverManager.getConnection(url, username, password);
        } catch (SQLException sqlE) {
            System.err.print("Error connecting to database: ");
            sqlE.printStackTrace(System.err);
            return null;
        }
    }

    public static ArrayList<String> getVariables(String variableString) {
        ArrayList<String> variableArrayList = new ArrayList<>();

        if ((variableString.charAt(0) == '|') && (variableString.charAt(variableString.length() - 1) == '|')) {

            int i = 1;
            int arrayIndex = 0;
            String variable = "";

            while (variableString.charAt(i) != '|') {
                if (variableString.charAt(i) == ':') {
                    variableArrayList.add(arrayIndex, variable);
                    variable = "";
                    arrayIndex++;
                } else {
                    variable += variableString.charAt(i);
                    i++;
                }
            }
            variableArrayList.add(arrayIndex, variable);
        }
        return variableArrayList;
    }

    public static String generateSetStringInputs(String query) {

        String[] generationCode = query.split("-");

        String generatedQuery = "";
        switch (generationCode[0].charAt(0)) {
            case '0':
                generatedQuery += "SELECT ";
                break;
            case '1':
                generatedQuery += "SELECT DISTINCT ";
                break;
            default:

        }
        ArrayList<String> variables = getVariables(generationCode[1].substring(1));
        switch (generationCode[1].charAt(0)) {
            case '0':
                generatedQuery += "* ";
                break;
            case '1':

                generatedQuery += "nedap.article." + variables.get(0) + ", COUNT(nedap.article." + variables.get(0) + ") ";
                break;
            case '2':

                generatedQuery += "nedap.alarm." + variables.get(0) + ", COUNT(nedap.alarm." + variables.get(0) + ") ";
                break;
            case '3':
                generatedQuery += "nedap.store.id AS store_id, nedap.store.longitude, nedap.store.latitude ";
                break;
            case '4':

                if (variables.get(2).equals("0")) {
                    generatedQuery += "SUM(";
                } else if (variables.get(2).equals("1")) {
                    generatedQuery += "COUNT(";
                }
                generatedQuery += variables.get(0) + ") AS " + variables.get(1) + " ";
                break;
            case '5':

                generatedQuery += "nedap." + variables.get(1) + "." + variables.get(0) + ", COUNT(" + variables.get(0) + ") AS " + variables.get(2) + " ";
                break;
            case '6':
                generatedQuery += "day AS weekday, COUNT(day) ";
            default:

        }

        generatedQuery += "FROM ";
        variables = getVariables(generationCode[2].substring(1));
        switch (generationCode[2].charAt(0)) {
            case '0':

                break;
            case '1':

                generatedQuery += "nedap." + variables.get(0) + " ";
                break;
            case '2':

                generatedQuery += "nedap." + variables.get(0) + " nedap." + variables.get(1) + " ";
                break;
            case '3':

                generatedQuery += "nedap." + variables.get(0) + " nedap." + variables.get(1) + " nedap." + variables.get(2) + " ";
                break;
            case '4':

                if (variables.get(0).equals("1")) {
                    generatedQuery += "(SELECT DISTINCT nedap.alarm.store_id, COUNT(nedap.alarm.store_id) AS stolen_items FROM nedap.alarm, nedap.article WHERE nedap.article.id = nedap.alarm.article_id AND date(timestamp) >= " + variables.get(1) + " AND date(timestamp) <= " + variables.get(1) + " GROUP BY alarm.store_id) AS SumInterval";

                } else {
                    generatedQuery += "(SELECT DISTINCT nedap.alarm.store_id, COUNT(nedap.alarm.store_id) AS stolen_items FROM nedap.alarm, nedap.article WHERE nedap.article.id = nedap.alarm.article_id GROUP BY alarm.store_id) AS SumInterval";
                }
                break;
            case '5':

                generatedQuery += "(SELECT DATE_PART(" + variables.get(0) + ", timestamp) as timeinterval, store_id FROM alarm GROUP BY alarm.timestamp, store_id) AS timeinterval_table";
                break;
            case '6':
                generatedQuery += "SELECT day AS weekday, COUNT(day) FROM (SELECT trim(to_char(timestamp, 'day')) AS day FROM alarm) AS day_table ";
                break;
        }

        variables = getVariables(generationCode[3].substring(1));
        switch (generationCode[3].charAt(0)) {
            case '0':

                break;
            case '1':
                generatedQuery += "WHERE " + variables.get(0) + " " + variables.get(1) + " " + variables.get(2) + " ";
                break;
            case '2':
                generatedQuery += "WHERE " + variables.get(0) + " ";
                break;
            case '3':
                generatedQuery += "WHERE " + variables.get(0) + " " + variables.get(1) + " " + variables.get(2) + " ";
                if (variables.get(3) != "0") {
                    generatedQuery += "AND date(timestamp) >=" + variables.get(3) + " ";
                }
                if (variables.get(4) != "0") {
                    generatedQuery += "AND date(timestamp) <=" + variables.get(3) + " ";
                }
                break;
            case '4':
                generatedQuery += "WHERE " + variables.get(0) + " " + variables.get(1) + " " + variables.get(2) + " ";
                if (variables.get(3) != "0") {
                    generatedQuery += "AND date(timestamp) >= to_timestamp(" + variables.get(3) + ", 'dd-mm-yyyy hh24:mi:ss') ";
                }
                if (variables.get(4) != "0") {
                    generatedQuery += "AND date(timestamp) <= to_timestamp(" + variables.get(4) + ", 'dd-mm-yyyy hh24:mi:ss') ";
                }
                break;
            case '5':
                generatedQuery += "WHERE article.id = alarm.article_id ";
                if (!variables.get(0).equals("0")) {
                } else {
                    generatedQuery += "AND date(timestamp) = " + variables.get(0) + " ";
                }

                break;
        }

        variables = getVariables(generationCode[4].substring(1));
        switch (generationCode[4].charAt(0)) {
            case '0':
                break;
            case '1':
                generatedQuery += "GROUP BY nedap."+variables.get(0)+" ";
                break;

        }

        variables = getVariables(generationCode[5].substring(1));
        switch (generationCode[5].charAt(0)) {
            case '0':
                break;
            case '1':
                generatedQuery += "HAVING COUNT("+variables.get(0)+") "+variables.get(1)+" "+variables.get(2)+" ";
                break;

        }

        variables = getVariables(generationCode[6].substring(1));
        switch (generationCode[6].charAt(0)) {
            case '0':
                break;
            case '1':
                generatedQuery += "ORDER BY COUNT("+variables.get(0)+") DESC ";
                break;
        }

        variables = getVariables(generationCode[7].substring(1));
        switch (generationCode[7].charAt(0)) {
            case '0':
                break;
            case '1':
                generatedQuery+= "LIMIT "+variables.get(0)+" ";
                break;
        }

        return generatedQuery;
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
            // Check if query needs input for prepared statement.
            if (query.contains("?")) {
                String[] generationCodeArray = query.split(";");
                try {
                    generateSetStringInputs(generationCodeArray[1]);
                } catch (NullPointerException e) {
                    return "Prepared statement generation code missing";
                }


                //st.setString();
            }

            PreparedStatement st = connection.prepareStatement(query);


            ResultSet resultSet = st.executeQuery();
            ResultSetMetaData rsmd = resultSet.getMetaData();

            int columnsNumber = rsmd.getColumnCount();

            String result = "";
            // prints query results

            while (resultSet.next()) {
                for (int i = 1; i < columnsNumber + 1; i++) {

                    result += resultSet.getString(i) + ":";
                }
            }

            // A wise man once said that if you open a door you should also close it
            connection.close();
            return result;
        } catch (SQLException sqlE) {
            System.err.println("Error connecting: " + sqlE);
            return "sqlException";
        }
    }

    /**
     * Generates a JSON array of objects of the entire table, with each row being converted to a single object.
     * <p>
     * For example table:
     * <p>
     * create table t (a int, b text)
     * insert into t values (1, 'value1');
     * insert into t values (2, 'value2');
     * insert into t values (3, 'value3');
     * <p>
     * Result function
     * [{"a":1,"b":"value1"},{"a":2,"b":"value2"},{"a":3,"b":"value3"}]
     *
     * @param sheetType type of table to get data from
     * @return
     */
    public static JSONArray getTableJsonList(int sheetType) {
        fillRequiredLabels();
        String tableName = "";
        // Check the sheetType and assign corresponding name
        switch (sheetType) {
            case 0:
                tableName = "nedap.alarm ";
                break;
            case 1:
                tableName = "nedap.article ";
                break;
            case 2:
                tableName = "nedap.store ";
                break;
            default:
        }
        // Start of query which outputs a JSON array with JSON objects
        String query = "SELECT array_to_json(array_agg(t)) FROM " + tableName + " t;";

        // Connect to database
        Connection connection = getConnection();
        assert connection != null;

        // Remove colon from end
        StringBuffer sb = new StringBuffer(executeQuery(connection, query));
        sb.deleteCharAt(sb.length() - 1);

        JSONArray jsonarray = (JSONArray) new JSONTokener(sb.toString()).nextValue();

        return jsonarray;

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

            fillRequiredLabels();

            if (checkLabels(columnLabels, getRequiredLabels(0))) {
                parsePushToDB(sheet, getRequiredLabels(0), 0);
            } else if (checkLabels(columnLabels, getRequiredLabels(1))) {
                parsePushToDB(sheet, getRequiredLabels(1), 1);
            } else if (checkLabels(columnLabels, getRequiredLabels(2))) {
                parsePushToDB(sheet, getRequiredLabels(2), 2);
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

        if ((requiredFoundCount == requiredLabels.size()) & (requiredLabels.size() != 0)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Function which using the required columns produces and sends a query for putting in all the data from the sheet into the DB
     *
     * @param sheet          sheet to read from
     * @param requiredLabels required columns/labels
     * @param type           type of table to push to
     */
    public static void parsePushToDB(XSSFSheet sheet, ArrayList<String> requiredLabels, int type) {
        int row = 0;
        int column = 0;
        int requiredLabelIterator = 0;
        ArrayList<Integer> indexArray = new ArrayList<>();


        // Get's the indexes of the required labels.
        while (!(getCellData(sheet, row, column).equals("null") || getCellData(sheet, row, column).equals(""))) {
            fillRequiredLabels();
            ArrayList<String> columnLabel = new ArrayList<String>();
            columnLabel.add(getCellData(sheet, row, column));

            ArrayList<String> requiredLabel = new ArrayList<String>();

            try {
                requiredLabel.add(requiredLabels.get(requiredLabelIterator));
            } catch (IndexOutOfBoundsException ex) {

            }

            if (checkLabels(columnLabel, requiredLabel)) {
                indexArray.add(column);
                requiredLabelIterator++;
                column = 0;

            } else {
                column++;
            }


        }


        ArrayList<String> parsedSheetRowStrings = new ArrayList<>();
        String sheetRow = "";
        row = 1;
        column = indexArray.get(0); //start with primary key column

        // While a next row is not empty we put all rows into separate strings
        while (!getCellData(sheet, row, column).equals("null")) {
            for (Integer index : indexArray) {
                String cellContent = getCellData(sheet, row, index);

                if (cellContent.equals("")) {
                    cellContent = "NULL";
                    sheetRow += cellContent + ", ";
                } else {
                    cellContent = cellContent.replace("Store-", "");
                    cellContent = cellContent.replace("Article-", "");
                    cellContent = cellContent.replace("Category-", "");
                    cellContent = cellContent.replace("\'", "^");

                    sheetRow += "\'" + cellContent + "\'" + ", ";
                }

            }
            sheetRow = sheetRow.substring(0, sheetRow.length() - 2);
            parsedSheetRowStrings.add(sheetRow);
            sheetRow = "";
            row++;

        }

        String finalQuery = "";

        for (String parsedRow : parsedSheetRowStrings) {

            String insertQuery = "INSERT INTO ";
            switch (type) {
                case 0:
                    insertQuery += "nedap.alarm ";
                    break;
                case 1:
                    insertQuery += "nedap.article ";
                    break;
                case 2:
                    insertQuery += "nedap.store ";
                    break;
            }
            insertQuery += "VALUES (" + parsedRow + ");";

            finalQuery += insertQuery;

        }
        finalQuery = "SET datestyle = dmy;" + finalQuery;
        Connection connection = getConnection();
        assert connection != null;
        executeQuery(connection, finalQuery);
    }

}
