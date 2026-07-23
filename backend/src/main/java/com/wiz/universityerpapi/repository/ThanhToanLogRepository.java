package com.wiz.universityerpapi.repository;

import com.wiz.universityerpapi.entity.ThanhToanLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ThanhToanLogRepository extends JpaRepository<ThanhToanLog, Long> {
    Optional<ThanhToanLog> findByVnpTxnRef(String vnpTxnRef);
    boolean existsByVnpTxnRef(String vnpTxnRef);
}
