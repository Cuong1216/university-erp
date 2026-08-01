package com.wiz.universityerpapi.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import java.lang.reflect.Method;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.wiz.universityerpapi.core.config.RateLimitFilter;

class RateLimitFilterTest {

    private RateLimitFilter rateLimitFilter;
    private Method getClientIpMethod;

    @BeforeEach
    void setUp() throws Exception {
        rateLimitFilter = new RateLimitFilter(null, null); // Mocks not needed for getClientIp
        
        // Inject trusted proxy IPs via reflection
        java.lang.reflect.Field trustedProxyIpsField = RateLimitFilter.class.getDeclaredField("trustedProxyIps");
        trustedProxyIpsField.setAccessible(true);
        trustedProxyIpsField.set(rateLimitFilter, Arrays.asList("127.0.0.1", "10.0.0.0/8"));

        getClientIpMethod = RateLimitFilter.class.getDeclaredMethod("getClientIp", jakarta.servlet.http.HttpServletRequest.class);
        getClientIpMethod.setAccessible(true);
    }

    @Test
    void testGetClientIp_withTrustedProxy_usesXForwardedFor() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("10.0.0.5"); // Trusted proxy (in 10.0.0.0/8)
        request.addHeader("X-Forwarded-For", "203.0.113.1, 10.0.0.2");

        String clientIp = (String) getClientIpMethod.invoke(rateLimitFilter, request);
        assertEquals("203.0.113.1", clientIp);
    }

    @Test
    void testGetClientIp_withNonTrustedProxy_usesRemoteAddr() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("192.168.1.100"); // Not in trusted list
        request.addHeader("X-Forwarded-For", "203.0.113.1, 10.0.0.2");

        String clientIp = (String) getClientIpMethod.invoke(rateLimitFilter, request);
        assertEquals("192.168.1.100", clientIp);
    }
}
