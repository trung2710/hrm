package com.example.hrm.controller;

import com.example.hrm.domain.DonTu;
import com.example.hrm.repository.FormRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class FormController {
    @Autowired
    private FormRepository formRepository;
    @GetMapping("/form")
    public String getFormPage(Model model){
        List<DonTu> forms = formRepository.findAll();
        model.addAttribute("forms",forms);
        return "admin/form/show";
    }

    @PostMapping("/form/create")
    public String postCreateForm(Model model, @RequestParam("ten") String ten, @RequestParam("ngay") String ngay){
        return "redirect:/form";
    }

    @PostMapping("/form/update")
    public String postUpdateForm(Model model, @RequestParam("action") String action, @RequestParam("id") Integer id){
        DonTu form=this.formRepository.findById(id).get();
        if(action.equals("reject")){
            form.setTrangThai("Từ chối");
        }
        else{
            form.setTrangThai("Đã duyệt");
        }
        return "redirect:/form";
    }

    @PostMapping("/form/delete")
    public String postDeleteForm(Model model, @RequestParam("id") Integer id){
        this.formRepository.deleteById(id);
        return "redirect:/form";
    }

}
