package nl.utwente.m4.lossprevention.utils;

import java.sql.*;

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
                result+= resultSet.getString(1);
            }

            // A wise man once said that if you open a door you should also close it
            connection.close();
            return result;
        } catch (SQLException sqlE) {
            System.err.println("Error connecting: " + sqlE);
            return "sqlException";
        }
    }
}
