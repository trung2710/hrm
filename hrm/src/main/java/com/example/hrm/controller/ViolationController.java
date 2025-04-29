package com.example.hrm.controller;

import com.example.hrm.domain.ViPham;
import com.example.hrm.repository.ViolationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.util.List;

@Controller
public class ViolationController {
    @Autowired
    private ViolationRepository violationRepository;
    @GetMapping("/violation-type")
    public String getViolationPage(Model model){
        List<ViPham> viPhams = violationRepository.findAll();
        model.addAttribute("viPhams",viPhams);
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

}
