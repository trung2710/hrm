package com.example.hrm.controller;

import com.example.hrm.repository.DepartmentRepository;
import com.example.hrm.repository.PositionRepository;
import com.example.hrm.domain.ChucVu;
import com.example.hrm.repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
public class PositionController {
    @Autowired
    private PositionRepository positionRepository;
    @Autowired
    private DepartmentRepository departmentRepository;
    @Autowired
    private RoleRepository roleRepository;
    @GetMapping("/position")
    public String getPositionPage(Model model){
        List<ChucVu> chucVus=this.positionRepository.findAllPositions();
        model.addAttribute("chucVus",chucVus);
        return "admin/position/show";
    }
    @PostMapping("/position/create")
    private String postCreatePosition(RedirectAttributes redirectAttributes,
    @RequestParam("tenChucVu") String tenChucVu,
    @RequestParam("tenQuyen") String maQuyen,
    @RequestParam("tenPhongBan") String maPhongBan){
        ChucVu chucVu=new ChucVu();
        chucVu.setTenChucVu(tenChucVu);
        chucVu.setPhongBan(this.departmentRepository.findByTenPhongBan(maPhongBan));
        chucVu.setQuyen(this.roleRepository.findByName(maQuyen));
        this.positionRepository.save(chucVu);
        String mess="Da tao thanh cong 1 chuc vu";
        redirectAttributes.addFlashAttribute("mess",mess);
        return "redirect:/position";
    }
    @PostMapping("/position/delete")
    private String postDeletePosition(RedirectAttributes redirectAttributes, @RequestParam("id") Integer id){
        this.positionRepository.deleteById(id);
        String mess="Da xoa thanh cong 1 chuc vu";
        redirectAttributes.addFlashAttribute("mess",mess);
        return "redirect:/position";
    }

    @PostMapping("/position/update")
    private String postUpdatePosition(RedirectAttributes redirectAttributes,
    @RequestParam("id") Integer id,
    @RequestParam("tenChucVu") String tenChucVu,
    @RequestParam("tenQuyen") String tenQuyen,
    @RequestParam("tenPhongBan") String tenPhongBan){
        ChucVu chucVu=this.positionRepository.findById(id).get();
        chucVu.setTenChucVu(tenChucVu);
        chucVu.setPhongBan(this.departmentRepository.findByTenPhongBan(tenPhongBan));
        chucVu.setQuyen(this.roleRepository.findByName(tenQuyen));
        this.positionRepository.save(chucVu);
        String mess="Da sua thanh cong chuc vu co ma chuc vu : "+chucVu.getId();
        redirectAttributes.addFlashAttribute("mess",mess);
        return "redirect:/position";
    }
}
