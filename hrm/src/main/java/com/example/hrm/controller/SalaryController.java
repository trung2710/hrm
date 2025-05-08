package com.example.hrm.controller;

import com.example.hrm.domain.*;
import com.example.hrm.repository.AttendanceRepository;
import com.example.hrm.repository.ContractRepository;
import com.example.hrm.repository.SalaryRepository;
import com.example.hrm.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.List;

@Controller
public class SalaryController {
    @Autowired
    private SalaryRepository salaryRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private AttendanceRepository attendanceRepository;
    @Autowired
    private ContractRepository contractRepository;

    @GetMapping("/salary")
    public String getSalaryPage(Model model) {
        List<Luong> luongs = salaryRepository.findAll();
        model.addAttribute("luongs", luongs);
        for (Luong luong : luongs) {
            try {
                int month = luong.getThang();
                int year = luong.getNam();
                NhanVien user = luong.getNhanVien();

                // Lấy hợp đồng mới nhất
                List<HopDong> hopDongs = this.contractRepository.findAllByNV(user.getId(), Sort.by(Sort.Direction.DESC, "ngayBatDau"));
                if (hopDongs == null || hopDongs.isEmpty()) {
                    throw new IllegalArgumentException("Không tìm thấy hợp đồng cho nhân viên: " + user.getHoTen());
                }
                HopDong hopDong = hopDongs.get(0);

                Double luongCoBan = hopDong.getLuongCoBan() != null ? hopDong.getLuongCoBan().doubleValue() : 0.0;
                if (luongCoBan <= 0) {
                    throw new IllegalArgumentException("Lương cơ bản không hợp lệ cho nhân viên: " + user.getHoTen());
                }

                Double hour = luongCoBan / 26.0 / 8.0;
                Double tangCa = hour + (hour + 20000.0);

                // Tính toán chấm công
                List<ChamCong> chamCongs = this.attendanceRepository.findByMonthYear(user.getId(), month, year);
                Double sum4 = 0.0;
                int dem = 0;
                for (ChamCong chamCong : chamCongs) {
                    if (!chamCong.getTrangThai().equals("Vắng")) {
                        dem += 1;
                        sum4 += chamCong.getSoGioTangCa();
                    }
                }
                luong.setTienTangCa(BigDecimal.valueOf(sum4).multiply(BigDecimal.valueOf(tangCa)));

                // Tính toán thưởng
                List<NV_Thuong> thuongs = user.getThuongs();
                BigDecimal sum1 = BigDecimal.ZERO;
                for (NV_Thuong thuong : thuongs) {
                    if (thuong.getNgayThuong().getMonthValue() == month && thuong.getNgayThuong().getYear() == year) {
                        sum1 = sum1.add(thuong.getMucTien());
                    }
                }
                luong.setTienThuong(sum1);

                // Tính toán phụ cấp
                List<NV_PhuCap> phuCaps = user.getPhuCaps();
                BigDecimal sum2 = BigDecimal.ZERO;
                for (NV_PhuCap phuCap : phuCaps) {
                    if (phuCap.getPhuCap().getLoaiPhuCap().equals("Có thời hạn")) {
                        if (phuCap.getNgayBatDau().getMonthValue() <= month && phuCap.getNgayBatDau().getYear() <= year
                                && phuCap.getNgayKetThuc().getMonthValue() >= month && phuCap.getNgayKetThuc().getYear() >= year) {
                            sum2 = sum2.add(phuCap.getMucTien());
                        }
                    } else {
                        sum2 = sum2.add(phuCap.getMucTien());
                    }
                }
                luong.setTienPC(sum2);

                // Tính toán vi phạm
                List<NV_ViPham> viPhams = user.getViPhams();
                BigDecimal sum3 = BigDecimal.ZERO;
                for (NV_ViPham viPham : viPhams) {
                    if (viPham.getNgayViPham().getMonthValue() == month && viPham.getNgayViPham().getYear() == year) {
                        sum3 = sum3.add(viPham.getViPham().getSoTienPhat());
                    }
                }
                luong.setTienViPham(sum3);

                // Tính thu nhập
                BigDecimal thuNhap = BigDecimal.ZERO;
                thuNhap = thuNhap.add(sum1).add(sum2).subtract(sum3).add(BigDecimal.valueOf(sum4 * tangCa));
                thuNhap=thuNhap.add(user.getLuongHienTai());
                BigDecimal baoHiem = thuNhap.multiply(BigDecimal.valueOf(0.105));
                BigDecimal thuNhapTinhThue = thuNhap.subtract(baoHiem).subtract(BigDecimal.valueOf(11000000));
                if (thuNhapTinhThue.compareTo(BigDecimal.ZERO) < 0) {
                    thuNhapTinhThue = BigDecimal.ZERO;
                }
                BigDecimal thueThuNhap = thuNhapTinhThue.multiply(BigDecimal.valueOf(0.05));
                luong.setThueThuNhap(thueThuNhap);

                thuNhap = thuNhap.subtract(thueThuNhap).subtract(baoHiem);
                luong.setTongThuNhap(thuNhap);

                // Lưu lại lương
                if (thuNhap.compareTo(BigDecimal.ZERO) >= 0) {
                    this.salaryRepository.save(luong);
                } else {
                    throw new IllegalArgumentException("Thu nhập không hợp lệ cho nhân viên: " + user.getHoTen());
                }
            } catch (Exception e) {
                System.err.println("Lỗi khi tính toán lương cho nhân viên: " + luong.getNhanVien().getHoTen());
                e.printStackTrace();
            }
        }
        List<NhanVien> users = userRepository.findAll();
        model.addAttribute("users", users);
        return "admin/salary/show";
    }

    @PostMapping("/salary/create")
    public String createSalary(RedirectAttributes redirectAttributes, @RequestParam("id") Integer id,
                               @RequestParam("thang") Integer thang, @RequestParam("nam") Integer nam) {
        try {
            Luong luong = new Luong();
            List<Luong> luongs = this.salaryRepository.findAll();

            for (Luong x : luongs) {
                if (x.getThang() == thang && x.getNhanVien().getId() == id && x.getNam() == nam) {
                    redirectAttributes.addFlashAttribute("mess", "Bảng lương đã tồn tại cho tháng " + thang + "/" + nam);
                    return "redirect:/salary";
                }
            }

            luong.setNam(nam);
            luong.setThang(thang);
            NhanVien user = this.userRepository.findById(id);
            luong.setNhanVien(user);
            luong.setThueThuNhap(BigDecimal.ZERO);
            luong.setTongThuNhap(BigDecimal.ZERO);
            this.salaryRepository.save(luong);

            // Tính toán lương (gọi lại logic tính toán ở trên nếu cần)
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mess", "Lỗi khi tạo bảng lương: " + e.getMessage());
        }
        return "redirect:/salary";
    }

    @PostMapping("/salary/delete")
    public String deleteSalary(Model model, @RequestParam("id") Integer id) {
        this.salaryRepository.deleteById(id);
        return "redirect:/salary";
    }
}