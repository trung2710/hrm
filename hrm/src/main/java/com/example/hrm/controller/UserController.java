package com.example.hrm.controller;

import com.example.hrm.domain.NhanVien;
import com.example.hrm.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class UserController {
    @Autowired
    private UserRepository userRepository;
    @GetMapping("/user")
    public String getUserPage(Model model){
        model.addAttribute("users",userRepository.findAll());
        return "admin/user/show";
    }

    @GetMapping("/user{id}")
    public String getUserDetail(Model model, @PathVariable Integer id){
        NhanVien nhanVien=this.userRepository.findById(id);
        model.addAttribute("user",nhanVien);
        return "admin/user/detail";
    }

    @GetMapping("/user/create")
    public String getUserCreatePage(Model model){
        model.addAttribute("newUser",new NhanVien());
        return "admin/user/create";
    }



    @GetMapping("/user/update/{id}")
    public String getUserUpdatePage(Model model, @PathVariable Integer id){
        NhanVien nhanVien=this.userRepository.findById(id);
        model.addAttribute("user",nhanVien);
        return "admin/user/update";
    }

    @GetMapping("/user/delete/{id}")
    public String getUserDeletePage(Model model, @PathVariable Integer id){
        NhanVien nhanVien=new NhanVien();
        nhanVien.setId(id);
        model.addAttribute("user",nhanVien);
        return "admin/user/delete";
    }

}
