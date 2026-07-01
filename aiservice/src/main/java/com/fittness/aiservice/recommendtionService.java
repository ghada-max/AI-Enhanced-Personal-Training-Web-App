package com.fittness.aiservice;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Service
@AllArgsConstructor
@RequestMapping("/api/recommendations")
public class recommendtionService {
    private repository repo;

    public List<recommendation> getUserRecommendation(String userId) {

    return  repo.findByUserId(userId);

     //Us AI to generate recommendation
    }

    public recommendation getActivityRecommendation(String activityId) {

    return repo.findByActivityId(activityId).orElseThrow(()-> new RuntimeException(("No recommendation found")));

    }
}
