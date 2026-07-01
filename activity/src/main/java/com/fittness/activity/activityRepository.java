package com.fittness.activity;

import com.fittness.activity.Model.Activity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface activityRepository extends MongoRepository<Activity, String> {
    List<Activity> findByUserId(String userId);}
