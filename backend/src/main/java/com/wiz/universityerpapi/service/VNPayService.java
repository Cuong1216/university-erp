package com.wiz.universityerpapi.service;

import com.wiz.universityerpapi.config.VNPayConfig;
import com.wiz.universityerpapi.dto.payment.PaymentDTOs.*;
import com.wiz.universityerpapi.entity.HocPhi;
import com.wiz.universityerpapi.entity.ThanhToanLog;
import com.wiz.universityerpapi.exception.ResourceNotFoundException;
import com.wiz.universityerpapi.repository.HocPhiRepository;
import com.wiz.universityerpapi.repository.ThanhToanLogRepository;
import com.wiz.universityerpapi.util.VNPayUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class VNPayService {

    private final VNPayConfig vnpayConfig;
    private final HocPhiRepository hocPhiRepository;
    private final ThanhToanLogRepository thanhToanLogRepository;

    @Transactional(readOnly = true)
    public List<TuitionResponseDTO> getStudentTuitions(String maSv) {
        List<HocPhi> list = hocPhiRepository.findByMaSv(maSv);
        return list.stream().map(this::toTuitionResponseDTO).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<TuitionResponseDTO> getAllTuitions() {
        return hocPhiRepository.findAll().stream().map(this::toTuitionResponseDTO).collect(Collectors.toList());
    }

    private TuitionResponseDTO toTuitionResponseDTO(HocPhi h) {
        BigDecimal conLai = h.getSoTienPhaiNop().subtract(h.getSoTienDaNop() != null ? h.getSoTienDaNop() : BigDecimal.ZERO);
        if (conLai.compareTo(BigDecimal.ZERO) < 0) conLai = BigDecimal.ZERO;
        return TuitionResponseDTO.builder()
                .maHocPhi(h.getMaHocPhi())
                .maSv(h.getMaSv())
                .namHoc(h.getNamHoc())
                .hocKy(h.getHocKy())
                .soTienPhaiNop(h.getSoTienPhaiNop())
                .soTienDaNop(h.getSoTienDaNop() != null ? h.getSoTienDaNop() : BigDecimal.ZERO)
                .soTienConLai(conLai)
                .trangThai(h.getTrangThai())
                .hanNop(h.getHanNop())
                .build();
    }

    public CreatePaymentUrlResponseDTO createPaymentUrl(CreatePaymentUrlRequestDTO request) {
        HocPhi hocPhi = hocPhiRepository.findById(request.getMaHocPhi())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thông tin học phí: " + request.getMaHocPhi()));

        BigDecimal amountToPay = request.getAmount();
        if (amountToPay == null || amountToPay.compareTo(BigDecimal.ZERO) <= 0) {
            amountToPay = hocPhi.getSoTienPhaiNop().subtract(hocPhi.getSoTienDaNop() != null ? hocPhi.getSoTienDaNop() : BigDecimal.ZERO);
        }

        // VNPay số tiền tính bằng đơn vị VND * 100
        long amountVal = amountToPay.multiply(new BigDecimal(100)).longValue();
        String vnpTxnRef = hocPhi.getMaHocPhi() + "_" + System.currentTimeMillis();

        Map<String, String> vnpParams = new HashMap<>();
        vnpParams.put("vnp_Version", vnpayConfig.getVnpVersion());
        vnpParams.put("vnp_Command", vnpayConfig.getVnpCommand());
        vnpParams.put("vnp_TmnCode", vnpayConfig.getVnpTmnCode());
        vnpParams.put("vnp_Amount", String.valueOf(amountVal));
        vnpParams.put("vnp_CurrCode", "VND");
        vnpParams.put("vnp_TxnRef", vnpTxnRef);
        vnpParams.put("vnp_OrderInfo", "Thanh toan hoc phi " + hocPhi.getMaHocPhi() + " cho sinh vien " + hocPhi.getMaSv());
        vnpParams.put("vnp_OrderType", "tuition");
        vnpParams.put("vnp_Locale", "vn");

        String returnUrl = request.getReturnUrl() != null && !request.getReturnUrl().isEmpty()
                ? request.getReturnUrl() : vnpayConfig.getVnpReturnUrl();
        vnpParams.put("vnp_ReturnUrl", returnUrl);
        vnpParams.put("vnp_IpAddr", request.getIpAddress() != null ? request.getIpAddress() : "127.0.0.1");

        Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        String vnpCreateDate = formatter.format(cld.getTime());
        vnpParams.put("vnp_CreateDate", vnpCreateDate);

        cld.add(Calendar.MINUTE, 15); // Hết hạn thanh toán sau 15 phút
        String vnpExpireDate = formatter.format(cld.getTime());
        vnpParams.put("vnp_ExpireDate", vnpExpireDate);

        List<String> fieldNames = new ArrayList<>(vnpParams.keySet());
        Collections.sort(fieldNames);
        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();

        Iterator<String> itr = fieldNames.iterator();
        while (itr.hasNext()) {
            String fieldName = itr.next();
            String fieldValue = vnpParams.get(fieldName);
            if ((fieldValue != null) && (fieldValue.length() > 0)) {
                hashData.append(fieldName);
                hashData.append('=');
                hashData.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII));

                query.append(URLEncoder.encode(fieldName, StandardCharsets.US_ASCII));
                query.append('=');
                query.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII));

                if (itr.hasNext()) {
                    query.append('&');
                    hashData.append('&');
                }
            }
        }

        String queryUrl = query.toString();
        String vnpSecureHash = VNPayUtil.hmacSHA512(vnpayConfig.getVnpHashSecret(), hashData.toString());
        queryUrl += "&vnp_SecureHash=" + vnpSecureHash;
        String paymentUrl = vnpayConfig.getVnpPayUrl() + "?" + queryUrl;

        return CreatePaymentUrlResponseDTO.builder()
                .paymentUrl(paymentUrl)
                .vnpTxnRef(vnpTxnRef)
                .amount(amountToPay)
                .message("Tạo URL thanh toán VNPay thành công")
                .build();
    }

    /**
     * Xử lý Webhook callback (IPN - Instant Payment Notification) từ VNPay.
     * Bảo đảm an toàn tuyệt đối với 2 lớp phòng ngự:
     * 1. Transaction Isolation Level = SERIALIZABLE (chống race condition giữa 2 luồng xử lý đồng thời).
     * 2. Idempotency Check bằng cột vnp_txn_ref UNIQUE + catch DataIntegrityViolationException.
     */
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public VNPayWebhookResponseDTO processWebhookCallback(Map<String, String> params) {
        log.info("Nhận Webhook IPN từ VNPay: TxnRef={}", params.get("vnp_TxnRef"));

        // 1. Kiểm tra chữ ký bảo mật (HMAC SHA512 Checksum Verification)
        String vnpSecureHash = params.get("vnp_SecureHash");
        Map<String, String> fields = new HashMap<>(params);
        fields.remove("vnp_SecureHash");
        fields.remove("vnp_SecureHashType");

        String calculatedHash = VNPayUtil.hashAllFields(fields, vnpayConfig.getVnpHashSecret());
        if (!calculatedHash.equalsIgnoreCase(vnpSecureHash)) {
            log.warn("CẢNH BÁO: Chữ ký Webhook VNPay không hợp lệ! Expected={}, Actual={}", calculatedHash, vnpSecureHash);
            return new VNPayWebhookResponseDTO("97", "Invalid Checksum");
        }

        String vnpTxnRef = params.get("vnp_TxnRef");
        if (vnpTxnRef == null || !vnpTxnRef.contains("_")) {
            return new VNPayWebhookResponseDTO("99", "Invalid TxnRef format");
        }

        // 2. IDEMPOTENCY DEFENSE: Kiểm tra giao dịch đã được xử lý thành công trước đó hay chưa
        if (thanhToanLogRepository.existsByVnpTxnRef(vnpTxnRef)) {
            log.info("IDEMPOTENCY SKIP: Giao dịch TxnRef={} đã được ghi nhận trước đó. Bỏ qua xử lý lặp.", vnpTxnRef);
            return new VNPayWebhookResponseDTO("02", "Order already confirmed");
        }

        String maHocPhi = vnpTxnRef.substring(0, vnpTxnRef.lastIndexOf("_"));
        Optional<HocPhi> hocPhiOpt = hocPhiRepository.findById(maHocPhi);
        if (hocPhiOpt.isEmpty()) {
            log.error("Không tìm thấy mã học phí: {} từ TxnRef={}", maHocPhi, vnpTxnRef);
            return new VNPayWebhookResponseDTO("01", "Order not found");
        }

        HocPhi hocPhi = hocPhiOpt.get();
        BigDecimal vnpAmount = new BigDecimal(params.getOrDefault("vnp_Amount", "0")).divide(new BigDecimal(100));

        // 3. Ghi log giao dịch vào database với bẫy lỗi UNIQUE Constraint
        try {
            ThanhToanLog logEntry = ThanhToanLog.builder()
                    .maHocPhi(maHocPhi)
                    .vnpTxnRef(vnpTxnRef)
                    .vnpAmount(vnpAmount)
                    .vnpOrderInfo(params.get("vnp_OrderInfo"))
                    .vnpTransactionNo(params.get("vnp_TransactionNo"))
                    .vnpBankCode(params.get("vnp_BankCode"))
                    .vnpPayDate(params.get("vnp_PayDate"))
                    .vnpResponseCode(params.get("vnp_ResponseCode"))
                    .vnpTransactionStatus(params.get("vnp_TransactionStatus"))
                    .vnpSecureHash(vnpSecureHash)
                    .build();

            thanhToanLogRepository.saveAndFlush(logEntry);
        } catch (DataIntegrityViolationException ex) {
            log.warn("RACE CONDITION BLOCKED: Giao dịch TxnRef={} vừa được luồng khác ghi nhận. Trả về mã 02 Idempotent.", vnpTxnRef);
            return new VNPayWebhookResponseDTO("02", "Order already confirmed");
        }

        // 4. Cập nhật số tiền và trạng thái học phí nếu thanh toán thành công (RspCode == "00")
        String responseCode = params.get("vnp_ResponseCode");
        if ("00".equals(responseCode)) {
            BigDecimal currentPaid = hocPhi.getSoTienDaNop() != null ? hocPhi.getSoTienDaNop() : BigDecimal.ZERO;
            BigDecimal newPaid = currentPaid.add(vnpAmount);
            hocPhi.setSoTienDaNop(newPaid);

            if (newPaid.compareTo(hocPhi.getSoTienPhaiNop()) >= 0) {
                hocPhi.setTrangThai("DA_NOP_DU");
            } else {
                hocPhi.setTrangThai("NOP_MOT_PHAN");
            }
            hocPhiRepository.save(hocPhi);
            log.info("Cập nhật thành công học phí {} cho sinh viên {}. Số tiền thanh toán: {} VNĐ", 
                    maHocPhi, hocPhi.getMaSv(), vnpAmount);
        } else {
            log.info("Giao dịch TxnRef={} không thành công hoặc bị hủy. Mã phản hồi VNPay: {}", vnpTxnRef, responseCode);
        }

        return new VNPayWebhookResponseDTO("00", "Confirm Success");
    }
}
