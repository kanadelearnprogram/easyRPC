package com.kanade.consumer;

import com.kanade.common.model.User;
import com.kanade.common.service.UserService;

import java.util.Optional;

public class EasyConsumer {
    public static void main(String[] args) {
        UserService userService = ServiceProxyFactory.getProxy(UserService.class);

        User user = new User();
        user.setName("kanade");

        User temp = userService.getUser(user);
        if (temp != null){
            System.out.println(temp);
        }else {
            System.out.println("null");
        }
    }
}
