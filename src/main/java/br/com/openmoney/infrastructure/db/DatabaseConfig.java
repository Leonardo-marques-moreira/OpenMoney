package br.com.openmoney.infrastructure.db;

import br.com.openmoney.infrastructure.exception.DatabaseException;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;


public class DatabaseConfig {

    private static final String PROPERTIES_FILE = "db.properties";

    private static String url;
    private static String user;
    private static String password;

    static {
        try (InputStream in = DatabaseConfig.class
                .getClassLoader()
                .getResourceAsStream(PROPERTIES_FILE)) {

            if (in == null) {
                throw new DatabaseException(
                        "Arquivo " + PROPERTIES_FILE + " nao encontrado no classpath.");
            }

            Properties props = new Properties();
            props.load(in);

            url = props.getProperty("db.url");
            user = props.getProperty("db.user");
            password = props.getProperty("db.password");

            Class.forName("org.postgresql.Driver");

        } catch (IOException | ClassNotFoundException e) {
            throw new DatabaseException("Falha ao inicializar DatabaseConfig: " + e.getMessage(), e);
        }
    }

 
    public static Connection getConnection() {
        try {
            return DriverManager.getConnection(url, user, password);
        } catch (SQLException e) {
            throw new DatabaseException(
                    "Nao foi possivel conectar ao banco de dados: " + e.getMessage(), e);
        }
    }

   
    public static Connection getTransactionalConnection() {
        try {
            Connection conn = DriverManager.getConnection(url, user, password);
            conn.setAutoCommit(false);
            return conn;
        } catch (SQLException e) {
            throw new DatabaseException(
                    "Nao foi possivel abrir conexao transacional: " + e.getMessage(), e);
        }
    }

    private DatabaseConfig() {}
}
