package com.example.hrm.controller;

import com.example.hrm.domain.NV_PhuCap;
import com.example.hrm.domain.NhanVien;
import com.example.hrm.domain.PhuCap;
import com.example.hrm.domain.idClass.NVPhuCapId;
import com.example.hrm.repository.AllowanceRepository;
import com.example.hrm.repository.NV_AllowanceRepository;
import com.example.hrm.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Controller
public class AllowanceController {
    @Autowired
    private AllowanceRepository allowanceRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private NV_AllowanceRepository nv_allowanceRepository;
    @GetMapping("/allowance")
    public String getAllowancePage(Model model){
        List<PhuCap> phuCaps = allowanceRepository.findAll();
        model.addAttribute("phuCaps",phuCaps);
        List<NhanVien> nhanVienList = userRepository.findAll();
        model.addAttribute("nhanViens",nhanVienList);
        List<NV_PhuCap> nv_phuCaps = nv_allowanceRepository.findAll();
        model.addAttribute("nv_phuCaps",nv_phuCaps);
        return "admin/allowances/show";
    }

    @PostMapping("/allowance/create")
    public String postCreateAllowance(Model model
    ,@RequestParam("ten") String ten,@RequestParam("mota") String mota, @RequestParam("loai") String loai
    ,@RequestParam("muc") Double muc){
        PhuCap phuCap=new PhuCap();
        phuCap.setTenPhuCap(ten);
        phuCap.setMoTa(mota);
        phuCap.setLoaiPhuCap(loai);
        phuCap.setMucPhuCap(muc);
        this.allowanceRepository.save(phuCap);
        return "redirect:/allowance";
    }

    @PostMapping("/allowance/update")
    public String postUpdateAllowance(Model model,@RequestParam("id") Integer id
    ,@RequestParam("ten") String ten,@RequestParam("mota") String mota, @RequestParam("loai") String loai
    ,@RequestParam("muc") Double muc){
        PhuCap phuCap=this.allowanceRepository.findById(id).get();
        phuCap.setTenPhuCap(ten);
        phuCap.setMoTa(mota);
        phuCap.setLoaiPhuCap(loai);
        phuCap.setMucPhuCap(muc);
        this.allowanceRepository.save(phuCap);
        return "redirect:/allowance";
    }

    @PostMapping("/allowance/delete")
    public String postDeleteAllowance(Model model,@RequestParam("id") Integer id){
        this.allowanceRepository.deleteById(id);
        return "redirect:/allowance";
    }

    @PostMapping("/allowance/create/employee")
    public String postCreateAllowanceEmployee(Model model
    , @RequestParam("id") Integer id, @RequestParam("type") Integer type
    , @RequestParam(value = "sta", required = false) LocalDate sta, @RequestParam(value = "end", required = false) LocalDate end){
        NV_PhuCap nv_phuCap=new NV_PhuCap();
        nv_phuCap.setNhanVien(this.userRepository.findById(id));
        nv_phuCap.setPhuCap(this.allowanceRepository.findById(type).get());
        nv_phuCap.setNgayBatDau(sta);
        nv_phuCap.setNgayKetThuc(end);
        PhuCap phuCap=this.allowanceRepository.findById(type).get();
        if(phuCap.getMucPhuCap()<=100){
            NhanVien nhanVien=this.userRepository.findById(id);
            BigDecimal mucPhuCap=BigDecimal.valueOf(phuCap.getMucPhuCap()*nhanVien.getLuongHienTai().doubleValue()/100);
            nv_phuCap.setMucTien(mucPhuCap);
        }
        else{
            nv_phuCap.setMucTien(BigDecimal.valueOf(phuCap.getMucPhuCap()));
        }
        this.nv_allowanceRepository.save(nv_phuCap);
        return "redirect:/allowance";
    }
    @PostMapping("/allowance/update/employee")
    public String postUpdateAllowanceEmployee(Model model, @RequestParam("type") Integer type,@RequestParam("id") Integer id,
    @RequestParam(value = "sta", required = false) LocalDate sta, @RequestParam(value = "end", required = false) LocalDate end){
        //Neu sua type thi phair set lai muc tien
        NVPhuCapId nv_id=new NVPhuCapId(this.userRepository.findById(id), this.allowanceRepository.findById(type).get());
        NV_PhuCap nv=this.nv_allowanceRepository.findById(nv_id).get();
        nv.setNgayBatDau(sta);
        nv.setNgayKetThuc(end);
        nv.setPhuCap(this.allowanceRepository.findById(type).get());
        PhuCap phuCap=this.allowanceRepository.findById(type).get();
        if(phuCap.getMucPhuCap()<=100){
            NhanVien nhanVien=this.userRepository.findById(id);
            BigDecimal mucPhuCap=BigDecimal.valueOf(phuCap.getMucPhuCap()*nhanVien.getLuongHienTai().doubleValue()/100);
            nv.setMucTien(mucPhuCap);
        }
        else{
            nv.setMucTien(BigDecimal.valueOf(phuCap.getMucPhuCap()));
        }
        this.nv_allowanceRepository.save(nv);
        return "redirect:/allowance";
    }

    @PostMapping("/allowance/delete/employee")
    public String postDeleteAllowanceEmployee(Model model,@RequestParam("id") Integer id, @RequestParam("type") Integer type){
        this.nv_allowanceRepository.deleteById(new NVPhuCapId(this.userRepository.findById(id), this.allowanceRepository.findById(type).get()));
        return "redirect:/allowance";
    }
}
