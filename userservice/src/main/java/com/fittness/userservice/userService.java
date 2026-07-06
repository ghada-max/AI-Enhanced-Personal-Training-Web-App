package com.fittness.userservice;

import com.fittness.userservice.dto.UserResponse;
import com.fittness.userservice.dto.registerRequest;
import com.fittness.userservice.repo.Userrepo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class userService {
    @Autowired
    private Userrepo repo;

    public UserResponse getUserProfile(String userId) {
        return repo.findById(userId)
                .map(user -> {
                    UserResponse response = new UserResponse();
                    response.setId(user.getId());
                    response.setFirstname(user.getFirstname());
                    response.setLastname(user.getLastname());
                    response.setEmail(user.getEmail());
                    response.setUpdatedAt(user.getUpdatedAt());
                    response.setCreateAt(user.getCreateAt());

                    return response; // Don't forget to return the object out of the map block!
                })
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public UserResponse register(registerRequest request) {
        if (repo.existsByEmail(request.getEmail())){
            User existingUser= (User) repo.findByEmail(request.getEmail());
            UserResponse userResponse=new UserResponse();
            userResponse.setId(existingUser.getId());
            userResponse.setKeykloakId(existingUser.getKeycloakId());
            userResponse.setEmail(existingUser.getEmail());
            userResponse.setFirstname(existingUser.getFirstname());
            userResponse.setPassword(existingUser.getPassword());
            userResponse.setLastname(existingUser.getLastname());
            userResponse.setCreateAt(existingUser.getCreateAt());
            userResponse.setUpdatedAt(existingUser.getUpdatedAt());

        }


        User userRequest=new User();
        userRequest.setEmail(request.getEmail());
        userRequest.setPassword(request.getPassword());
        userRequest.setFirstname(request.getFirstname());
        userRequest.setLastname(request.getLastName());
        userRequest.setKeycloakId(request.getKeykloakId());
        repo.save(userRequest);

        UserResponse response=new UserResponse();
        response.setEmail(userRequest.getId());
        response.setKeykloakId(userRequest.getKeycloakId());

        response.setEmail(userRequest.getEmail());
        response.setFirstname(userRequest.getFirstname());
        response.setPassword(userRequest.getPassword());
        response.setLastname(userRequest.getLastname());
        response.setCreateAt(userRequest.getCreateAt());
        response.setUpdatedAt(userRequest.getUpdatedAt());


        return response;
    }

    public Boolean existByUserId(String userId) {
        log.info("calling user api validation for iserId:"+userId);
        return repo.existsByKeycloakId(userId);

    }
}
