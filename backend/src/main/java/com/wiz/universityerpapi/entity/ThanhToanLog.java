package com.wiz.universityerpapi.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "thanh_toan_log")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ThanhToanLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ma_hoc_phi", length = 30, nullable = false)
    private String maHocPhi;

    @Column(name = "vnp_txn_ref", length = 100, nullable = false, unique = true)
    private String vnpTxnRef; // Khóa vật lý chống Webhook Idempotency / race condition

    @Column(name = "vnp_amount", precision = 15, scale = 2, nullable = false)
    private BigDecimal vnpAmount;

    @Column(name = "vnp_order_info", columnDefinition = "TEXT")
    private String vnpOrderInfo;

    @Column(name = "vnp_transaction_no", length = 100)
    private String vnpTransactionNo;

    @Column(name = "vnp_bank_code", length = 50)
    private String vnpBankCode;

    @Column(name = "vnp_pay_date", length = 20)
    private String vnpPayDate;

    @Column(name = "vnp_response_code", length = 10)
    private String vnpResponseCode;

    @Column(name = "vnp_transaction_status", length = 10)
    private String vnpTransactionStatus;

    @Column(name = "vnp_secure_hash", columnDefinition = "TEXT")
    private String vnpSecureHash;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;
}
