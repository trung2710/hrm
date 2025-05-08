package com.example.hrm.controller;

import com.example.hrm.constant.DonTuStatusEnum;
import com.example.hrm.domain.DonTu;
import com.example.hrm.domain.NhanVien;
import com.example.hrm.repository.FormRepository;
import com.example.hrm.repository.UserRepository;
import com.example.hrm.service.CustomUserDetail;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.List;

@Controller
public class FormController {
    @Autowired
    private FormRepository formRepository;
    @Autowired
    private UserRepository userRepository;
    @GetMapping("/form")
    public String getFormPage(Model model, Authentication authentication){
        CustomUserDetail cus=(CustomUserDetail) authentication.getPrincipal();
        NhanVien nhanVien=cus.getNhanVien();
        List<DonTu> forms = formRepository.findAll();
        if(nhanVien.getChucVu().getQuyen().getTenQuyen().equals("Admin") || nhanVien.getChucVu().getQuyen().getTenQuyen().equals("Manager")){
            model.addAttribute("forms",forms);
        }
        else{
            List<DonTu> forms1=new java.util.ArrayList<>();
            for(DonTu form:forms){
                if(form.getNhanVien().getId().equals(nhanVien.getId())){
                    forms1.add(form);
                }
            }
            model.addAttribute("forms",forms1);
        }
        return "admin/form/show";
    }

    @PostMapping("/form/create")
    public String postCreateForm(Model model, @RequestParam("type") String type, Authentication authentication){
        CustomUserDetail cus=(CustomUserDetail) authentication.getPrincipal();
        NhanVien nhanVien=cus.getNhanVien();
        DonTu form=new DonTu();
        form.setNguoiPheDuyet(this.userRepository.findById(1));
        form.setNhanVien(nhanVien);
        form.setTrangThai(DonTuStatusEnum.WAITING.getValue());
        form.setLoaiDon(type);
        form.setNgayGuiDon(LocalDate.now());
        this.formRepository.save(form);
        return "redirect:/form";
    }

    @PostMapping("/form/update")
    public String postUpdateForm(Model model, @RequestParam("action") String action, @RequestParam("id") Integer id){
        DonTu form=this.formRepository.findById(id).get();
        if(action.equals("no")){
            form.setTrangThai("Từ chối");
        }
        else{
            form.setTrangThai("Đã duyệt");
        }
        this.formRepository.save(form);
        return "redirect:/form";
    }

    @PostMapping("/form/delete")
    public String postDeleteForm(Model model, @RequestParam("id") Integer id){
        this.formRepository.deleteById(id);
        return "redirect:/form";
    }

}
