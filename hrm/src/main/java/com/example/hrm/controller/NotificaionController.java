package com.example.hrm.controller;

import com.example.hrm.domain.ThongBao;
import com.example.hrm.repository.NotificationRepository;
import com.example.hrm.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
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
    public String getNotificationPage(Model model){
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
        model.addAttribute("thongBaoList",thongBaoList);
        return "admin/notification/show";
    }

    @PostMapping("/notification/create")
    public String createNotification(Model model, @RequestParam("title") String title, @RequestParam("content") String content
    ,@RequestParam("pvi") String pvi, @RequestParam(value="dt", required = false) String doiTuong){
        ThongBao thongBao=new ThongBao();
        thongBao.setTieuDe(title);
        thongBao.setNoiDung(content);
        thongBao.setPhamVi(pvi);
        thongBao.setDoiTuong(doiTuong);
        thongBao.setNguoiTao(this.userRepository.findById(1));
        this.notificationRepository.save(thongBao);
        return "redirect:/notification";
    }

    @PostMapping("/notification/delete")
    public String deleteNotification(Model model, @RequestParam("id") Integer id){
        this.notificationRepository.deleteById(id);
        return "redirect:/notification";
    }
}
