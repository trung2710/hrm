package com.example.hrm.controller;

import com.example.hrm.domain.BaoHiem;
import com.example.hrm.domain.NhanVien;
import com.example.hrm.repository.InsuranceRepository;
import com.example.hrm.repository.UserRepository;
import com.example.hrm.service.CustomUserDetail;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.query.Param;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;

@Controller
public class InsuranceController {
    @Autowired
    private InsuranceRepository insuranceRepository;
    @Autowired
    private UserRepository userRepository;
    @GetMapping("/insurance")
    public String getInsurancePage(Model model, Authentication authentication){
        CustomUserDetail cus=(CustomUserDetail) authentication.getPrincipal();
        NhanVien nhanVien=cus.getNhanVien();
        List<BaoHiem> bhs=this.insuranceRepository.findAll();
        if(nhanVien.getChucVu().getQuyen().getTenQuyen().equals("Admin") || nhanVien.getChucVu().getQuyen().getTenQuyen().equals("Manager")){
            model.addAttribute("bhs",bhs );
            List<NhanVien> nhanViens=this.userRepository.findAll();
            model.addAttribute("nhanViens",nhanViens);
        }
        else{
            List<BaoHiem> bhs1=new java.util.ArrayList<>();
            for(BaoHiem bh:bhs){
                if(bh.getNhanVien().getId().equals(nhanVien.getId())){
                    bhs1.add(bh);
                }
            }
            model.addAttribute("bhs",bhs1);
        }


        return "admin/insurance/show";
    }

    @PostMapping("/insurance/create")
    public String postCreateInsurance(RedirectAttributes redirectAttributes, Model model
    , @RequestParam("id") Integer id, @RequestParam("ten") String ten, @RequestParam("sta")LocalDate sta,
                                      @RequestParam("end") LocalDate end){
        if(this.insuranceRepository.findByName(id, ten, sta)==null){
            BaoHiem bh=new BaoHiem();
            bh.setTenBaoHiem(ten);
            bh.setNgayBatDau(sta);
            bh.setNgayHetHan(end);
            bh.setNhanVien(this.userRepository.findById(id));
            this.insuranceRepository.save(bh);
            String mess="Da tao thanh cong 1 bao hiem";
            redirectAttributes.addFlashAttribute("mess",mess);
        }
        else{
            String mess="Da co bao hiem ton tai voi nguoi dung nay";
            redirectAttributes.addFlashAttribute("mess",mess);
            return "redirect:/insurance";
        }
        return "redirect:/insurance";
    }

    @PostMapping("/insurance/update")
    public String postUpdateInsurance(Model model,@Param("id") Integer id,  @Param("ten") String ten
    , @Param("sta") LocalDate sta, @Param("end") LocalDate end){
        BaoHiem baoHiem=this.insuranceRepository.findById(id).get();
        baoHiem.setTenBaoHiem(ten);
        baoHiem.setNgayBatDau(sta);
        baoHiem.setNgayHetHan(end);
        this.insuranceRepository.save(baoHiem);
        return "redirect:/insurance";
    }

    @PostMapping("/insurance/delete")
    public String postDeleteInsurance(Model model, @Param("id") Integer id){
        this.insuranceRepository.deleteById(id);
        return "redirect:/insurance";
    }
}
