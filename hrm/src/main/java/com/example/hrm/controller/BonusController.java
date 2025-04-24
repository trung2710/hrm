package com.example.hrm.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class BonusController {
    @GetMapping("/bonus")
    public String getBonusPage(){
        return "admin/bonus/show";
    }
}
