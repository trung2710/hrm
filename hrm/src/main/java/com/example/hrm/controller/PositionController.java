package com.example.hrm.controller;

import com.example.hrm.repository.PositionRepository;
import com.example.hrm.domain.ChucVu;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class PositionController {
    @Autowired
    private PositionRepository positionRepository;
    @GetMapping("/position")
    public String getPositionPage(Model model){
        List<ChucVu> chucVus=this.positionRepository.findAllPositions();
        model.addAttribute("chucVus",chucVus);
        return "admin/position/show";
    }
}
