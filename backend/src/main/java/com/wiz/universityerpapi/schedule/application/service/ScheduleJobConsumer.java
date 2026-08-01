package com.wiz.universityerpapi.schedule.application.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wiz.universityerpapi.core.config.ActiveMqConfig;
import com.wiz.universityerpapi.schedule.application.dto.ScheduleOptimizationDTOs.ScheduleJobDTO;
import com.wiz.universityerpapi.schedule.application.dto.ScheduleOptimizationDTOs.ScheduleOptimizationResponseDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import com.wiz.universityerpapi.schedule.application.dto.ScheduleOptimizationDTOs.ScheduleJobDTO;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScheduleJobConsumer {

    private final ScheduleOptimizationService scheduleOptimizationService;
    private final RedisTemplate<String, Object> redisTemplate;
    private final SimpMessagingTemplate messagingTemplate;
    private final ObjectMapper objectMapper;

    @JmsListener(destination = ActiveMqConfig.SCHEDULE_OPTIMIZATION_REQUESTS_QUEUE)
    public void consumeScheduleOptimizationRequest(ScheduleJobDTO jobRequest) {
        log.info("Received schedule optimization job for requestId: {}", jobRequest.getRequestId());
        
        try {
            // 1. Run the synchronous solver engine
            ScheduleOptimizationResponseDTO response = scheduleOptimizationService.optimizeSchedule(jobRequest.getRequestPayload());
            
            // 2. Save result to Redis (TTL 1 hour)
            String redisKey = "schedule:result:" + jobRequest.getRequestId();
            String jsonResponse = objectMapper.writeValueAsString(response);
            redisTemplate.opsForValue().set(redisKey, jsonResponse, Duration.ofHours(1));
            log.info("Saved schedule optimization result to Redis for requestId: {}", jobRequest.getRequestId());
            
            // 3. (Optional) Send WebSocket notification
            messagingTemplate.convertAndSend("/topic/schedule/status/" + jobRequest.getRequestId(), response);
            
        } catch (Exception e) {
            log.error("Error processing schedule optimization job for requestId: {}", jobRequest.getRequestId(), e);
            
            // Optionally, save an error state in Redis
            ScheduleOptimizationResponseDTO errorResponse = ScheduleOptimizationResponseDTO.builder()
                .status("FAILED")
                .message("Đã xảy ra lỗi khi tự động xếp lịch: " + e.getMessage())
                .build();
            
            try {
                String redisKey = "schedule:result:" + jobRequest.getRequestId();
                String jsonResponse = objectMapper.writeValueAsString(errorResponse);
                redisTemplate.opsForValue().set(redisKey, jsonResponse, Duration.ofHours(1));
                
                messagingTemplate.convertAndSend("/topic/schedule/status/" + jobRequest.getRequestId(), errorResponse);
            } catch (JsonProcessingException ex) {
                log.error("Error serializing error response", ex);
            }
        }
    }
}
