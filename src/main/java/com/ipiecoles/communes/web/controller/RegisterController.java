package com.ipiecoles.communes.web.controller;

import com.ipiecoles.communes.web.model.User;
import com.ipiecoles.communes.web.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("/register")
@Controller
public class RegisterController {

    @Autowired
    UserRepository userRepository;

    @GetMapping
    public String register(){
        return "register";
    }

    @PostMapping("/new)")
    public String createNewUser(User user){
        return "login";
    }
}
