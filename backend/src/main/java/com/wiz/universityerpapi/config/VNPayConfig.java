package com.wiz.universityerpapi.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Getter
public class VNPayConfig {

    @Value("${vnpay.tmn-code:ERPSaaS01}")
    private String vnpTmnCode;

    @Value("${vnpay.hash-secret:VNPAY_ERP_SECRET_HASH_KEY_2024_WIZ_9876543210}")
    private String vnpHashSecret;

    @Value("${vnpay.pay-url:https://sandbox.vnpayment.vn/paymentv2/vpcpay.html}")
    private String vnpPayUrl;

    @Value("${vnpay.return-url:http://localhost:5173/tuition/result}")
    private String vnpReturnUrl;

    @Value("${vnpay.api-version:2.1.0}")
    private String vnpVersion;

    @Value("${vnpay.command:pay}")
    private String vnpCommand;
}
