package com.fittness.aiservice;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface repository extends MongoRepository<recommendation,String> {

   List<recommendation> findByUserId(String userId);
   Optional<recommendation> findByActivityId(String activityId);

   //see by all activitie the common userId and generate recommendations

}
