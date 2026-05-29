package com.exam.controller;

import javax.servlet.http.HttpServletRequest;

public class BaseController {

    protected Long getCurrentUserId(HttpServletRequest request) {
        Object userId = request.getAttribute("userId");
        if (userId != null) {
            return Long.valueOf(userId.toString());
        }
        return null;
    }
}