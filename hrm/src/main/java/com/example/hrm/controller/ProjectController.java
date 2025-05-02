package com.example.hrm.controller;

import com.example.hrm.domain.DuAn;
import com.example.hrm.domain.NV_DuAn;
import com.example.hrm.repository.ProjectReppository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.swing.*;
import java.util.List;

@Controller
public class ProjectController {
    @Autowired
    private ProjectReppository projectReppository;
    @Autowired
    private ObjectMapper objectMapper;
    @GetMapping("/project")
    public String getProjectPage(Model model){
        List<DuAn> duAns = projectReppository.findAllProjects();

        for(DuAn x : duAns){
            String s="";
            for(NV_DuAn y : x.getListNhanVien()){
                s+=y.getNhanVien().getHoTen()+":"+y.getVaiTro()+",";
            }
            s=s.substring(0,s.length()-1);
            x.setNhanViens(s);
        }

        model.addAttribute("duAns",duAns);
        DuAn duAn = new DuAn();
        model.addAttribute("newDuAn",duAn);
        return "admin/project/show";
    }

    @PostMapping("/project/create")
    public String postCreateProject(Model model){
        return "redirect:/project";
    }

    @PostMapping("/project/update")
    public String postUpdateProject(Model model){
        return "redirect:/project";
    }

    @PostMapping("/project/delete")
    public String postDeleteProject(Model model, @RequestParam("id") Integer id){
        this.projectReppository.deleteById(id);
        return "redirect:/project";
    }
}
