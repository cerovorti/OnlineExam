package com.exam.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.exam.entity.User;

public interface UserService extends IService<User> {
    User login(String username, String password);
}
