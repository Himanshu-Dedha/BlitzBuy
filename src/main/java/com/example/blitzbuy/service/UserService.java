package com.example.blitzbuy.service;

import com.example.blitzbuy.data.entity.Users;

public interface UserService {
    Users findByUsername(String username);

    Users save(Users users);
}
