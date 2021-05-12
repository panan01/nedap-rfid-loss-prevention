package nl.utwente.m4.lossprevention.sql;

import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public final class DbConnector {
    static {
        try {
            Class.forName("org.postgresql.Driver");
        } catch(ClassNotFoundException cnfE) {
            System.err.println("Error loading SQL driver: " + cnfE);
            System.exit(-1);
        }
    }

    private DbConnector() {
    }

    public static Connection connect(String connectionFile) throws IOException, SQLException {
        Properties properties = new Properties();

        try(FileReader reader = new FileReader(connectionFile)) {
            properties.load(reader);
        }

        String host = properties.getProperty("host");
        String dbName = properties.getProperty("name");
        String schema = properties.getProperty("schema");
        String username = properties.getProperty("username");
        String password = properties.getProperty("password");

        if(host == null) throw new IOException("Missing database 'host'");
        if(dbName == null) throw new IOException("Missing database 'name'");
        if(schema == null) throw new IOException("Missing database 'schema'");
        if(username == null) throw new IOException("Missing database 'username'");
        if(password == null) throw new IOException("Missing database 'password'");

        String url = String.format("jdbc:postgresql://%s/%s?currentSchema=%s", host, dbName, schema);

        return DriverManager.getConnection(url, username, password);
    }
}
