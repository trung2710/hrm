package com.example.hrm.controller;

import com.example.hrm.domain.BaoHiem;
import com.example.hrm.domain.HopDong;
import com.example.hrm.domain.NhanVien;
import com.example.hrm.repository.*;
import com.example.hrm.service.UploadService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

@Controller
public class UserController {
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private UploadService uploadService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ContractRepository contractRepository;
    @Autowired
    private DepartmentRepository departmentRepository;
    @Autowired
    private PositionRepository positionRepository;
    //lien quan den xoa nhan vien
    @Autowired
    private InsuranceRepository insuranceRepository;
    @GetMapping("/user")
    public String getUserPage(Model model){
        List<NhanVien> users=this.userRepository.findAll();
        model.addAttribute("users",users);
        return "admin/employee/show";
    }

    @GetMapping("/user/{id}")
    public String getUserDetail(Model model, @PathVariable Integer id){
        NhanVien nhanVien=this.userRepository.findById(id);
        model.addAttribute("user",nhanVien);
        List<HopDong> hopDongs=this.contractRepository.findAllByNV(nhanVien.getId(), Sort.by(Sort.Direction.DESC, "ngayBatDau"));
        model.addAttribute("hopDongs", hopDongs.isEmpty() ? null : hopDongs.get(0));
        if(hopDongs.isEmpty() == false){
            nhanVien.setLuongHienTai(hopDongs.get(0).getLuongCoBan());
        }

        return "admin/employee/detail";
    }

    @GetMapping("/user/create")
    public String getUserCreatePage(Model model){
        model.addAttribute("newUser",new NhanVien());
        model.addAttribute("cvs", this.positionRepository.findAllPositions());
        return "admin/employee/create";
    }


    @PostMapping("/user/create")
    public String postUserCreate(Model model, @ModelAttribute("newUser") @Valid NhanVien nv, BindingResult newUserBindingResult, @RequestParam("avatarfile") MultipartFile file){
        //ket qua cua qua trinh valid, duoc  lay thong qua blindingresult
        List<FieldError> errors = newUserBindingResult.getFieldErrors();
        for (FieldError error : errors ) {
            System.out.println (">>>"+error.getField() + " - " + error.getDefaultMessage());
        }

        if(newUserBindingResult.hasErrors()){
            return "admin/employee/create";
        }
        String avatarFile=null;
        try {
            avatarFile=this.uploadService.handleSaveUploadFile(file,"upload");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        nv.setAvatar(avatarFile);
        nv.setPassword(this.passwordEncoder.encode(nv.getPassword()));
        nv.setLuongHienTai(BigDecimal.ONE);
        nv.setTrangThai("Đang làm");
        nv.setSoNgayPhep(12);
        this.userRepository.save(nv);

        return "redirect:/user";
    }

    @GetMapping("/user/update/{id}")
    public String getUserUpdatePage(Model model, @PathVariable Integer id){
        NhanVien nhanVien=this.userRepository.findById(id);
        model.addAttribute("newUser",nhanVien);
        model.addAttribute("cvs", this.positionRepository.findAllPositions());
        return "admin/employee/update";
    }

    @PostMapping("/user/update")
    public String postUserUpdate(Model model, @ModelAttribute("newUser") @Valid NhanVien nv, BindingResult newUserBindingResult, @RequestParam("avatarfile") MultipartFile file){
        List<FieldError> errors = newUserBindingResult.getFieldErrors();
        for (FieldError error : errors ) {
            System.out.println (">>>"+error.getField() + " - " + error.getDefaultMessage());
        }

        if(newUserBindingResult.hasErrors()){
            return "admin/employee/update";
        }

        String avatarFile=null;
        try {
            avatarFile=this.uploadService.handleSaveUploadFile(file,"upload");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        NhanVien nhanVien=this.userRepository.findById(nv.getId());
        if(nhanVien!=null){
            nhanVien.setAvatar(avatarFile);
            nhanVien.setCccd(nv.getCccd());
            nhanVien.setHoTen(nv.getHoTen());
            nhanVien.setNgaySinh(nv.getNgaySinh());
            nhanVien.setSoDienThoai(nv.getSoDienThoai());
            nhanVien.setGioiTinh(nv.getGioiTinh());
            nhanVien.setDiaChi(nv.getDiaChi());
            nhanVien.setTrangThai(nv.getTrangThai());
            nhanVien.setSoNgayPhep(nv.getSoNgayPhep());
            nhanVien.setChucVu(nv.getChucVu());
            nhanVien.setThamNien(nv.getThamNien());
            this.userRepository.save(nhanVien);
        }
        return  "redirect:/user";
    }


    @GetMapping("/user/delete/{id}")
    public String getUserDeletePage(Model model, @PathVariable Integer id){
        NhanVien nhanVien=new NhanVien();
        nhanVien.setId(id);
        model.addAttribute("newUser",nhanVien);
        return "admin/employee/delete";
    }

    @PostMapping("/user/delete")
    public String postUserDelete(Model model, @ModelAttribute("newUser") NhanVien nv){
        NhanVien nhanVien=this.userRepository.findById(nv.getId());
        for(BaoHiem x: nhanVien.getBaoHiems()){
            this.insuranceRepository.delete(x);
        }
        //can phai xoa them vi pham, thuong , phu cap, cham cong lien quan dn nhan vien thi moi xoa duoc nhan vien.
        this.userRepository.delete(nhanVien);
        return "redirect:/user";
    }

}
