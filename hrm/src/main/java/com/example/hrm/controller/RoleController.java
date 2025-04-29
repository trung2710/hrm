package com.example.hrm.controller;

import com.example.hrm.domain.Quyen;
import com.example.hrm.repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class RoleController {
    @Autowired
    private RoleRepository roleRepository;
    @GetMapping("/role")
    public String getRolePage(Model model){
        List<Quyen> quyenList = this.roleRepository.findAll();
        model.addAttribute("quyenList",quyenList);
        return "admin/role/show";
    }
    @PostMapping("/role/create")
    public String createRole(Model model, @RequestParam("ten") String ten, @RequestParam("mota") String mota){
        Quyen quyen=new Quyen();
        quyen.setTenQuyen(ten);
        quyen.setMoTa(mota);
        this.roleRepository.save(quyen);
        return "redirect:/role";
    }

    @PostMapping("/role/update")
    public String updateRole(Model model, @RequestParam("id") Integer id, @RequestParam("ten") String ten, @RequestParam("mota") String mota){
        Quyen quyen=this.roleRepository.findById(id).get();
        quyen.setTenQuyen(ten);
        quyen.setMoTa(mota);
        this.roleRepository.save(quyen);
        return "redirect:/role";
    }
    @PostMapping("/role/delete")
    public String deleteRole(Model model, @RequestParam("id") Integer id){
        this.roleRepository.deleteById(id);
        return "redirect:/role";
    }
}
