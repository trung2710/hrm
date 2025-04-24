package com.example.hrm.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ViolationController {
    @GetMapping("/violation")
    public String getViolationPage(){
        return "admin/violation/show";
    }
}
