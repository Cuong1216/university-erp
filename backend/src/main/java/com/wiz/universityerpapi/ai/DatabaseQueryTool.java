package com.wiz.universityerpapi.ai;

import dev.langchain4j.agent.tool.Tool;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
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

    private static final Pattern FORBIDDEN_KEYWORDS = Pattern.compile(
            "(?i)\\b(DROP|DELETE|UPDATE|INSERT|ALTER|TRUNCATE|EXEC|GRANT|REVOKE|CREATE|REPLACE|MERGE)\\b"
    );

    public DatabaseQueryTool(@Qualifier("aiReadOnlyJdbcTemplate") JdbcTemplate aiReadOnlyJdbcTemplate) {
        this.aiReadOnlyJdbcTemplate = aiReadOnlyJdbcTemplate;
    }

    @Tool("Thực thi câu lệnh SQL SELECT (đọc dữ liệu) trên cơ sở dữ liệu PostgreSQL để trả lời câu hỏi của người dùng. Chỉ nhận câu lệnh SELECT hợp lệ.")
    public String executeReadOnlyQuery(String sqlQuery) {
        log.info("AI Copilot yêu cầu thực thi SQL: {}", sqlQuery);

        if (sqlQuery == null || sqlQuery.trim().isEmpty()) {
            return "Lỗi: Câu lệnh SQL bị trống.";
        }

        String cleanedSql = sqlQuery.trim();
        if (cleanedSql.endsWith(";")) {
            cleanedSql = cleanedSql.substring(0, cleanedSql.length() - 1).trim();
        }

        // Lớp bảo vệ 1: Kiểm tra Application-level
        if (!cleanedSql.toUpperCase().startsWith("SELECT")) {
            log.warn("Từ chối truy vấn không bắt đầu bằng SELECT: {}", cleanedSql);
            return "Lỗi: Chỉ được phép thực thi câu lệnh SELECT (truy vấn chỉ đọc).";
        }

        if (FORBIDDEN_KEYWORDS.matcher(cleanedSql).find()) {
            log.warn("Phát hiện từ khóa cấm trong truy vấn AI: {}", cleanedSql);
            return "Lỗi bảo mật: Phát hiện từ khóa thay đổi cấu trúc/dữ liệu cấm trong câu lệnh.";
        }

        // Đảm bảo không truy vấn quá số lượng bản ghi gây nghẽn bộ nhớ
        if (!cleanedSql.toUpperCase().contains("LIMIT ")) {
            cleanedSql = cleanedSql + " LIMIT 50";
        }

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
}
