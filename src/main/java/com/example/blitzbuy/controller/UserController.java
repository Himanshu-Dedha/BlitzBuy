package com.example.blitzbuy.controller;

import com.example.blitzbuy.data.entity.Users;
import com.example.blitzbuy.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("v1/users")
public class UserController {

    private final UserService userService;

    @GetMapping("/{username}")
    public ResponseEntity<Users> findUserById(@PathVariable(value = "username") String username) {
        return ResponseEntity.ok(userService.findByUsername(username));
    }


    @PostMapping()
    public ResponseEntity<Users> createUser(@RequestBody Users user) {
        return ResponseEntity.ok(userService.save(user));
    }
}
