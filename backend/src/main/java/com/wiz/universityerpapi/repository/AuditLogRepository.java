package com.wiz.universityerpapi.repository;

import com.wiz.universityerpapi.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    List<AuditLog> findByUsernameOrderByCreatedAtDesc(String username);

    List<AuditLog> findByEntityNameAndEntityIdOrderByCreatedAtDesc(String entityName, String entityId);

    List<AuditLog> findByCreatedAtBetweenOrderByCreatedAtDesc(LocalDateTime fromDate, LocalDateTime toDate);
}
