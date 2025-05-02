package com.example.hrm.controller;

import com.example.hrm.domain.NV_Thuong;
import com.example.hrm.domain.NhanVien;
import com.example.hrm.domain.Thuong;
import com.example.hrm.domain.idClass.NVThuongId;
import com.example.hrm.repository.BonusRepository;
import com.example.hrm.repository.NV_AllowanceRepository;
import com.example.hrm.repository.NV_ThuongRepository;
import com.example.hrm.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Controller
public class BonusController {
    @Autowired
    private BonusRepository bonusRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private NV_ThuongRepository nvThuongRepository;
    @GetMapping("/bonus")
    public String getBonusPage(Model model){
        List<Thuong> thuongs=this.bonusRepository.findAll();
        model.addAttribute("thuongs",thuongs);
        List<NhanVien> nhanVienList=this.userRepository.findAll();
        model.addAttribute("nhanVienList",nhanVienList);
        List<NhanVien> qd=new ArrayList<>();
        for(NhanVien x:nhanVienList){
            if(x.getChucVu().getId()==1 || x.getChucVu().getId()==2){
                qd.add(x);
            }
        }
        model.addAttribute("qds",qd);
        List<NV_Thuong> nvThuongList=this.nvThuongRepository.findAll();
        model.addAttribute("nvThuongList",nvThuongList);
        return "admin/bonus/show";
    }

    @PostMapping("/bonus/create")
    public String postCreateBonus(Model model, @RequestParam("id") Integer id, @RequestParam("ten") String ten, @RequestParam("tien") BigDecimal tien){
        Thuong thuong=new Thuong();
        thuong.setTenThuong(ten);
        thuong.setNguoiRaQuyetDinh(this.userRepository.findById(id));
        thuong.setMucThuong(tien);
        this.bonusRepository.save(thuong);
        return "redirect:/bonus";
    }

    @PostMapping("/bonus/update")
    public String postUpdateBonus(Model model, @RequestParam("ten") String ten, @RequestParam("tien") BigDecimal tien, @RequestParam("id") Integer id, @RequestParam("ma") String ma){
        Thuong thuong=this.bonusRepository.findById(Integer.parseInt(ma));
        thuong.setTenThuong(ten);
        thuong.setMucThuong(tien);
        thuong.setNguoiRaQuyetDinh(this.userRepository.findById(id));
        this.bonusRepository.save(thuong);
        return "redirect:/bonus";
    }

    @PostMapping("/bonus/delete")
    public String postDeleteBonus(Model model, @RequestParam("id") Integer id){
        this.bonusRepository.deleteById(id);
        return "redirect:/bonus";
    }


    @PostMapping("/bonus/create/employee")
    public String postCreateBonusEmployee(Model model, @RequestParam("id") Integer id, @RequestParam("ma") Integer ma){
        NV_Thuong nvThuong=new NV_Thuong();
        nvThuong.setNhanVien(this.userRepository.findById(id));
        nvThuong.setThuong(this.bonusRepository.findById(ma).get());
        Thuong thuong=this.bonusRepository.findById(ma).get();
        if(thuong.getMucThuong().doubleValue()<=100){
            NhanVien nhanVien=this.userRepository.findById(id);
            BigDecimal tien=BigDecimal.valueOf(thuong.getMucThuong().doubleValue()*nhanVien.getLuongHienTai().doubleValue()/100);
            nvThuong.setMucTien(tien);
        }
        else{
            nvThuong.setMucTien(thuong.getMucThuong());
        }
        this.nvThuongRepository.save(nvThuong);
        return "redirect:/bonus";
    }

    @PostMapping("/bonus/update/employee")
    public String postUpdateBonusEmployee(Model model, @RequestParam("tien") BigDecimal tien, @RequestParam("id") Integer id, @RequestParam("ma") Integer ma){
        NVThuongId ID=new NVThuongId(this.bonusRepository.findById(ma).get(), this.userRepository.findById(id));
        NV_Thuong nvThuong=this.nvThuongRepository.findById(ID).get();
        nvThuong.setMucTien(tien);
        this.nvThuongRepository.save(nvThuong);
        return "redirect:/bonus";
    }

    @PostMapping("/bonus/delete/employee")
    public String postDeleteBonusEmployee(Model model, @RequestParam("id") Integer id, @RequestParam("ma") Integer ma){
        NVThuongId ID=new NVThuongId(this.bonusRepository.findById(ma).get(), this.userRepository.findById(id));
        this.nvThuongRepository.deleteById(ID);
        return "redirect:/bonus";
    }
}
