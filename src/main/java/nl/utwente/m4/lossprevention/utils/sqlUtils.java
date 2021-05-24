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
     * @return returns "Status-0" if successfully executed and "Status-1" if empty file was used
     */
    public static String XSSFSheet_to_DB(XSSFSheet sheet) {

        //Check file contents and the correct method for parsing
        ArrayList<String> columnLabels = getColumnLabels(sheet);
        if (!columnLabels.get(0).equals("Empty file")) {
            int fileType = -1;
            for (String label : columnLabels) {

                return "Status-0";
            }
            return "Status-2";
        } else {
            return "Status-1";
        }


    }
}
