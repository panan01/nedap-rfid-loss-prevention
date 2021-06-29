package nl.utwente.m4.lossprevention.utils;

import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.json.JSONArray;
import org.json.JSONTokener;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

import static nl.utwente.m4.lossprevention.utils.excelUtils.*;

@Path("/app")
public class sqlUtils {


    /**
     * For testing purposes
     */
   // public static void main(String[] args) {


        //System.out.println(XSSFSheet_to_DB(read("20210503_UTwente_Nedap_Stores.xlsx")));
        // System.out.println(XSSFSheet_to_DB(read("20210503_UTwente_Nedap_Articles.xlsx")));
        //System.out.println(XSSFSheet_to_DB(read("20210503_UTwente_Nedap_Alarms.xlsx")));

        //String query = "SELECT array_to_json(array_agg(t)) FROM (?) AS t;0-6|store.id|-2|users:store|-7|test@test.com|-0-0-4|store.id|-0";
        //String realQuery = "2-7-0-0-0-0-0-0";


        //System.out.println(generateSetStringInputs(realQuery));

     /* String[] generationCodeArray = query.split(";");

        System.out.println(generationCodeArray[1]);
        System.out.println(generateSetStringInputs(generationCodeArray[1]));

        query = generationCodeArray[0];
        query = query.replace("?", generateSetStringInputs(generationCodeArray[1]));
        System.out.println(query);
        Connection connection = getConnection();
        assert connection != null;
        System.out.println(executeQuery(connection, query));*/


    //}

    @POST
    @Produces(MediaType.TEXT_PLAIN)
    public static String postMethod(String query) {
        Connection connection = getConnection();
        return executeQuery(connection, query);
    }

    //============================================== Database Utils  ===============================================\\
    private static final ArrayList<String> requiredLabelsType1 = new ArrayList<>(Arrays.asList(
            "EPC (UT)", "Timestamp", "Store ID (UT)", "Article ID (UT)"
    ));  // alarm type
    private static final ArrayList<String> requiredLabelsType2 = new ArrayList<>(Arrays.asList(
            "Article ID (UT)", "Category (UT)", "Article (UT)", "Color", "Size", "Price (EUR)"
    ));  // article type
    private static final ArrayList<String> requiredLabelsType3 = new ArrayList<>(Arrays.asList(
            "Store ID (UT)", "Latitude (UT)", "Longitude (UT)"
    ));  // store type

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

    /**
     * Extracts all variables from an |variable:variable| array which uses | as begin/end and : as separators
     * @param variableString string to extract variables from
     * @return if variables are present they will be returned in the arraylist
     */
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

    /**
     * Checks if a variable is a column in our database
     * @param variable variable to check
     * @return true if it's a (allowed) column in our database, false if not
     */
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

            case "store_access.store_id":
            case "store_access.allowed_user":

            case "users.email":
            case "users.last_name":
            case "users.first_name":
            case "users.type":


            case "timeinterval":
            case "day":
            case "*":
                return true;
        }
        return false;
    }

    /**
     * Checks if a variable is a table in our database
     * @param variable variable to check
     * @return true if it's a table in our database, false if not
     */
    public static boolean checkVariableIsTable(String variable) {
        switch (variable) {
            case "article":
            case "alarm":
            case "store":
            case "store_access":
            case "users":
                return true;

        }
        return false;
    }

    /**
     * Check if the variable doesn't have un-allowed symbols or characters
     * @param variables
     * @param generationCodeInputType to indicate which section to check variables for
     * @param checkType to indicate which subsection has to correct checks
     * @return returns true if variable is valid, otherwise false.
     */
    public static boolean variablesValid(ArrayList<String> variables, int generationCodeInputType, int checkType) {
        switch (generationCodeInputType) {
            case 1:
                switch (checkType) {
                    case 1:
                    case 6:
                        for (String variable : variables) {
                            if (!checkVariableIsColumn(variable)) {
                                return false;
                            }

                        }
                        return true;

                    case 3:
                        if (variables.get(0).matches("^[a-zA-Z_ \"]*$") & variables.get(1).matches("^[a-zA-Z_ \"]*$")) {
                            return true;
                        }
                        break;
                    case 4:
                        if (checkVariableIsColumn(variables.get(0)) & variables.get(1).matches("^[a-zA-Z_ ]*$")) {

                            return true;

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
                            if ((variables.get(1).charAt(0) == '\'' & variables.get(1).charAt(variables.get(1).length() - 1) == '\'') | (variables.get(1).charAt(0) == '0' & variables.get(2).charAt(0) == '0')) {
                                if (variables.get(1).matches("^[0-9 ' # _]*$")) {

                                } else {
                                    return false;
                                }
                            } else {
                                return false;
                            }
                        } else {
                            return false;
                        }

                        if (!Objects.isNull(variables.get(2))) {
                            if ((variables.get(2).charAt(0) == '\'' & variables.get(2).charAt(variables.get(2).length() - 1) == '\'') | (variables.get(1).charAt(0) == '0' & variables.get(2).charAt(0) == '0')) {
                                if (variables.get(1).matches("^[0-9 ' # _]*$")) {
                                    return true;
                                } else {
                                    return false;
                                }
                            } else {
                                return false;
                            }
                        } else {
                            return false;
                        }

                    case 5:
                        switch (variables.get(0)) {
                            case "'minute'":
                            case "'hour'":
                            case "'day'":
                            case "'month'":
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
                        break;
                    case 6:

                        for (String variable : variables) {
                            if (!variable.matches("^[0-9]*$")) {
                                return false;
                            }


                            return true;
                        }

                        break;
                    case 7:


                        if (variables.get(0).matches("^[a-zA-Z @ .]*$")) {
                            return true;
                        }

                        break;


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
                if (checkType == 1 | checkType == 4) {
                    if (checkVariableIsColumn(variables.get(0))) {
                        return true;
                    }
                }
                if (checkType == 3) {
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

    /**
     * This methods generates a query using a querybuilder code (called query) which is split on dashes
     * @param query generation coe
     * @return returns the functional query (if no incorrect/un-allowed inputs)
     */
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
            case '2':
                generatedQuery += "WITH jan AS (SELECT store.id, (SELECT COALESCE(SUM(1), 0) FROM nedap.alarm WHERE store.id = alarm.store_id AND alarm.timestamp >= to_timestamp('01-01-2021', 'dd-mm-yyyy') AND alarm.timestamp < to_timestamp('01-02-2021', 'dd-mm-yyyy')) AS amount\n" +
                        "    FROM nedap.store GROUP BY store.id ORDER BY store.id ), feb AS (SELECT store.id, (SELECT COALESCE(SUM(1), 0) FROM nedap.alarm WHERE store.id = alarm.store_id AND alarm.timestamp >= to_timestamp('01-02-2021', 'dd-mm-yyyy') AND alarm.timestamp < to_timestamp('01-03-2021', 'dd-mm-yyyy')) AS amount\n" +
                        "    FROM nedap.store GROUP BY store.id ORDER BY store.id ), mar AS (SELECT store.id, (SELECT COALESCE(SUM(1), 0) FROM nedap.alarm WHERE store.id = alarm.store_id AND alarm.timestamp >= to_timestamp('01-03-2021', 'dd-mm-yyyy') AND alarm.timestamp < to_timestamp('01-04-2021', 'dd-mm-yyyy')) AS amount\n" +
                        "    FROM nedap.store GROUP BY store.id  ORDER BY store.id  ) SELECT jan.id, jan.amount AS jan_amount, feb.amount AS feb_amount, mar.amount AS mar_amount FROM jan, feb, mar WHERE jan.id = feb.id AND feb.id = mar.id";
                break;
            case '3':
                generatedQuery += "WITH jan AS (SELECT article.category, COUNT(alarm.article_id) AS amount FROM nedap.article LEFT JOIN (SELECT * FROM nedap.alarm WHERE alarm.timestamp >= to_timestamp('01-01-2021', 'dd-mm-yyyy') AND alarm.timestamp < to_timestamp('01-02-2021', 'dd-mm-yyyy')) AS alarm ON article.id = alarm.article_id GROUP BY article.category ORDER BY article.category), feb AS (SELECT article.category, COUNT(alarm.article_id) AS amount FROM nedap.article LEFT JOIN (SELECT * FROM nedap.alarm WHERE alarm.timestamp >= to_timestamp('01-02-2021', 'dd-mm-yyyy') AND alarm.timestamp < to_timestamp('01-03-2021', 'dd-mm-yyyy')) AS alarm ON article.id = alarm.article_id GROUP BY article.category ORDER BY article.category), mar AS (SELECT article.category, COUNT(alarm.article_id) AS amount FROM nedap.article LEFT JOIN (SELECT * FROM nedap.alarm WHERE alarm.timestamp >= to_timestamp('01-03-2021', 'dd-mm-yyyy') AND alarm.timestamp < to_timestamp('01-04-2021', 'dd-mm-yyyy')) AS alarm ON article.id = alarm.article_id GROUP BY article.category ORDER BY article.category) SELECT jan.category, jan.amount AS jan_amount, feb.amount AS feb_amount, mar.amount AS mar_amount FROM jan, feb, mar WHERE jan.category = feb.category AND feb.category = mar.category";
                break;
            case '4':
                generatedQuery += "SELECT DISTINCT nedap.article.product, COUNT(nedap.article.product), article.price FROM nedap.article, alarm WHERE store_id = 1023553 AND article.id = alarm.article_id GROUP BY nedap.article.product, article.price ORDER BY COUNT(nedap.article.product) DESC";
                break;
            case '5':
                generatedQuery += "SELECT AVG(price) FROM article WHERE category = 1";
                break;
            case '6':
                generatedQuery += "SELECT day AS weekday, COUNT(day) FROM (SELECT trim(to_char(timestamp, 'day')) AS day, store_id FROM nedap.alarm) AS day_table GROUP BY day ORDER BY CASE WHEN day = 'monday' THEN 1 WHEN day = 'tuesday' THEN 2 WHEN day = 'wednesday' THEN 3 WHEN day = 'thursday' THEN 4 WHEN day = 'friday' THEN 5 WHEN day = 'saturday' THEN 6 WHEN day = 'sunday' THEN 7 END ASC";
            default:

        }
        ArrayList<String> variables = getVariables(generationCode[1].substring(1));
        switch (generationCode[1].charAt(0)) {
            case '0':
                generatedQuery += "* ";
                break;
            case '1':
                if (variablesValid(variables, 1, 1)) {
                    if (variables.get(0).equals("timeinterval") | variables.get(0).equals("*") | variables.get(0).equals("day")) {
                        if (variables.get(0).equals("*")) {
                            generatedQuery += "" + variables.get(0) + " ";
                        } else {
                            generatedQuery += "" + variables.get(0) + ", COUNT(" + variables.get(0) + ") ";
                        }

                    } else {
                        generatedQuery += "nedap." + variables.get(0) + ", COUNT(nedap." + variables.get(0) + ") ";
                    }

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
                    generatedQuery += "nedap." + variables.get(0) + ", COUNT(" + variables.get(0) + ") AS " + variables.get(1) + " ";
                } else {
                    return "Invalid variables! variables: " + variables;
                }


                break;
            case '5':
                generatedQuery += "day AS weekday, COUNT(day) ";

            case '6':
                if (variablesValid(variables, 1, 6)) {
                    for (String variable : variables) {
                        generatedQuery += "nedap." + variable + ", ";
                    }
                    generatedQuery = generatedQuery.substring(0, generatedQuery.length() - 2);
                    generatedQuery += " ";


                } else {
                    return "Invalid variables! variables: " + variables;
                }

                break;
            case '7':

                break;

        }


        variables = getVariables(generationCode[2].substring(1));
        switch (generationCode[2].charAt(0)) {
            case '0':

                break;
            case '1':
                generatedQuery += "FROM ";
                if (variablesValid(variables, 2, 1)) {
                    generatedQuery += "nedap." + variables.get(0) + " ";
                } else {
                    return "Invalid variables! variables: " + variables;
                }

                break;
            case '2':
                generatedQuery += "FROM ";
                if (variablesValid(variables, 2, 2)) {
                    generatedQuery += "nedap." + variables.get(0) + ", nedap." + variables.get(1) + " ";
                } else {
                    return "Invalid variables! variables: " + variables;
                }

                break;
            case '3':
                generatedQuery += "FROM ";
                if (variablesValid(variables, 2, 3)) {
                    generatedQuery += "nedap." + variables.get(0) + ", nedap." + variables.get(1) + " nedap." + variables.get(2) + " ";
                } else {
                    return "Invalid variables! variables: " + variables;
                }

                break;
            case '4':
                generatedQuery += "FROM ";
                if (variablesValid(variables, 2, 4)) {
                    if (variables.get(0).equals("1")) {


                        String time = variables.get(1).replace('_', ':');
                        time = time.replace('#', '-');


                        String time2 = variables.get(2).replace('_', ':');
                        time2 = time2.replace('#', '-');


                        generatedQuery += "(SELECT DISTINCT nedap.alarm.store_id, COUNT(nedap.alarm.store_id) AS stolen_items FROM nedap.alarm, nedap.article WHERE nedap.article.id = nedap.alarm.article_id AND date(timestamp) >= " + time + " AND date(timestamp) <= " + time2 + " GROUP BY alarm.store_id) AS SumInterval";

                    } else {
                        generatedQuery += "(SELECT DISTINCT nedap.alarm.store_id, COUNT(nedap.alarm.store_id) AS stolen_items FROM nedap.alarm, nedap.article WHERE nedap.article.id = nedap.alarm.article_id GROUP BY alarm.store_id) AS SumInterval";
                    }
                } else {
                    return "Invalid variables! variables: " + variables;
                }

                break;
            case '5':
                generatedQuery += "FROM ";
                if (variablesValid(variables, 2, 5)) {
                    generatedQuery += "(SELECT DATE_PART(" + variables.get(0) + ", timestamp) as timeinterval, store_id FROM nedap.alarm GROUP BY alarm.timestamp, store_id) AS timeinterval_table ";

                } else {
                    return "Invalid variables! variables: " + variables;
                }
                break;
            case '6':
                generatedQuery += "FROM ";
                generatedQuery += "(SELECT trim(to_char(timestamp, 'day')) AS day, store_id FROM nedap.alarm) AS day_table ";
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
                        String time = variables.get(3).replace('_', ':');
                        time = time.replace('#', '-');
                        generatedQuery += "AND date(timestamp) >=" + time + " ";
                    }
                    if (!variables.get(4).equals("0")) {
                        String time = variables.get(4).replace('_', ':');
                        time = time.replace('#', '-');
                        generatedQuery += "AND date(timestamp) <=" + time + " ";
                    }
                } else {
                    return "Invalid variables! variables: " + variables;
                }


                break;
            case '3':
                if (variablesValid(variables, 3, 3)) {
                    generatedQuery += "WHERE " + variables.get(0) + " " + variables.get(1) + " " + variables.get(2) + " ";
                    if (!variables.get(3).equals("0")) {
                        String time = variables.get(3).replace('_', ':');
                        time = time.replace('#', '-');
                        generatedQuery += "AND alarm.timestamp >= to_timestamp(" + time + ", 'dd-mm-yyyy hh24:mi:ss') ";
                    }
                    if (!variables.get(4).equals("0")) {
                        String time = variables.get(4).replace('_', ':');
                        time = time.replace('#', '-');
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
                        String time = variables.get(0).replace('_', ':');
                        time = time.replace('#', '-');
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
                break;
            case '6':
                if (variablesValid(variables, 3, 6)) {
                    generatedQuery += "WHERE store_id = " + variables.get(0);
                   /* for (String variable : variables) {
                        generatedQuery += variable + " OR ";
                    }*/

                    /*generatedQuery = generatedQuery.substring(0, generatedQuery.length() - 3);*/
                } else {
                    return "Invalid variables! variables: " + variables;
                }


                break;
            case '7':

                if (variablesValid(variables, 3, 7)) {
                    generatedQuery += "WHERE users.email = '" + variables.get(0) + "' AND ((SELECT store_access.allowed_user FROM nedap.store_access WHERE nedap.store.id = nedap.store_access.store_id) = '" + variables.get(0) + "' OR users.type = 'admin') ";

                } else {
                    return "Invalid variables! variables: " + variables;
                }
                break;
        }

        variables = getVariables(generationCode[4].substring(1));
        switch (generationCode[4].charAt(0)) {
            case '0':
                break;
            case '1':
                if (variablesValid(variables, 4, 1)) {

                    if (variables.get(0).equals("timeinterval") | variables.get(0).equals("*") | variables.get(0).equals("day")) {
                        generatedQuery += "GROUP BY " + variables.get(0) + " ";
                    } else {
                        generatedQuery += "GROUP BY nedap." + variables.get(0) + " ";
                    }


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
                    if (variables.get(0).equals("timeinterval") | variables.get(0).equals("*") | variables.get(0).equals("day")) {
                        generatedQuery += "ORDER BY " + variables.get(0) + " ASC ";
                    } else {
                        generatedQuery += "ORDER BY COUNT(" + variables.get(0) + ") DESC ";
                    }


                } else {
                    return "Invalid variables! variables: " + variables;
                }

                break;
            case '2':

                generatedQuery += "ORDER BY CASE WHEN day = 'monday' THEN 1 WHEN day = 'tuesday' THEN 2 WHEN day = 'wednesday' THEN 3 WHEN day = 'thursday' THEN 4 WHEN day = 'friday' THEN 5 WHEN day = 'saturday' THEN 6 WHEN day = 'sunday' THEN 7 END ASC ";


                break;
            case '3':
                if (variablesValid(variables, 6, 3)) {

                    generatedQuery += "ORDER BY COUNT(" + variables.get(0) + ") ASC ";


                } else {
                    return "Invalid variables! variables: " + variables;
                }
                break;
            case '4':
                if (variablesValid(variables, 6, 4)) {

                    generatedQuery += "ORDER BY " + variables.get(0) + " ";


                } else {
                    return "Invalid variables! variables: " + variables;
                }
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

            StringBuilder result = new StringBuilder();
            // prints query results

            while (resultSet.next()) {
                for (int i = 1; i < columnsNumber + 1; i++) {
                    result.append(resultSet.getString(i));
                    if (i != columnsNumber) {
                        result.append(":");
                    }
                }
            }

            // A wise man once said that if you open a door you should also close it
            connection.close();
            return result.toString();
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

        // turn result into JSON
        String result = executeQuery(connection, query);
        JSONArray jsonarray = (JSONArray) new JSONTokener(result).nextValue();

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
                if (cellContent.toUpperCase().contains("SELECT") || cellContent.toUpperCase().contains("FROM") || cellContent.toUpperCase().contains("DELETE") || cellContent.toUpperCase().contains("FROM") || cellContent.toUpperCase().contains("UPDATE") || cellContent.toUpperCase().contains("INSERT") || cellContent.toUpperCase().contains("CREATE") || cellContent.toUpperCase().contains("ALTER") || cellContent.toUpperCase().contains("DROP") || cellContent.toUpperCase().contains("TABLE") || cellContent.toUpperCase().contains("INDEX")) {
                } else {
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
