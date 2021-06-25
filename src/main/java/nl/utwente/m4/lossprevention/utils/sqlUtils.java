package nl.utwente.m4.lossprevention.utils;

import java.sql.*;

import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.util.ArrayList;
import java.util.Objects;


import static nl.utwente.m4.lossprevention.utils.excelUtils.*;

public class sqlUtils {
    

    /**
     * For testing purposes
     */
    public static void main(String[] args) {
        // System.out.println(XSSFSheet_to_DB(read("20210503_UTwente_Nedap_Stores.xlsx")));
        // System.out.println(XSSFSheet_to_DB(read("20210503_UTwente_Nedap_Articles.xlsx")));
        // System.out.println(XSSFSheet_to_DB(read("20210503_UTwente_Nedap_Alarms.xlsx")));

        String query = "SELECT array_to_json(array_agg(t)) FROM (?) AS t;0-3|stolen_items:\"Stolen Items Within Interval\":0|4|1:'2021#01#07':'2021#01#08'|-0-0-0-0-0-0";
        String realQuery = "1-2|store_id|-2|article:alarm|-1|article.id:=:alarm.article_id|-1|alarm.store_id|-0-1|alarm.store_id|-0";


        //System.out.println(generateSetStringInputs(realQuery));

        String[] generationCodeArray = query.split(";");

        System.out.println(generationCodeArray[1]);
        System.out.println(generateSetStringInputs(generationCodeArray[1]));

        query = generationCodeArray[0];
        query = query.replace("?", generateSetStringInputs(generationCodeArray[1]));
        System.out.println(query);
        Connection connection = getConnection();
        assert connection != null;
        System.out.println(executeQuery(connection, query));


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
        try {

            if ((variableString.charAt(0) == '|') && (variableString.charAt(variableString.length() - 1) == '|')) {

                int i = 1;
                int arrayIndex = 0;
                String variable = "";

                while (variableString.charAt(i) != '|') {
                    if (variableString.charAt(i) == ':') {

                        variableArrayList.add(arrayIndex, variable);
                        variable = "";
                        arrayIndex++;
                        i++;
                    } else {
                        variable += variableString.charAt(i);
                        i++;
                    }
                }
                variableArrayList.add(arrayIndex, variable);
            }
        } catch (StringIndexOutOfBoundsException e) {
            //no variables
        }


        return variableArrayList;
    }

    public static boolean checkVariableIsColumn(String variable) {
        switch (variable) {
            case "article.id":
            case "article.product":
            case "article.category":
            case "article.color":
            case "article.size":
            case "article.price":

            case "alarm.epc":
            case "alarm.store_id":
            case "alarm.article_id":
            case "alarm.timestamp":

            case "store.id":
            case "store.latitude":
            case "store.longitude":
                return true;
        }
        return false;
    }

    public static boolean checkVariableIsTable(String variable) {
        switch (variable) {
            case "article":
            case "alarm":
            case "store":
                return true;

        }
        return false;
    }

    public static boolean variablesValid(ArrayList<String> variables, int generationCodeInputType, int checkType) {
        switch (generationCodeInputType) {
            case 1:
                switch (checkType) {
                    case 1:
                        return checkVariableIsColumn(variables.get(0));

                    case 3:
                        if (variables.get(0).matches("^[a-zA-Z_ \"]*$") & variables.get(1).matches("^[a-zA-Z_ \"]*$")) {
                            return true;
                        }
                        break;
                    case 4:
                        if (checkVariableIsTable(variables.get(1))) {

                            if (checkVariableIsColumn(variables.get(0)) & variables.get(2).matches("^[a-zA-Z_ ]*$")) {
                                return true;
                            }

                        }

                        break;
                }
                return false;

            case 2:
                switch (checkType) {
                    case 1:
                        if (checkVariableIsTable(variables.get(0))) {
                            return true;
                        }
                        break;
                    case 2:
                        if (checkVariableIsTable(variables.get(0)) & checkVariableIsTable(variables.get(1))) {
                            return true;
                        }
                        break;
                    case 3:
                        if (checkVariableIsTable(variables.get(0)) & checkVariableIsTable(variables.get(1)) & checkVariableIsTable(variables.get(2))) {
                            return true;
                        }
                        break;
                    case 4:
                        if (!Objects.isNull(variables.get(1))) {
                            if (variables.get(1).charAt(0) == '\'' & variables.get(1).charAt(variables.get(1).length() - 1) == '\'') {
                                if (variables.get(1).matches("^[0-9 ' -]*$")) {

                                } else {
                                    return false;
                                }
                            } else {
                                return false;
                            }
                        }
                        if (!Objects.isNull(variables.get(2))) {
                            if (variables.get(2).charAt(0) == '\'' & variables.get(2).charAt(variables.get(2).length() - 1) == '\'') {
                                if (variables.get(1).matches("^[0-9 ' -]*$")) {
                                    return true;
                                } else {
                                    return false;
                                }
                            } else {
                                return false;
                            }
                        } else {
                            return true;
                        }

                    case 5:
                        switch (variables.get(0)) {
                            case "minute":
                            case "hour":
                            case "day":
                            case "month ":
                                return true;
                        }
                        break;

                }

                break;
            case 3:
                switch (checkType) {
                    case 1:
                        if ((checkVariableIsColumn(variables.get(0)) | variables.get(0).matches("^[-0-9]*$")) & (variables.get(1).matches("^[=<> ]*$")) & (checkVariableIsColumn(variables.get(2)) | variables.get(2).matches("^[-0-9]*$"))) {
                            return true;
                        }
                        break;
                    case 2:

                    case 3:
                        if ((checkVariableIsColumn(variables.get(0)) | variables.get(0).matches("^[-0-9]*$")) & (variables.get(1).matches("^[=<> ]*$")) & (checkVariableIsColumn(variables.get(2)) | variables.get(2).matches("^[-0-9]*$"))) {
                            if (variables.get(3).matches("^[0-9 ' _ #]*$") & variables.get(4).matches("^[0-9 ' _ #]*$")) {
                                return true;
                            }
                        }
                        break;
                    case 4:
                        if (variables.get(0).matches("^[0-9 ' _ #]*$")) {
                            return true;
                        }

                        break;
                    case 5:
                        if ((checkVariableIsColumn(variables.get(0)) | variables.get(0).matches("^[-0-9]*$")) & (variables.get(1).matches("^[=<> ]*$")) & (checkVariableIsColumn(variables.get(2)) | variables.get(2).matches("^[-0-9]*$")) & (variables.get(3).equals("AND") | variables.get(3).equals("OR")) & (checkVariableIsColumn(variables.get(4)) | variables.get(4).matches("^[-0-9]*$")) & (variables.get(5).matches("^[=<> ]*$")) & (checkVariableIsColumn(variables.get(6)) | variables.get(6).matches("^[-0-9]*$"))) {
                            return true;
                        }


                }
                break;
            case 4:
                if (checkType == 1) {
                    if (checkVariableIsColumn(variables.get(0)) | variables.get(0).equals("timeinterval")) {
                        return true;
                    }
                }
                break;
            case 5:
                if (checkType == 1) {
                    if (checkVariableIsColumn(variables.get(0)) & (variables.get(1).matches("^[=<> ]*$")) & (variables.get(2).matches("^[0-9 ]*$"))) {
                        return true;
                    }
                }
                break;
            case 6:
                if (checkType == 1) {
                    if (checkVariableIsColumn(variables.get(0))) {
                        return true;
                    }
                }
                break;
            case 7:
                if (checkType == 1) {
                    if (variables.get(0).matches("^[0-9 ]*$")) {
                        return true;
                    }
                }
                break;

        }
        return false;

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
                if (variablesValid(variables, 1, 1)) {
                    generatedQuery += "nedap." + variables.get(0) + ", COUNT(nedap." + variables.get(0) + ") ";
                } else {
                    return "Invalid variables! variables: " + variables;
                }

                break;

            case '2':
                generatedQuery += "nedap.store.id AS store_id, nedap.store.longitude, nedap.store.latitude ";
                break;
            case '3':
                if (variablesValid(variables, 1, 3)) {
                    if (variables.get(2).equals("0")) {
                        generatedQuery += "SUM(";
                    } else if (variables.get(2).equals("1")) {
                        generatedQuery += "COUNT(";
                    }
                    generatedQuery += variables.get(0) + ") AS " + variables.get(1) + " ";
                } else {
                    return "Invalid variables! variables: " + variables;
                }
                break;
            case '4':
                if (variablesValid(variables, 1, 4)) {
                    generatedQuery += "nedap." + variables.get(1) + "." + variables.get(0) + ", COUNT(" + variables.get(0) + ") AS " + variables.get(2) + " ";
                } else {
                    return "Invalid variables! variables: " + variables;
                }


                break;
            case '5':
                generatedQuery += "day AS weekday, COUNT(day) ";
            default:

        }

        generatedQuery += "FROM ";
        variables = getVariables(generationCode[2].substring(1));
        switch (generationCode[2].charAt(0)) {
            case '0':

                break;
            case '1':
                if (variablesValid(variables, 2, 1)) {
                    generatedQuery += "nedap." + variables.get(0) + " ";
                } else {
                    return "Invalid variables! variables: " + variables;
                }

                break;
            case '2':
                if (variablesValid(variables, 2, 2)) {
                    generatedQuery += "nedap." + variables.get(0) + ", nedap." + variables.get(1) + " ";
                } else {
                    return "Invalid variables! variables: " + variables;
                }

                break;
            case '3':
                if (variablesValid(variables, 2, 3)) {
                    generatedQuery += "nedap." + variables.get(0) + ", nedap." + variables.get(1) + " nedap." + variables.get(2) + " ";
                } else {
                    return "Invalid variables! variables: " + variables;
                }

                break;
            case '4':
                if (variablesValid(variables, 2, 4)) {
                    if (variables.get(0).equals("1")) {
                        generatedQuery += "(SELECT DISTINCT nedap.alarm.store_id, COUNT(nedap.alarm.store_id) AS stolen_items FROM nedap.alarm, nedap.article WHERE nedap.article.id = nedap.alarm.article_id AND date(timestamp) >= " + variables.get(1) + " AND date(timestamp) <= " + variables.get(2) + " GROUP BY alarm.store_id) AS SumInterval";

                    } else {
                        generatedQuery += "(SELECT DISTINCT nedap.alarm.store_id, COUNT(nedap.alarm.store_id) AS stolen_items FROM nedap.alarm, nedap.article WHERE nedap.article.id = nedap.alarm.article_id GROUP BY alarm.store_id) AS SumInterval";
                    }
                } else {
                    return "Invalid variables! variables: " + variables;
                }

                break;
            case '5':
                if (variablesValid(variables, 2, 5)) {
                    generatedQuery += "(SELECT DATE_PART(" + variables.get(0) + ", timestamp) as timeinterval, store_id FROM alarm GROUP BY alarm.timestamp, store_id) AS timeinterval_table";

                } else {
                    return "Invalid variables! variables: " + variables;
                }
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
                if (variablesValid(variables, 3, 1)) {
                    generatedQuery += "WHERE " + variables.get(0) + " " + variables.get(1) + " " + variables.get(2) + " ";
                } else {
                    return "Invalid variables! variables: " + variables;
                }


                break;

            case '2':
                if (variablesValid(variables, 3, 2)) {
                    generatedQuery += "WHERE " + variables.get(0) + " " + variables.get(1) + " " + variables.get(2) + " ";
                    if (!variables.get(3).equals("0")) {
                        String time = variables.get(3).replace('_',':');
                        time = time.replace('#','-');
                        generatedQuery += "AND date(timestamp) >=" + time + " ";
                    }
                    if (!variables.get(4).equals("0")) {
                        String time = variables.get(4).replace('_',':');
                        time = time.replace('#','-');
                        generatedQuery += "AND date(timestamp) <=" + time + " ";
                    }
                } else {
                    return "Invalid variables! variables: " + variables;
                }


                break;
            case '3':
                if (variablesValid(variables, 3, 3)) {
                    generatedQuery += "WHERE " + variables.get(0) + " " + variables.get(1) + " " + variables.get(2) + " ";
                    if (!variables.get(3).equals("0") ) {
                        String time = variables.get(3).replace('_',':');
                        time = time.replace('#','-');
                        generatedQuery += "AND alarm.timestamp >= to_timestamp(" + time + ", 'dd-mm-yyyy hh24:mi:ss') ";
                    }
                    if (!variables.get(4).equals("0") ) {
                        String time = variables.get(4).replace('_',':');
                        time = time.replace('#','-');
                        generatedQuery += "AND alarm.timestamp <= to_timestamp(" + time + ", 'dd-mm-yyyy hh24:mi:ss') ";
                    }
                } else {
                    return "Invalid variables! variables: " + variables;
                }


                break;
            case '4':
                if (variablesValid(variables, 3, 4)) {
                    generatedQuery += "WHERE article.id = alarm.article_id ";
                    if (!variables.get(0).equals("0")) {
                        String time = variables.get(0).replace('_',':');
                        time = time.replace('#','-');
                        generatedQuery += "AND date(timestamp) = " + time + " ";
                    } else {


                    }
                } else {
                    return "Invalid variables! variables: " + variables;
                }

                break;
            case '5':
                if (variablesValid(variables, 3, 5)) {
                    generatedQuery += "WHERE " + variables.get(0) + " " + variables.get(1) + " " + variables.get(2) + " " + variables.get(3) + " " + variables.get(4) + " " + variables.get(5) + " " + variables.get(6) + " ";

                } else {
                    return "Invalid variables! variables: " + variables;
                }
        }

        variables = getVariables(generationCode[4].substring(1));
        switch (generationCode[4].charAt(0)) {
            case '0':
                break;
            case '1':
                if (variablesValid(variables, 4, 1)) {
                    generatedQuery += "GROUP BY nedap." + variables.get(0) + " ";
                } else {
                    return "Invalid variables! variables: " + variables;
                }
                break;
        }

        variables = getVariables(generationCode[5].substring(1));
        switch (generationCode[5].charAt(0)) {
            case '0':
                break;
            case '1':
                if (variablesValid(variables, 5, 1)) {
                    generatedQuery += "HAVING COUNT(" + variables.get(0) + ") " + variables.get(1) + " " + variables.get(2) + " ";
                } else {
                    return "Invalid variables! variables: " + variables;
                }


                break;

        }

        variables = getVariables(generationCode[6].substring(1));
        switch (generationCode[6].charAt(0)) {
            case '0':
                break;
            case '1':
                if (variablesValid(variables, 6, 1)) {
                    generatedQuery += "ORDER BY COUNT(" + variables.get(0) + ") DESC ";

                } else {
                    return "Invalid variables! variables: " + variables;
                }

                break;
            case '2':

                generatedQuery += "ORDER BY BY CASE WHEN day = 'monday' THEN 1 WHEN day = 'tuesday' THEN 2 WHEN day = 'wednesday' THEN 3 WHEN day = 'thursday' THEN 4 WHEN day = 'friday' THEN 5 WHEN day = 'saturday' THEN 6 WHEN day = 'sunday' THEN 7 ";


                break;
        }

        variables = getVariables(generationCode[7].substring(1));
        switch (generationCode[7].charAt(0)) {
            case '0':
                break;
            case '1':
                if (variablesValid(variables, 7, 1)) {

                    generatedQuery += "LIMIT " + variables.get(0) + " ";
                } else {
                    return "Invalid variables! variables: " + variables;
                }


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
                    query = generationCodeArray[0];
                    query = query.replace("?", generateSetStringInputs(generationCodeArray[1]));

                    //   st.setString(1, generateSetStringInputs(generationCodeArray[1]));
                } catch (NullPointerException e) {
                    return "Prepared statement generation code missing";
                }


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
