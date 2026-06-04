package com.wxai.controller;

import com.wxai.entity.User;
import com.wxai.request.LoginRequest;
import com.wxai.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/user")
public class UserController {

    @Resource
    private UserService userService;

    @PostMapping("/register")
    public Map<String, Object> register(@RequestBody LoginRequest req) {
        try {
            User user = userService.register(req.getUsername(), req.getPassword());
            return success(userToMap(user));
        } catch (IllegalArgumentException e) {
            return error(e.getMessage());
        }
    }

    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody LoginRequest req) {
        try {
            User user = userService.login(req.getUsername(), req.getPassword());
            return success(userToMap(user));
        } catch (IllegalArgumentException e) {
            return error(e.getMessage());
        }
    }

    @PostMapping("/logout")
    public Map<String, Object> logout(HttpServletRequest request) {
        User user = (User) request.getAttribute("currentUser");
        if (user != null) {
            userService.logout(user.getId());
        }
        return success(null);
    }

    @GetMapping("/info")
    public Map<String, Object> info(HttpServletRequest request) {
        User user = (User) request.getAttribute("currentUser");
        return success(userToMap(user));
    }

    private Map<String, Object> userToMap(User user) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", user.getId());
        map.put("username", user.getUsername());
        map.put("nickname", user.getNickname());
        map.put("token", user.getToken());
        return map;
    }

    private Map<String, Object> success(Object data) {
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("data", data);
        return result;
    }

    private Map<String, Object> error(String message) {
        Map<String, Object> result = new HashMap<>();
        result.put("code", 400);
        result.put("message", message);
        return result;
    }

}