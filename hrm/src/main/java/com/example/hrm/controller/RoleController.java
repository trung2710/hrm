package com.example.hrm.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class RoleController {
    @GetMapping("/role")
    public String getRolePage(){
        return "admin/role/show";
    }
}
