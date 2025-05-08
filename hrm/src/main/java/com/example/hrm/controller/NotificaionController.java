package com.example.hrm.controller;

import com.example.hrm.domain.NhanVien;
import com.example.hrm.domain.ThongBao;
import com.example.hrm.repository.NotificationRepository;
import com.example.hrm.repository.UserRepository;
import com.example.hrm.service.CustomUserDetail;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Arrays;
import java.util.List;

@Controller
public class NotificaionController {
    @Autowired
    private NotificationRepository notificationRepository;
    @Autowired
    private UserRepository userRepository;
    @GetMapping("/notification")
    public String getNotificationPage(Model model,Authentication authentication){
        CustomUserDetail cus=(CustomUserDetail) authentication.getPrincipal();
        NhanVien nhanVien=cus.getNhanVien();
        List<ThongBao> thongBaoList = notificationRepository.findAll();
        for(ThongBao x : thongBaoList){
            if(x.getPhamVi().equals("PhongBan")){
                List<String> list = Arrays.asList(x.getDoiTuong().split(","));
                x.setListDoiTuong(list);
            }
            else if( x.getPhamVi().equals("CaNhan")){
                List<String> list = Arrays.asList(x.getDoiTuong().split(","));
                List<String> newList=new java.util.ArrayList<>();
                for(String y : list){
                    newList.add(this.userRepository.findById(Integer.parseInt(y)).getHoTen());
                }
                x.setListDoiTuong(newList);
            }
            this.notificationRepository.save(x);
        }
        if(nhanVien.getChucVu().getQuyen().getTenQuyen().equals("Admin") || nhanVien.getChucVu().getQuyen().getTenQuyen().equals("Manager")){
            model.addAttribute("thongBaoList",thongBaoList);
        }
        else{
            List<ThongBao> thongBaoList1=new java.util.ArrayList<>();
            for(ThongBao x : thongBaoList){
               if(x.getPhamVi().equals("PhongBan") && nhanVien.getChucVu().getPhongBan().getTenPhongBan().contains(x.getDoiTuong()) || x.getPhamVi().equals("ToanCongTy")){
                    thongBaoList1.add(x);
               }
               else if(x.getPhamVi().equals("CaNhan") && nhanVien.getId().toString().contains(x.getDoiTuong())){
                   thongBaoList1.add(x);
               }
            }
            model.addAttribute("thongBaoList",thongBaoList1);
        }
        return "admin/notification/show";
    }

    @PostMapping("/notification/create")
    public String createNotification(Model model, @RequestParam("title") String title, @RequestParam("content") String content
    ,@RequestParam("pvi") String pvi, @RequestParam(value="dt", required = false) String doiTuong, Authentication authentication){
        CustomUserDetail cus=(CustomUserDetail) authentication.getPrincipal();
        NhanVien nhanVien=cus.getNhanVien();
        ThongBao thongBao=new ThongBao();
        thongBao.setTieuDe(title);
        thongBao.setNoiDung(content);
        thongBao.setPhamVi(pvi);
        thongBao.setDoiTuong(doiTuong);
        thongBao.setNguoiTao(nhanVien);
        this.notificationRepository.save(thongBao);
        return "redirect:/notification";
    }

    @PostMapping("/notification/delete")
    public String deleteNotification(Model model, @RequestParam("id") Integer id){
        this.notificationRepository.deleteById(id);
        return "redirect:/notification";
    }
}
