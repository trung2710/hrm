package com.example.hrm.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PositionController {
    @GetMapping("/position")
    public String getPositionPage(){
        return "admin/position/show";
    }
}
