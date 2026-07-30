package com.wiz.universityerpapi.ai;

import com.wiz.universityerpapi.aop.LogAuditAction;
import dev.langchain4j.agent.tool.Tool;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Công cụ (Tool) dành cho AI LLM thực thi các câu truy vấn SELECT trên cơ sở dữ liệu.
 * Bảo vệ 2 lớp:
 * 1. App-level: Kiểm tra từ khóa nguy hiểm bằng Regex & buộc giới hạn LIMIT <= 50.
 * 2. DB-level: Thực thi qua aiReadOnlyJdbcTemplate (user erp_ai_readonly_user chỉ có quyền SELECT).
 */
@Slf4j
@Component
public class DatabaseQueryTool {

    private final JdbcTemplate aiReadOnlyJdbcTemplate;

    public DatabaseQueryTool(@Qualifier("aiReadOnlyJdbcTemplate") JdbcTemplate aiReadOnlyJdbcTemplate) {
        this.aiReadOnlyJdbcTemplate = aiReadOnlyJdbcTemplate;
    }

    @Tool("Thực thi câu lệnh SQL SELECT (đọc dữ liệu) trên cơ sở dữ liệu PostgreSQL để trả lời câu hỏi của người dùng. Chỉ nhận câu lệnh SELECT hợp lệ.")
    @LogAuditAction(actionType = "AI_QUERY", entityName = "AI")
    public String executeReadOnlyQuery(String sqlQuery) {
        if (sqlQuery == null || sqlQuery.trim().isEmpty()) {
            return "Lỗi: Câu lệnh SQL bị trống.";
        }

        String cleanedSql = sqlQuery.trim();
        if (cleanedSql.endsWith(";")) {
            cleanedSql = cleanedSql.substring(0, cleanedSql.length() - 1).trim();
        }

        try {
            cleanedSql = validateSqlSafety(cleanedSql);
        } catch (IllegalArgumentException e) {
            return "Trợ lý AI không thể thực thi câu lệnh này vì lý do bảo mật. Vui lòng đặt câu hỏi theo cách khác.";
        }

        log.info("[AI_COPILOT_QUERY] User='{}' SQL='{}'", getCurrentUsername(), cleanedSql);

        try {
            // Lớp bảo vệ 2: Thực thi qua DB user erp_ai_readonly_user
            List<Map<String, Object>> rows = aiReadOnlyJdbcTemplate.queryForList(cleanedSql);
            if (rows.isEmpty()) {
                return "Kết quả truy vấn: Không tìm thấy dữ liệu nào phù hợp với điều kiện.";
            }
            // Trả về JSON representation cho LLM
            return rows.toString();
        } catch (Exception e) {
            log.error("Lỗi khi thực thi SQL từ AI tool: {}", e.getMessage());
            return "Lỗi thực thi SQL: " + e.getMessage() + ". Hãy kiểm tra lại tên bảng và cột theo đúng schema được cung cấp.";
        }
    }

    private String validateSqlSafety(String sql) {
        String upper = sql.trim().toUpperCase();
        if (!upper.startsWith("SELECT")) {
            throw new IllegalArgumentException("Chỉ cho phép câu lệnh SELECT");
        }

        List<String> BLOCKED_KEYWORDS = List.of(
            "DROP", "DELETE", "UPDATE", "INSERT", "TRUNCATE", "ALTER", "CREATE",
            "EXEC", "EXECUTE", "CALL", "GRANT", "REVOKE",
            "--", "/*", "*/", "xp_", "pg_read_file", "COPY", "\\\\copy",
            "INTO OUTFILE", "INFORMATION_SCHEMA", "PG_SHADOW", "PG_AUTHID"
        );

        for (String keyword : BLOCKED_KEYWORDS) {
            if (upper.contains(keyword.toUpperCase())) {
                throw new IllegalArgumentException("Từ khóa không được phép");
            }
        }

        if (!upper.contains("LIMIT")) {
            sql = sql.stripTrailing() + " LIMIT 50";
            log.info("Auto-appended LIMIT 50 to AI query");
        }
        return sql;
    }

    private String getCurrentUsername() {
        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            return SecurityContextHolder.getContext().getAuthentication().getName();
        }
        return "system";
    }
}
