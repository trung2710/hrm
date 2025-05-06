package com.example.hrm.controller;

import com.example.hrm.domain.NV_ViPham;
import com.example.hrm.domain.NhanVien;
import com.example.hrm.domain.ViPham;
import com.example.hrm.domain.idClass.NVViPhamId;
import com.example.hrm.repository.NV_ViolationRepository;
import com.example.hrm.repository.UserRepository;
import com.example.hrm.repository.ViolationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Controller
public class ViolationController {
    @Autowired
    private ViolationRepository violationRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private NV_ViolationRepository nvViolationRepository;
    @GetMapping("/violation-type")
    public String getViolationPage(Model model){
        List<ViPham> viPhams = violationRepository.findAll();
        model.addAttribute("viPhams",viPhams);
        List<NhanVien> nhanVienList = userRepository.findAll();
        model.addAttribute("nhanVienList",nhanVienList);
        List<NV_ViPham> nv_viPhams=this.nvViolationRepository.findAll();
        model.addAttribute("nv_viPhams",nv_viPhams);
        return "admin/violation/show";
    }


    @PostMapping("/violation-type/create")
    public String postCreateViolationType(Model model, @RequestParam("ten") String ten, @RequestParam("hinhThuc") String hinhThuc,
    @RequestParam("soTien") BigDecimal soTien){
        ViPham viPham=new ViPham();
        viPham.setLoaiViPham(ten);
        viPham.setHinhThucPhat(hinhThuc);
        if(soTien!=null){
            viPham.setSoTienPhat(soTien);
        }
        else viPham.setSoTienPhat(BigDecimal.ZERO);
        this.violationRepository.save(viPham);
        return "redirect:/violation-type";
    }

    @PostMapping("/violation-type/update")
    public String postUpdateViolationType(Model model,
    @RequestParam("id") Integer id,
    @RequestParam("ten") String ten,
     @RequestParam("hinhThuc") String hinhThuc,
     @RequestParam("soTien") BigDecimal soTien){
        ViPham viPham=this.violationRepository.findById(id).get();
        viPham.setLoaiViPham(ten);
        viPham.setHinhThucPhat(hinhThuc);
        if(soTien!=null){
            viPham.setSoTienPhat(soTien);
        }
        else viPham.setSoTienPhat(BigDecimal.ZERO);
        this.violationRepository.save(viPham);
        return "redirect:/violation-type";
    }
    @PostMapping("/violation-type/delete")
    public String postDeleteViolationType(Model model, @RequestParam("id") Integer id){
        this.violationRepository.deleteById(id);
        return "redirect:/violation-type";
    }

    @PostMapping("/violation/create")
    public String postCreateViolation(Model model, @RequestParam("id") Integer id, @RequestParam("ma") Integer ma
    ,@RequestParam("date") LocalDate date,@RequestParam("mota") String mota){
        NV_ViPham nv_viPham=new NV_ViPham();
        nv_viPham.setNhanVien(this.userRepository.findById(id));
        nv_viPham.setViPham(this.violationRepository.findById(ma).get());
        nv_viPham.setNgayViPham(date);
        nv_viPham.setMoTa(mota);
        nv_viPham.setNguoiRaQuyetDinh(this.userRepository.findById(id));
        this.nvViolationRepository.save(nv_viPham);
        return "redirect:/violation-type";
    }

    @PostMapping("/violation/update")
    public String postUpdateViolation(Model model, @RequestParam("id") Integer id, @RequestParam("ma") Integer ma
    ,@RequestParam("date") LocalDate date,@RequestParam("mota") String mota){
        NhanVien nhanVien=this.userRepository.findById(id);
        ViPham viPham=this.violationRepository.findById(ma).get();
        NVViPhamId ID=new NVViPhamId(viPham, nhanVien);
        NV_ViPham nv_viPham=this.nvViolationRepository.findById(ID).get();
        nv_viPham.setNgayViPham(date);
        nv_viPham.setMoTa(mota);
        nv_viPham.setNgayRaQuyetDinh(LocalDate.now());
        this.nvViolationRepository.save(nv_viPham);
        return "redirect:/violation-type";
    }
    @PostMapping("/violation/delete")
    public String postDeleteViolation(Model model, @RequestParam("id") Integer id,@RequestParam("ma") Integer ma){
        NhanVien nhanVien=this.userRepository.findById(id);
        ViPham viPham=this.violationRepository.findById(ma).get();
        NVViPhamId ID=new NVViPhamId(viPham, nhanVien);
        this.nvViolationRepository.deleteById(ID);
        return "redirect:/violation-type";
    }

}
