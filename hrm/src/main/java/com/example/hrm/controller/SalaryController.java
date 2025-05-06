package com.example.hrm.controller;

import com.example.hrm.domain.*;
import com.example.hrm.repository.SalaryRepository;
import com.example.hrm.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.List;

@Controller
public class SalaryController {
    @Autowired
    private SalaryRepository salaryRepository;
    @Autowired
    private UserRepository userRepository;
    @GetMapping("/salary")
    public String getSalaryPage(Model model){
        List<Luong> luongs = salaryRepository.findAll();
        model.addAttribute("luongs",luongs);
        for(Luong luong:luongs){
            NhanVien user=luong.getNhanVien();
            List<NV_Thuong> thuongs=user.getThuongs();
            BigDecimal sum1=BigDecimal.ZERO;
            for(NV_Thuong thuong:thuongs){
                sum1 = sum1.add(thuong.getMucTien());
            }
            List<NV_PhuCap> phuCaps=user.getPhuCaps();
            BigDecimal sum2=BigDecimal.ZERO;
            for(NV_PhuCap phuCap:phuCaps){
                sum2 = sum2.add(phuCap.getMucTien());
            }
            List<NV_ViPham> viPhams=user.getViPhams();
            BigDecimal sum3=BigDecimal.ZERO;
            for (NV_ViPham viPham:viPhams){
                sum3=sum3.add(viPham.getViPham().getSoTienPhat());
            }
            luong.setTienThuong(sum1);
            luong.setTienPC(sum2);
            luong.setTienViPham(sum3);
            List<ChamCong> chamCongs=user.getChamCongs();
            Double sum4=0.0;
            for(ChamCong chamCong:chamCongs){
                if(luong.getThang()==chamCong.getNgay().getMonthValue()){
                    sum4+=chamCong.getSoGioTangCa();
                }
            }
            luong.setTienTangCa(BigDecimal.valueOf(sum4*100000));
            this.salaryRepository.save(luong);
        }
        List<NhanVien> users = userRepository.findAll();
        model.addAttribute("users",users);
        return "admin/salary/show";
    }

    @PostMapping("/salary/create")
    public String createSalary(RedirectAttributes redirectAttributes, @RequestParam("id") Integer id, @RequestParam("thang") Integer thang, @RequestParam("nam") Integer nam){
        Luong luong=new Luong();
        List<Luong> luongs=this.salaryRepository.findAll();
        boolean check=true;
        for(Luong x:luongs){
            if(x.getThang()==thang && x.getNhanVien().getId()==id && x.getNam()==nam){
                check=false;
                String mess="Bang luong cua"+x.getNhanVien().getHoTen()+"da co trong thang "+thang+"/"+nam;
                redirectAttributes.addFlashAttribute("mess",mess);
                return "redirect:/salary";
            }
        }
        if(check==true){
            luong.setNam(nam);
            luong.setThang(thang);
            luong.setNhanVien(this.userRepository.findById(id));
            luong.setThueThuNhap(BigDecimal.ZERO);
            luong.setTongThuNhap(BigDecimal.ZERO);
            this.salaryRepository.save(luong);
            //Doan code duoi nay can phai set cac thong so cua bang luong vao day, xoa o phan /salary
        }
        return "redirect:/salary";
    }


    @PostMapping("/salary/delete")
    public String deleteSalary(Model model, @RequestParam("id") Integer id){
        this.salaryRepository.deleteById(id);
        return "redirect:/salary";
    }

}
