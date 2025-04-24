package com.example.hrm.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AllowanceController {
    @GetMapping("/allowance")
    public String getAllowancePage(){
        return "admin/allowances/show";
    }
}
