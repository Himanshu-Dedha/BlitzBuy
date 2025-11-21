package com.example.blitzbuy.service.impl;

import com.example.blitzbuy.data.entity.Users;
import com.example.blitzbuy.repository.UsersRepository;
import com.example.blitzbuy.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UsersRepository usersRepository;

    @Override
    public Users findByUsername (String username) {
        return usersRepository.findByUsername(username);
    }

    @Override
    public Users save(Users users) {
        return usersRepository.save(users);
    }
}
