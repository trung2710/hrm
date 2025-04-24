package com.example.hrm.controller;

import com.example.hrm.domain.PhongBan;
import com.example.hrm.repository.DepartmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
public class DepartmentController {
    @Autowired
    private DepartmentRepository departmentRepository;
    @GetMapping("/department")
    public String getDepartmentPage(Model model){
        List<PhongBan> pb=this.departmentRepository.findAll();
        for(PhongBan p:pb){
            p.setSoNhanVien(10);
            p.setTenTruongPhong("Trung");
        }
        model.addAttribute("pb",pb);
        return "admin/department/show";
    }

    @PostMapping("/department/update")
    public String updateDepartment(Model model, @RequestParam("id") Integer id , @RequestParam("tenPB") String tenPhongBan){
        PhongBan pb=this.departmentRepository.findById(id).get();
        pb.setTenPhongBan(tenPhongBan);
        this.departmentRepository.save(pb);
        return "redirect:/department";
    }

    @PostMapping("/department/create")
    public String createDepartment(RedirectAttributes redirectAttributes, @RequestParam("ten") String tenPhongBan){
        PhongBan pb=new PhongBan();
        pb.setTenPhongBan(tenPhongBan);
        this.departmentRepository.save(pb);
        String mess="Da tao thanh cong 1 phong ban";
        redirectAttributes.addFlashAttribute("mess",mess);
        return "redirect:/department";
    }

    @PostMapping("/department/delete")
    public String deleteDepartment(RedirectAttributes redirectAttributes, @RequestParam("id") Integer id){
        this.departmentRepository.deleteById(id);
        String mess="Da xoa thanh cong 1 phong ban";
        redirectAttributes.addFlashAttribute("mess",mess);
        return "redirect:/department";
    }
}
