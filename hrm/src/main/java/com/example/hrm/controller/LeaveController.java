package com.example.hrm.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LeaveController {
    @GetMapping("/leave")
    public String getLeavePage(){
        return "admin/leave/show";
    }
}
