package com.example.user_backend.controllers;

import com.example.user_backend.dtos.UserDTO;
import com.example.user_backend.dtos.builder.UserBuilder;
import com.example.user_backend.models.User;
import com.example.user_backend.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.sql.SQLOutput;
import java.util.List;

@RestController
@CrossOrigin
@RequestMapping(value = "/api/users")
public class UserController {

    private final UserService userService;
    private final RestTemplate restTemplate;

    @Autowired
    public UserController(UserService personService, RestTemplate restTemplate) {
        this.userService = personService;
        this.restTemplate = restTemplate;
    }

    @GetMapping()

    public ResponseEntity<List<User>> getUsers() {
        List<User> dtos = userService.findUsers();
        try {

            return new ResponseEntity<>(dtos, HttpStatus.OK);
        }
        catch(Exception exception) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping()
    public ResponseEntity<Long> addUser(@RequestBody UserDTO user) {
        ResponseEntity<Long> response = restTemplate.postForEntity(
                "http://spring-device:8081/api/device/users",
                user,
                Long.class);

        if (response.getStatusCode() == HttpStatus.CREATED) {
            Long userID = userService.insert(user);
            return new ResponseEntity<>(userID, HttpStatus.CREATED);
        } else {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long userId) {
        ResponseEntity<Void> response = restTemplate.exchange(
                "http://spring-device:8081/api/device/users/{userId}",
                HttpMethod.DELETE,
                null,
                Void.class,
                userId
        );
        System.out.println(response.getStatusCode());
        if (response.getStatusCode() == HttpStatus.NO_CONTENT) {
            userService.delete(userId);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } else {

            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

  /*  @DeleteMapping("/{userId}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long userId) {
        ResponseEntity<Void> response = restTemplate.exchange(
                "http://localhost:8081/api/device/users/{userId}",
                HttpMethod.DELETE,
                null,
                Void.class,
                userId
        );
        try {
            userService.delete(userId);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch(Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }*/
    @PutMapping("/{id}")
    public ResponseEntity<UserDTO> updateUser(@PathVariable Long id, @RequestBody UserDTO newUser) {
        UserDTO updatedUserData = UserBuilder.toUserDTO(userService.update(id, newUser));

        return new ResponseEntity<>(updatedUserData, HttpStatus.OK);
    }

    @GetMapping(value = "/{id}")
    public ResponseEntity<UserDTO> getUser(@PathVariable("id") Long userId) {
        System.out.println(userId);
        UserDTO dto = userService.findUserById(userId);
        return new ResponseEntity<>(dto, HttpStatus.OK);
    }
}

