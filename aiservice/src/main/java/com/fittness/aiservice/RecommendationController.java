package com.fittness.aiservice;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequestMapping("/recommendations")
@RestController
public class RecommendationController {
    @Autowired
    private  recommendtionService serv;
    @Autowired
    private ActivityAIService aiService;

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<recommendation>> getUserRecommendation(@PathVariable String userId )
    {
        return ResponseEntity.ok(serv.getUserRecommendation(userId));
    }


    @GetMapping("/activity/{activityId}")
    public ResponseEntity<recommendation> getActivityRecommendation(@PathVariable String activityId )
    {
        return ResponseEntity.ok(serv.getActivityRecommendation(activityId));
    }

}
