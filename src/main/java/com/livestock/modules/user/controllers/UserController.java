package com.livestock.modules.user.controllers;

import com.livestock.modules.user.domain.user.User;
import com.livestock.modules.user.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/cavalo")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/create")
    public ResponseEntity<Object> create(@RequestBody User user) {
        try {
            var userResponse = userService.createUser(user);
            return ResponseEntity.ok(userResponse);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
