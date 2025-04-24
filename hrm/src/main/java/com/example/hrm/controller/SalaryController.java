package com.example.hrm.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SalaryController {
    @GetMapping("/salary")
    public String getSalaryPage(){
        return "admin/salary/show";
    }
}
