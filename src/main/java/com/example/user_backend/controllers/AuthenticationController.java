package com.example.user_backend.controllers;


import com.example.user_backend.controllers.handlers.exceptions.InvalidPasswordException;
import com.example.user_backend.controllers.handlers.exceptions.ResourceNotFoundException;
import com.example.user_backend.dtos.UserDTO;
import com.example.user_backend.models.Role;
import com.example.user_backend.services.UserService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class AuthenticationController {

    @Autowired
    private UserService userService;

    @PostMapping("/login")
    @CrossOrigin("http://localhost:4201")
    public ResponseEntity<String> login(@RequestBody Map<String, String> credentials) {
        String username = credentials.get("username");
        String password = credentials.get("password");

        UserDTO user;
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            user = userService.getUserByLogin(username, password);
            return new ResponseEntity<>(objectMapper.writeValueAsString(user), HttpStatus.OK);
        } catch (ResourceNotFoundException | InvalidPasswordException e) {
            return new ResponseEntity<>(e.getResource(), e.getStatus());
        } catch (JsonProcessingException e) {
            return new ResponseEntity<>("", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody Map<String, String> credentials) {
        String username = credentials.get("username");
        String email = credentials.get("email");
        String password = credentials.get("password");

        UserDTO user = new UserDTO(Role.ROLE_USER, username, email, password);
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            Long userId = userService.insert(user);
            return new ResponseEntity<>(objectMapper.writeValueAsString(user), HttpStatus.OK);
        } catch (ResourceNotFoundException e) {
            return new ResponseEntity<>(e.getResource(), e.getStatus());
        } catch (JsonProcessingException e) {
            return new ResponseEntity<>("", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}

