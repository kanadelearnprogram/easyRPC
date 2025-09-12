package com.kanade.provider;

import com.kanade.common.model.User;
import com.kanade.common.service.UserService;

public class UserServiceImpl implements UserService {
    @Override
    public User getUser(User user) {
        System.out.println(user);
        return user;
    }
}
