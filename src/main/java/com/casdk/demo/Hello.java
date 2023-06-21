package com.casdk.demo;

import org.springframework.stereotype.Component;

import com.casdk.demo.model.Greeting;
import com.casdk.demo.model.User;


import java.util.function.Function;

@Component
public class Hello implements Function<User, Greeting> {

    public Greeting apply(User user) {
        return new Greeting("Hello, " + user.getName() + "!\n");
    }
}
