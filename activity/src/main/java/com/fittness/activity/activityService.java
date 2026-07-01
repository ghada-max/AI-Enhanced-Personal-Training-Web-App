package com.fittness.activity;

import com.fittness.activity.Model.ActivityResponse;
import com.fittness.activity.Model.Activity;
import com.fittness.activity.Model.activityRequest;
import com.fittness.activity.user.UserValidation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class activityService {

    private final UserValidation uservalidation;
    private final activityRepository repo;
    private final RabbitTemplate rabbitTemplate;

    @Value("${rabbitmq.exchange.name}")
    private String exchange;

    @Value("${rabbitmq.routing.key}")
    private String routing;


    public ActivityResponse trackActivity(activityRequest request){
        boolean isValid=uservalidation.validateUser(request.getUserId());
        if (!isValid) {
            throw new RuntimeException("Invalid user");
        }           Activity actvty = Activity.builder().
                   userId(request.getUserId()).caloriesBurned(request.getCaloriesBurned())
                   .type(request.getType())
                   .startTime(request.getStartTime())
                   .additionalMetrics(request.getAdditonalMetrics())
                   .build();

        System.out.println("Exists? " + repo.findAll());
    Activity savedActivity=repo.save(actvty);
    //publish to the RabbiMQ queue for AI processing
        //producer

        try{
            rabbitTemplate.convertAndSend(exchange, routing, actvty);
             log.info("rabbit service done");
        }catch(Exception e){
           log.error("failed to log activity");
        }

        return ActivityResponse.builder()
                .id(savedActivity.getId())
                .userId(savedActivity.getUserId())
                .type(savedActivity.getType())
                .duration(savedActivity.getDuration())
                .caloriesBurned(savedActivity.getCaloriesBurned())
                .startTime(savedActivity.getStartTime())
                .additionalMetrics(savedActivity.getAdditionalMetrics())
                .createdAt(savedActivity.getCreatedAt())
                .updatedAt(savedActivity.getUpdatedAt())
                .build();

    }

    public List<ActivityResponse> getUserActivities(String userId) {
        return repo.findByUserId(userId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private ActivityResponse mapToResponse(Activity savedActivity) {
        return ActivityResponse.builder()
                .id(savedActivity.getId())
                .userId(savedActivity.getUserId())
                .type(savedActivity.getType())
                .duration(savedActivity.getDuration())
                .caloriesBurned(savedActivity.getCaloriesBurned())
                .startTime(savedActivity.getStartTime())
                .additionalMetrics(savedActivity.getAdditionalMetrics())
                .createdAt(savedActivity.getCreatedAt())
                .updatedAt(savedActivity.getUpdatedAt())
                .build();
    }
}
