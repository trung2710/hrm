package com.example.hrm.controller;

import com.example.hrm.domain.NghiPhep;
import com.example.hrm.domain.NhanVien;
import com.example.hrm.repository.LeaveRepository;
import com.example.hrm.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cglib.core.Local;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Controller
public class LeaveController {
    @Autowired
    private LeaveRepository leaveRepository;
    @Autowired
    private UserRepository userRepository;
    @GetMapping("/leave")
    public String getLeavePage(Model model){
        List<NghiPhep> nghiPheps = leaveRepository.findAll();
        for(NghiPhep nghiPhep:nghiPheps){
            nghiPhep.setSoNgay(ChronoUnit.DAYS.between(nghiPhep.getNgayBatDau(), nghiPhep.getNgayKetThuc())+1);
            this.leaveRepository.save(nghiPhep);
        }
        List<NhanVien> nhanVienList=this.userRepository.findAll();
        model.addAttribute("nhanVienList",nhanVienList);
        model.addAttribute("nghiPheps",nghiPheps);
        return "admin/leave/show";
    }

    @PostMapping("leave/update")
    public String postUpdateLeave(Model model, @RequestParam("id") String id, @RequestParam("action") String action){
        NghiPhep nghiPhep=this.leaveRepository.findById(Integer.parseInt(id));
        if(action.equals("ok")){
            nghiPhep.setTrangThaiPheDuyet("Đã duyệt");
        }
        else{
            nghiPhep.setTrangThaiPheDuyet("Từ chối");
        }
        this.leaveRepository.save(nghiPhep);
        return "redirect:/leave";
    }

    @PostMapping("/leave/create")
    public String postCreateLeave(RedirectAttributes redirectAttributes, @RequestParam("id") Integer id
    , @RequestParam("reason") String reason, @RequestParam("sta") LocalDate sta, @RequestParam("end") LocalDate end){
        NghiPhep nghiPhep=new NghiPhep();
        Long ngay=ChronoUnit.DAYS.between(sta,end)+1;
        NhanVien nhanVien=this.userRepository.findById(id);
        if(nhanVien.getSoNgayPhep()>=ngay){
            Long s=nhanVien.getSoNgayPhep()-ngay;
            nhanVien.setSoNgayPhep(Integer.parseInt(s.toString()));
            nghiPhep.setNhanVien(nhanVien);
            nghiPhep.setLiDo(reason);
            nghiPhep.setNgayBatDau(sta);
            nghiPhep.setNgayKetThuc(end);
            this.leaveRepository.save(nghiPhep);
            String mess="Da tao thanh cong 1 nghi phep";
            redirectAttributes.addFlashAttribute("mess",mess);
        }
        else{
            String mess="Ban da het so ngay nghi , ban khong the nghi phep nua.";
            redirectAttributes.addFlashAttribute("mess",mess);
        }
        return "redirect:/leave";
    }
}
