package com.example.hrm.controller;

import com.example.hrm.domain.NhanVien;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class EmployeeController {
    @GetMapping("/employee")
    public String getEmployeePage(){
        return "admin/employee/show";
    }
    @GetMapping("/employee/create")
    public String getEmployeeCreatePage(Model model){
        model.addAttribute("newEmployee", new NhanVien());
        return "admin/employee/create";
    }


}
