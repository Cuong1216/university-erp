package com.wiz.universityerpapi.tenant;

import lombok.extern.slf4j.Slf4j;
import org.hibernate.engine.jdbc.connections.spi.MultiTenantConnectionProvider;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

@Slf4j
@Component
public class MultiTenantConnectionProviderImpl implements MultiTenantConnectionProvider<String> {

    private final DataSource dataSource;

    public MultiTenantConnectionProviderImpl(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Connection getAnyConnection() throws SQLException {
        return dataSource.getConnection();
    }

    @Override
    public void releaseAnyConnection(Connection connection) throws SQLException {
        connection.close();
    }

    @Override
    public Connection getConnection(String tenantIdentifier) throws SQLException {
        final Connection connection = getAnyConnection();
        try (Statement statement = connection.createStatement()) {
            // Thiết lập Schema search_path cho Tenant hiện tại
            // TenantIdentifier đã được validate Regex an toàn trong Filter
            statement.execute("SET search_path TO " + tenantIdentifier + ", public");
        } catch (SQLException e) {
            throw new SQLException("Không thể chuyển đổi sang Schema của Tenant: " + tenantIdentifier, e);
        }
        return connection;
    }

    @Override
    public void releaseConnection(String tenantIdentifier, Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            // Quan trọng: Trả search_path về mặc định trước khi trả connection về HikariCP Pool
            // Nếu không, connection dùng chung sẽ bị rò rỉ dữ liệu (Data Leak) sang tenant khác!
            statement.execute("SET search_path TO public");
        } catch (SQLException e) {
            log.error("Lỗi khi reset Schema search_path về public", e);
        }
        connection.close();
    }

    @Override
    public boolean supportsAggressiveRelease() {
        return false;
    }

    @Override
    public boolean isUnwrappableAs(Class<?> unwrapType) {
        return false;
    }

    @Override
    public <T> T unwrap(Class<T> unwrapType) {
        return null;
    }
}
