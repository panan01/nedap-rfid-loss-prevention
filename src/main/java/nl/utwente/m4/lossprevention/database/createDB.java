package nl.utwente.m4.lossprevention.database;

import java.sql.*;

public class createDB {

    public static void driverLoader() {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException cnfE) {
            System.err.println("Error loading driver: " + cnfE);
        }
    }

    public static void getConnection() {
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

            // SQL query, note that it does weird stuff if no tables are in phpmyadmin
            String query = "CREATE TABLE nedap.Store (id INT PRIMARY KEY CHECK (id >= 0),latitude INT,longitude INT); " +
                    "CREATE TABLE nedap.Alarm ( \n" +
                    "  epc VARCHAR(80)PRIMARY KEY,\n" +
                    "  timestamp TIMESTAMP,\n" +
                    "  store_id INT, CONSTRAINT fk_store_id\n" +
                    "        FOREIGN KEY(store_id) \n" +
                    "            REFERENCES nedap.Store(id),\n" +
                    "  article_id BIGINT, \n" +
                    "  CONSTRAINT fk_article_id \n" +
                    "        FOREIGN KEY(article_id) \n" +
                    "            REFERENCES nedap.Article(id) );\n" +
                    "CREATE TABLE nedap.Article (\n" +
                    "    id BIGINT PRIMARY KEY CHECK (id > 0),\n" +
                    "    category INT CHECK (category > 0),\n" +
                    "    product INT CHECK (product > 0),\n" +
                    "    color VARCHAR(80),\n" +
                    "    size VARCHAR(80),\n" +
                    "    price DECIMAL(7, 2) CHECK (price >= 0)\n" +
                    ");";

            PreparedStatement st =
                    connection.prepareStatement(query);


            // Check if query needs input for prepared statement.
            if (query.contains("?")) {
                //st.setString();
            }

            ResultSet resultSet =
                    st.executeQuery();

            // prints query results
            while (resultSet.next()) {
                System.out.println(resultSet.getString(1));
            }

            connection.close();


        } catch (SQLException sqlE) {
            System.err.println("Error connecting: " + sqlE);
        }
    }

    public static void main(String[] args) {
        driverLoader();
        getConnection();
    }
}
