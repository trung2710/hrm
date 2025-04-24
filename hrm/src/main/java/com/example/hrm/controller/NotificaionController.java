package com.example.hrm.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class NotificaionController {
    @GetMapping("/notification")
    public String getNotificationPage(){
        return "admin/notification/show";
    }
}
