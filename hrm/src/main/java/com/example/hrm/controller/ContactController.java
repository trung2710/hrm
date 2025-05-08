package com.example.hrm.controller;

import com.example.hrm.constant.ContractStatusEnum;
import com.example.hrm.domain.HopDong;
import com.example.hrm.domain.NhanVien;
import com.example.hrm.repository.ContractRepository;
import com.example.hrm.repository.UserRepository;
import com.example.hrm.service.CustomUserDetail;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Controller
public class ContactController {
    @Autowired
    private ContractRepository contractRepository;
    @Autowired
    private UserRepository userRepository;
    @GetMapping("/contract")
    public String getContactPage(Model model, Authentication authentication){
        CustomUserDetail cus=(CustomUserDetail) authentication.getPrincipal();
        NhanVien nhanVien=cus.getNhanVien();
        if(nhanVien.getChucVu().getQuyen().getTenQuyen().equals("Admin") || nhanVien.getChucVu().getQuyen().getTenQuyen().equals("Manager")){
            List<HopDong> hopDongs = contractRepository.findAll();
            List<NhanVien> nhanVienList=this.userRepository.findAll();
            model.addAttribute("nhanViens",nhanVienList);
            int dem1=0;
            int dem2=0;
            int dem3=0;
            for(HopDong x : hopDongs){
                //logic nếu hợp đồng hết hạn thì set trạng thái của hợp đồng.
                if(x.getNgayKetThuc().isBefore(LocalDate.now())){
                    x.setTrangThai(ContractStatusEnum.EXPIRED.getValue());
                    this.contractRepository.save(x);
                }
                if (x.getTrangThai().equals(ContractStatusEnum.VALID.getValue())){
                    dem1+=1;
                }
                else if(ChronoUnit.DAYS.between(x.getNgayKetThuc(), LocalDate.now())<=60){
                    dem2+=1;
                }
                else{
                    dem3+=1;
                }
            }
            model.addAttribute("valid",dem1);
            model.addAttribute("expire",dem2);
            model.addAttribute("expired",dem3);
            model.addAttribute("hopDongs",hopDongs);
        }
        else{
            List<HopDong> hopDongs = contractRepository.findAll();
            List<HopDong>hp=new ArrayList<>();
            for (HopDong x : hopDongs){
                if(x.getNhanVien().getId().equals(nhanVien.getId())){
                    hp.add(x);
                }
            }
            model.addAttribute("hopDongs",hp);
        }

        return "admin/contract/show";
    }

    @PostMapping("/contract/create")
    public String postCreateContact(Model model
    , @RequestParam("ten") String ten, @RequestParam("id") Integer id
    , @RequestParam("sta") LocalDate sta, @RequestParam("end") LocalDate end
    , @RequestParam("luong")BigDecimal luongCoBan){
        HopDong hopDong=new HopDong();
        List<HopDong> hopDongs=this.contractRepository.findAll();
        boolean check=true;
        for(HopDong x:hopDongs){
            if(x.getNhanVien().getId().equals(id) && x.getNgayKetThuc().isAfter(sta)){
                check=false;
                break;
            }
        }
        if(check==true){
            hopDong.setLoaiHopDong(ten);
            hopDong.setNhanVien(this.userRepository.findById(id));
            hopDong.setNgayBatDau(sta);
            hopDong.setNgayKetThuc(end);
            hopDong.setLuongCoBan(luongCoBan);
            this.contractRepository.save(hopDong);
            NhanVien nhanVien=hopDong.getNhanVien();
            nhanVien.setLuongHienTai(hopDong.getLuongCoBan());
            this.userRepository.save(nhanVien);
        }
        return "redirect:/contract";
    }

    @PostMapping("/contract/delete")
    public String postDeleteContact(Model model, @RequestParam("id") Integer id){
        this.contractRepository.deleteById(id);
        return "redirect:/contract";
    }

}
