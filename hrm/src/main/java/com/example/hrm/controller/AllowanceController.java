package com.example.hrm.controller;

import com.example.hrm.domain.PhuCap;
import com.example.hrm.repository.AllowanceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

@Controller
public class AllowanceController {
    @Autowired
    private AllowanceRepository allowanceRepository;
    @GetMapping("/allowance")
    public String getAllowancePage(Model model){
        List<PhuCap> phuCaps = allowanceRepository.findAll();
        model.addAttribute("phuCaps",phuCaps);
        return "admin/allowances/show";
    }

    @PostMapping("/allowance/create")
    public String postCreateAllowance(Model model){
        return "redirect:/allowance";
    }

    @PostMapping("/allowance/update")
    public String postUpdateAllowance(Model model){
        return "redirect:/allowance";
    }

    @PostMapping("/allowance/delete")
    public String postDeleteAllowance(Model model){
        return "redirect:/allowance";
    }
}
