package com.fittness.aiservice.listener;

import com.fittness.aiservice.ActivityAIService;
import com.fittness.aiservice.DTO.Activity;
import com.fittness.aiservice.recommendation;
import com.fittness.aiservice.repository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class ActivityMessageListener {
private final ActivityAIService aiService;
private final repository rep;
    @RabbitListener(queues = "activities.queue")
    public void receiveActivity(Activity activity) throws Exception {
            System.out.println("Received activity: " + activity.toString());
        log.info("Generated Recommendation:"+ aiService.generateRecommendation(activity));
        recommendation rec=aiService.generateRecommendation(activity);
    }
}