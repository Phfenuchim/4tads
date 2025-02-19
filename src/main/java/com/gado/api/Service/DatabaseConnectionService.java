package com.gado.api.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

@Service
public class DatabaseConnectionService {

    private final DataSource dataSource;

    @Autowired
    public DatabaseConnectionService(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public Connection getConnection() throws SQLException {
        try {
            Connection connection = dataSource.getConnection();
            System.out.println("Conexão com o banco de dados estabelecida com sucesso");
            return connection;
        } catch (SQLException e) {
            System.out.println("Falha na conexão com o banco de dados: " + e.getMessage());
            throw e;
        }
    }

    public void closeConnection(Connection connection) {
        if (connection != null) {
            try {
                connection.close();
                System.out.println("Conexão com o banco de dados fechada com sucesso");
            } catch (SQLException e) {
                System.out.println("Falha ao fechar a conexão com o banco de dados: " + e.getMessage());
            }
        }
    }
}
