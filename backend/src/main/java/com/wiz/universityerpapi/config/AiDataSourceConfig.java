package com.wiz.universityerpapi.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

/**
 * Cấu hình DataSource và JdbcTemplate chuyên dụng chỉ có quyền READ-ONLY
 * dành riêng cho AI Text-to-SQL execution (sử dụng user erp_ai_readonly_user).
 * Lớp bảo vệ vật lý ngăn chặn rủi ro LLM ảo giác sinh câu lệnh xóa/sửa dữ liệu.
 */
@Configuration
public class AiDataSourceConfig {

    @Value("${ai.datasource.url:jdbc:postgresql://localhost:5432/university_erp_db}")
    private String dbUrl;

    @Value("${ai.datasource.username:erp_ai_readonly_user}")
    private String dbUsername;

    @Value("${ai.datasource.password:secure_ai_db_password}")
    private String dbPassword;

    @Value("${ai.datasource.driver-class-name:org.postgresql.Driver}")
    private String driverClassName;

    @Bean(name = "aiReadOnlyDataSource")
    public DataSource aiReadOnlyDataSource() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(dbUrl);
        config.setUsername(dbUsername);
        config.setPassword(dbPassword);
        config.setDriverClassName(driverClassName);
        config.setPoolName("AI-ReadOnly-HikariPool");
        config.setMaximumPoolSize(5); // AI pool nhỏ gọn, đủ phục vụ truy vấn đồng thời
        config.setMinimumIdle(1);
        config.setConnectionTimeout(15000); // 15s timeout
        config.setReadOnly(true); // Enforce read-only ở HikariCP level
        return new HikariDataSource(config);
    }

    @Bean(name = "aiReadOnlyJdbcTemplate")
    public JdbcTemplate aiReadOnlyJdbcTemplate(@Qualifier("aiReadOnlyDataSource") DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }
}
