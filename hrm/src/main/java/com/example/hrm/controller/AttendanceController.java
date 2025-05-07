package com.example.hrm.controller;

import com.example.hrm.constant.ChamCongStatusEnum;
import com.example.hrm.domain.ChamCong;
import com.example.hrm.domain.NV_ViPham;
import com.example.hrm.domain.NhanVien;
import com.example.hrm.repository.AttendanceRepository;
import com.example.hrm.repository.NV_ViolationRepository;
import com.example.hrm.repository.UserRepository;
import com.example.hrm.repository.ViolationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cglib.core.Local;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class AttendanceController {
    @Autowired
    private AttendanceRepository attendanceRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private NV_ViolationRepository nV_ViPhamRepository;
    @Autowired
    private ViolationRepository violationRepository;
    public double calculateHoursDifference(LocalTime startTime, LocalTime endTime) {
        // Đảm bảo endTime lớn hơn startTime, nếu không thì hoán đổi
        if (endTime.isBefore(startTime)) {
            LocalTime temp = startTime;
            startTime = endTime;
            endTime = temp;
        }

        // Tính Duration
        Duration duration = Duration.between(startTime, endTime);

        // Chuyển đổi sang số giờ (thập phân)
        long seconds = duration.getSeconds();
        double hours = seconds / 3600.0; // Chia cho 3600 để được giờ thập phân

        return hours;
    }
    @GetMapping("/attendance")
    public String getAttendancePage(Model model){
        List<ChamCong> chamCongs = this.attendanceRepository.findAll();
        model.addAttribute("chamCongs",chamCongs);
        List<NhanVien> users = this.userRepository.findAll();
        model.addAttribute("users",users);
        return "admin/attendance/show";
    }
    @PostMapping("/attendance/create")
    public String getAttendanceCreatePage(Model model, @RequestParam("id") Integer id, @RequestParam("ngay") LocalDate ngay
    , @RequestParam("in") LocalTime in){
        ChamCong chamCong=new ChamCong();
        chamCong.setNhanVien(this.userRepository.findById(id));
        chamCong.setNgay(ngay);
        chamCong.setGioVao(in);
        if(chamCong.getGioVao().isAfter(LocalTime.of(8,0))){
            chamCong.setTrangThai(ChamCongStatusEnum.LATE.getValue());
            NV_ViPham nV_ViPham=new NV_ViPham();
            nV_ViPham.setNhanVien(chamCong.getNhanVien());
            nV_ViPham.setViPham(violationRepository.findById(1));
            nV_ViPham.setNgayViPham(LocalDate.now());
            long min=Duration.between(LocalTime.of(8,0),chamCong.getGioVao()).toMinutes();
            nV_ViPham.setMoTa("Đi làm muộn"+min);
            nV_ViPham.setNguoiRaQuyetDinh(this.userRepository.findById(2));
            this.nV_ViPhamRepository.save(nV_ViPham);
        }
        else{
            chamCong.setTrangThai(ChamCongStatusEnum.ONTIME.getValue());
        }
        chamCong.setSoGioTangCa(0.0);
        this.attendanceRepository.save(chamCong);
        return "redirect:/attendance";
    }
    @PostMapping("/attendance/update")
    public String getAttendanceUpdatePage(Model model
    ,@RequestParam("mcc") Integer mcc, @RequestParam("id") Integer id, @RequestParam("ngay") LocalDate ngay
    , @RequestParam("in") LocalTime in, @RequestParam("out") LocalTime out, @RequestParam("status") String status){
        ChamCong chamCong=this.attendanceRepository.findById(mcc).get();
        chamCong.setNhanVien(this.userRepository.findById(id));
        chamCong.setNgay(ngay);
        chamCong.setGioVao(in);
        chamCong.setGioRa(out);
        if(chamCong.getGioVao().isAfter(LocalTime.of(8,0))){
            if(!chamCong.getTrangThai().equals(ChamCongStatusEnum.LATE.getValue())){
                NV_ViPham nV_ViPham=new NV_ViPham();
                nV_ViPham.setNhanVien(chamCong.getNhanVien());
                nV_ViPham.setViPham(violationRepository.findById(1));
                nV_ViPham.setNgayViPham(LocalDate.now());
                long min=Duration.between(chamCong.getGioVao(), chamCong.getGioRa()).toMinutes();
                nV_ViPham.setMoTa("Đi làm muộn"+min);
                nV_ViPham.setNguoiRaQuyetDinh(this.userRepository.findById(2));
                this.nV_ViPhamRepository.save(nV_ViPham);
            }
            chamCong.setTrangThai(ChamCongStatusEnum.LATE.getValue());
        }
        else {
            chamCong.setTrangThai(ChamCongStatusEnum.ONTIME.getValue());
        }
        boolean check=true;
        if(chamCong.getGioRa()==null || chamCong.getGioVao()==null){
            check=false;
        }
        if(check==false){
            chamCong.setTrangThai("Vắng");
        }
        if(chamCong.getGioRa().isBefore(LocalTime.of(17,0)) && check==true){
            chamCong.setSoGioTangCa(calculateHoursDifference(chamCong.getGioVao(), chamCong.getGioRa()));
        }
        this.attendanceRepository.save(chamCong);

        return "redirect:/attendance";
    }
    @PostMapping("/attendance/delete")
    public String getAttendanceDeletePage(Model model, @RequestParam("id") Integer id){
        this.attendanceRepository.deleteById(id);
        return "redirect:/attendance";
    }

    @GetMapping("/attendance/check")
    @ResponseBody
    public Map<String, Object> checkAttendance(@RequestParam("employeeId") Integer employeeId,
                                               @RequestParam("date") LocalDate date) {
        Map<String, Object> response = new HashMap<>();
        ChamCong chamCong = attendanceRepository.findByNhanVienIdAndNgay(employeeId, date);

        if (chamCong != null) {
            response.put("exists", true);
            response.put("attendance", chamCong); // Trả về dữ liệu chấm công
        } else {
            response.put("exists", false);
        }

        return response;
    }
}
