package com.example.lock.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

@RequiredArgsConstructor
public class LockRepository {
    private static final String GET_LOCK = "SELECT GET_LOCK(?, ?)";
    private static final String RELEASE_LOCK = "SELECT RELEASE_LOCK(?)";
    private static final String EXCEPTION_MESSAGE = "LOCK 을 수행하는 중에 오류가 발생하였습니다.";
    private static final int DEFAULT_TIMEOUT = 3000;
    private final DataSource dataSource;

    public <T> void executeWithLock(Long stockId, Long quantity, BiConsumer<Long, Long> consumer){
        try(Connection connection = dataSource.getConnection()){
            try{
                getLock(connection, String.valueOf(stockId));
                consumer.accept(stockId, quantity);
            } finally {
                releaseLock(connection, String.valueOf(stockId));
            }
        } catch (SQLException | RuntimeException e){
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    private void getLock(Connection connection, String stockId) throws SQLException {
        try(PreparedStatement preparedStatement = connection.prepareStatement(GET_LOCK)){
            preparedStatement.setString(1, stockId);
            preparedStatement.setInt(2, DEFAULT_TIMEOUT);

            checkResult(preparedStatement);
        }
    }

    private void releaseLock(Connection connection, String stockId) throws SQLException {
        try (PreparedStatement preparedStatement = connection.prepareStatement(RELEASE_LOCK)) {
            preparedStatement.setString(1, stockId);

            checkResult(preparedStatement);
        }
    }

    private void checkResult(PreparedStatement preparedStatement) throws SQLException {
        try(ResultSet resultSet = preparedStatement.executeQuery()){
            if(!resultSet.next()){
                throw new RuntimeException(EXCEPTION_MESSAGE);
            }
            int result = resultSet.getInt(1);
            if (result != 1){
                throw new RuntimeException(EXCEPTION_MESSAGE);
            }
        }
    }
}
