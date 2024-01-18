package com.example.user_backend.services;

import com.example.user_backend.controllers.handlers.exceptions.InvalidPasswordException;
import com.example.user_backend.controllers.handlers.exceptions.ResourceNotFoundException;
import com.example.user_backend.dtos.UserDTO;
import com.example.user_backend.dtos.builder.UserBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.example.user_backend.models.User;
import com.example.user_backend.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService implements UserDetailsService {
    private static final Logger LOGGER = LoggerFactory.getLogger(UserService.class);
    private final UserRepository userRepository;

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<User> findUsers() {
        return userRepository.findAll();
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findUserByEmail(username).orElseThrow(() -> new UsernameNotFoundException("Not found"));
    }


    public UserDTO findUserById(Long id) {
        Optional<User> userOptional = userRepository.findById(id);

        if (!userOptional.isPresent()) {
            LOGGER.error("User with id {} was not found in the database", id);
            throw new ResourceNotFoundException("User with ID " + id + " does not exist");
        }

        return UserBuilder.toUserDTO(userOptional.get());
    }

    public User getUserByLogin(String username, String password) {
        Optional<User> userOptional = userRepository.findUserByUsername(username);

        if (!userOptional.isPresent()) {
            throw new ResourceNotFoundException("User with username: " + username + " not found");
        }

        User user = userOptional.get();
        if (user.getPassword().equals(password)) {
            return user;
        } else {
            throw new InvalidPasswordException("Invalid password for user with username: " + username);
        }
    }

    public Long insert(UserDTO userDTO) {
        Optional<User> existingUser = userRepository.findUserByUsername(userDTO.getUsername());
        if (existingUser.isPresent()) {
            throw new ResourceNotFoundException("User with username '" + userDTO.getUsername() + "' already exists");
        }

        existingUser = userRepository.findUserByEmail(userDTO.getEmail());
        if (existingUser.isPresent()) {
            throw new ResourceNotFoundException("User with email '" + userDTO.getEmail() + "' already exists");
        }

        User user = UserBuilder.toEntity(userDTO);
        user = userRepository.save(user);
        LOGGER.debug("User with id {} was inserted into the database", user.getUserId());
        return user.getUserId();
    }

    public User update(Long userId, UserDTO newUser) {
        Optional<User> userOptional = userRepository.findById(userId);

        if (!userOptional.isPresent()) {
            throw new ResourceNotFoundException("User with ID " + userId + " does not exist");
        }

        User user = userOptional.get();
        user.setRole(newUser.getRole());
        user.setUsername(newUser.getUsername());
        user.setEmail(newUser.getEmail());
        user.setPassword(newUser.getPassword());

        return userRepository.save(user);
    }

    public void delete(Long userId) {
        Optional<User> userOptional = userRepository.findById(userId);

        if (!userOptional.isPresent()) {
            throw new ResourceNotFoundException("User with ID " + userId + " does not exist");
        }

        User existingUser = userOptional.get();
        userRepository.delete(existingUser);

        LOGGER.debug("User with id {} was deleted from the database", userId);
    }
}
