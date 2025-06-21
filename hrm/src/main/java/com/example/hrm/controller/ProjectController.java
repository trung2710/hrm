package com.example.hrm.controller;

import com.example.hrm.domain.DuAn;
import com.example.hrm.domain.NV_DuAn;
import com.example.hrm.domain.NhanVien;
import com.example.hrm.domain.PhongBan;
import com.example.hrm.domain.idClass.NVDuAnId;
import com.example.hrm.repository.DepartmentRepository;
import com.example.hrm.repository.NV_DuAnRepository;
import com.example.hrm.repository.ProjectReppository;
import com.example.hrm.repository.UserRepository;
import com.example.hrm.service.CustomUserDetail;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.swing.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Controller
public class ProjectController {
    @Autowired
    private ProjectReppository projectReppository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private NV_DuAnRepository nvDuAnRepository;
    @Autowired
    private DepartmentRepository departmentRepository;
    @GetMapping("/project")
    public String getProjectPage(Model model, Authentication authentication){
        CustomUserDetail cus=(CustomUserDetail) authentication.getPrincipal();
        NhanVien nhanVien=cus.getNhanVien();
        if(nhanVien.getChucVu().getQuyen().getTenQuyen().equals("Admin") || nhanVien.getChucVu().getQuyen().getTenQuyen().equals("Manager")){
            List<DuAn> duAns = projectReppository.findAllProjects();
            for(DuAn x : duAns){
                String s="";
                for(NV_DuAn y : x.getListNhanVien()){
                    s+=y.getNhanVien().getEmail()+":"+y.getNhanVien().getHoTen()+":"+y.getVaiTro()+",";
                }
                s=s.substring(0,s.length()-1);
                x.setNhanViens(s);
            }
            List<PhongBan> pb=this.departmentRepository.findAll();
            model.addAttribute("pbs",pb);
            List<NhanVien> nv=this.userRepository.findAll();
            model.addAttribute("employees",nv);
            model.addAttribute("duAns",duAns);
            DuAn duAn = new DuAn();
            model.addAttribute("newDuAn",duAn);
        }
        else{
            List<DuAn> duAns = projectReppository.findAllProjects();
            List<DuAn> da=new ArrayList<>();
            for(DuAn x : duAns){
                for(NV_DuAn  y : x.getListNhanVien()){
                    if(nhanVien.getId().equals(y.getNhanVien().getEmail())){
                        da.add(x);
                        break;
                    }
                }
            }
            for(DuAn x : da){
                String s="";
                for(NV_DuAn y : x.getListNhanVien()){
                    s+=y.getNhanVien().getEmail()+":"+y.getNhanVien().getHoTen()+":"+y.getVaiTro()+",";
                }
                s=s.substring(0,s.length()-1);
                x.setNhanViens(s);
            }
            List<PhongBan> pb=this.departmentRepository.findAll();
            model.addAttribute("pbs",pb);
            List<NhanVien> nv=this.userRepository.findAll();
            model.addAttribute("employees",nv);
            model.addAttribute("duAns",da);
            DuAn duAn = new DuAn();
            model.addAttribute("newDuAn",duAn);
        }

        return "admin/project/show";
    }

    @PostMapping("/project/create")
    public String postCreateProject(Model model , @RequestParam("ten") String ten
    , @RequestParam("kh") String kh, @RequestParam("pb") Integer pb, @RequestParam("tv") String tv
    , @RequestParam("sta") LocalDate sta, @RequestParam("end") LocalDate end
    ,@RequestParam("phi") BigDecimal phi, @RequestParam("ns") BigDecimal ns){
        DuAn duAn=new DuAn();
        duAn.setTenDuAn(ten);
        duAn.setTenKhachHang(kh);
        duAn.setNgayBatDau(sta);
        duAn.setNgayKetThuc(end);
        duAn.setPhongBan(this.departmentRepository.findById(pb).get());
        duAn.setPhiDuAn(phi);
        duAn.setNganSach(ns);
        duAn.setDoanhThu(ns.subtract(phi));
        this.projectReppository.save(duAn);
        String arr[]=tv.split(",");
        for(String x:arr){
            String y[]=x.split(":");
            NhanVien nhanVien=this.userRepository.findByEmail(y[0]);
            NV_DuAn nvDuAn=new NV_DuAn();
            nvDuAn.setNhanVien(nhanVien);
            nvDuAn.setVaiTro(y[1]);
            nvDuAn.setDuAn(duAn);
            this.nvDuAnRepository.save(nvDuAn);
        }
        duAn.setTrangThai("Đang triển khai");
        this.projectReppository.save(duAn);
        return "redirect:/project";
    }

    @PostMapping("/project/update")
    public String postUpdateProject(Model model, @RequestParam("id") Integer id , @RequestParam("ten") String ten
    , @RequestParam("kh") String kh, @RequestParam("tv") String tv
    , @RequestParam("sta") LocalDate sta, @RequestParam("end") LocalDate end
    ,@RequestParam("phi") BigDecimal phi, @RequestParam("ns") BigDecimal ns
    ,@RequestParam("status") String status){
        DuAn duAn=this.projectReppository.findById(id).get();
        duAn.setTenDuAn(ten);
        duAn.setTenKhachHang(kh);
        duAn.setNgayBatDau(sta);
        duAn.setNgayKetThuc(end);
        duAn.setPhiDuAn(phi);
        duAn.setNganSach(ns);
        duAn.setDoanhThu(ns.subtract(phi));
        duAn.setTrangThai(status);
        String arr[]=tv.split("\n");
        for(String x:arr){
            String y[]=x.split("-");
            boolean ok=false;
            for(NV_DuAn z:duAn.getListNhanVien()){
                if(z.getNhanVien().getEmail().equals(y[0].trim())){
                    z.setVaiTro(y[1].trim());
                    this.nvDuAnRepository.save(z);
                    ok=true;
                    break;
                }
            }
            if(ok==false){
                NhanVien nhanVien=this.userRepository.findByEmail(y[0].trim());
                NV_DuAn nvDuAn=new NV_DuAn();
                nvDuAn.setNhanVien(nhanVien);
                nvDuAn.setVaiTro(y[1].trim());
                nvDuAn.setDuAn(duAn);
                this.nvDuAnRepository.save(nvDuAn);
            }

        }
        this.projectReppository.save(duAn);
        return "redirect:/project";
    }

    @PostMapping("/project/delete")
    public String postDeleteProject(Model model, @RequestParam("id") Integer id){
        List<NV_DuAn> nvDuAns=this.nvDuAnRepository.findAll();
        for(NV_DuAn x:nvDuAns){
            if(x.getDuAn()==this.projectReppository.findById(id).get()){
                this.nvDuAnRepository.deleteById(new NVDuAnId( x.getDuAn(),x.getNhanVien()));
            }
        }
        this.projectReppository.deleteById(id);
        return "redirect:/project";
    }

}
