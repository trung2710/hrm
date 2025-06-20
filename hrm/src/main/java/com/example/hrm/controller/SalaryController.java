package com.example.hrm.controller;

import com.example.hrm.domain.*;
import com.example.hrm.repository.*;
import com.example.hrm.service.CustomUserDetail;
import com.example.hrm.specification.SalarySpec;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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
    @Autowired
    private DepartmentRepository departmentRepository;

    @GetMapping("/salary")
    public String getSalaryPage(Model model, Authentication authentication, @RequestParam("page") Optional<String> p
    ,@RequestParam("ten") Optional<String> ten, @RequestParam("pb")  Optional<String> pb
    ,@RequestParam("month") Optional<String> m, @RequestParam("year") Optional<String> y) {
        int page=1;
        try{
            page=Integer.parseInt(p.get());
        }catch(Exception e){
            //
        }
        String tenParam=ten.orElse(null);
        String pbParam=pb.orElse(null);
        String  mParam=m.orElse(null);
        String yParam=y.orElse(null);
        Pageable pageable= PageRequest.of(page-1, 20);
        Page<Luong> ls=this.salaryRepository.findAll(SalarySpec.findByCriteria(tenParam, mParam, yParam, pbParam), pageable);
        CustomUserDetail cus=(CustomUserDetail) authentication.getPrincipal();
        NhanVien nhanVien=cus.getNhanVien();
        List<Luong> luongs = ls.getContent();
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
        if(nhanVien.getChucVu().getQuyen().getTenQuyen().equals("Admin") || nhanVien.getChucVu().getQuyen().getTenQuyen().equals("Manager")){
            model.addAttribute("luongs", luongs);
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages",ls.getTotalPages());
            List<PhongBan> pbs=this.departmentRepository.findAll();
            model.addAttribute("pbs",pbs);
        }
        else{
            List<Luong> luongs1=new java.util.ArrayList<>();
            for(Luong luong:luongs){
                if(luong.getNhanVien().getId().equals(nhanVien.getId())){
                    luongs1.add(luong);
                }
            }
            model.addAttribute("luongs", luongs1);
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

    @GetMapping("/export/salary/excel")
    public void exportSalaryExcel(
            @RequestParam(required = false) String ten,
            @RequestParam(required = false) String pb,
            @RequestParam(required = false) String month,
            @RequestParam(required = false) String year,
            HttpServletResponse response) throws IOException {

        System.out.println("Filters: ten=" + ten + ", pb=" + pb + ", month=" + month + ", year=" + year);

        String tenParam = (ten != null && !ten.isEmpty()) ? ten : null;
        String pbParam = (pb != null && !pb.isEmpty()) ? pb : null;
        String mParam = (month != null && !month.isEmpty()) ? month : null;
        String yParam = (year != null && !year.isEmpty()) ? year : null;

        // Get ALL salary records (no pagination)
        Page<Luong> allSalaryPage = salaryRepository.findAll(
                SalarySpec.findByCriteria(tenParam, mParam, yParam, pbParam),
                Pageable.unpaged()
        );
        List<Luong> allSalaries = allSalaryPage.getContent();

        // Recalculate salary data
        for (Luong luong : allSalaries) {
            try {
                calculateSalaryDetails(luong);
            } catch (Exception e) {
                System.err.println("Error calculating salary for employee: " + luong.getNhanVien().getHoTen());
            }
        }

        exportSalaryToExcel(allSalaries, response, tenParam, pbParam, mParam, yParam);
    }

    // ✅ EXPORT PDF (HTML FORMAT)
    @GetMapping("/export/salary/pdf")
    public void exportSalaryPdf(
            @RequestParam(required = false) String ten,
            @RequestParam(required = false) String pb,
            @RequestParam(required = false) String month,
            @RequestParam(required = false) String year,
            HttpServletResponse response) throws IOException {

        System.out.println("User trung2710 requesting salary PDF export at 2025-06-20 00:55:29 UTC");
        System.out.println("Filters: ten=" + ten + ", pb=" + pb + ", month=" + month + ", year=" + year);

        String tenParam = (ten != null && !ten.isEmpty()) ? ten : null;
        String pbParam = (pb != null && !pb.isEmpty()) ? pb : null;
        String mParam = (month != null && !month.isEmpty()) ? month : null;
        String yParam = (year != null && !year.isEmpty()) ? year : null;

        // Get ALL salary records
        Page<Luong> allSalaryPage = salaryRepository.findAll(
                SalarySpec.findByCriteria(tenParam, mParam, yParam, pbParam),
                Pageable.unpaged()
        );
        List<Luong> allSalaries = allSalaryPage.getContent();

        // Recalculate salary data
        for (Luong luong : allSalaries) {
            try {
                calculateSalaryDetails(luong);
            } catch (Exception e) {
                System.err.println("Error calculating salary for employee: " + luong.getNhanVien().getHoTen());
            }
        }

        exportSalaryToPdf(allSalaries, response, tenParam, pbParam, mParam, yParam);
    }

    // ✅ HELPER METHOD: CALCULATE SALARY DETAILS
    private void calculateSalaryDetails(Luong luong) throws Exception {
        int month = luong.getThang();
        int year = luong.getNam();
        NhanVien user = luong.getNhanVien();

        // Get latest contract
        List<HopDong> hopDongs = this.contractRepository.findAllByNV(user.getId(), Sort.by(Sort.Direction.DESC, "ngayBatDau"));
        if (hopDongs == null || hopDongs.isEmpty()) {
            throw new IllegalArgumentException("No contract found for employee: " + user.getHoTen());
        }
        HopDong hopDong = hopDongs.get(0);

        Double luongCoBan = hopDong.getLuongCoBan() != null ? hopDong.getLuongCoBan().doubleValue() : 0.0;
        if (luongCoBan <= 0) {
            throw new IllegalArgumentException("Invalid basic salary for employee: " + user.getHoTen());
        }

        Double hour = luongCoBan / 26.0 / 8.0;
        Double tangCa = hour + (hour + 20000.0);

        // Calculate attendance
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

        // Calculate bonus
        List<NV_Thuong> thuongs = user.getThuongs();
        BigDecimal sum1 = BigDecimal.ZERO;
        for (NV_Thuong thuong : thuongs) {
            if (thuong.getNgayThuong().getMonthValue() == month && thuong.getNgayThuong().getYear() == year) {
                sum1 = sum1.add(thuong.getMucTien());
            }
        }
        luong.setTienThuong(sum1);

        // Calculate allowance
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

        // Calculate violations
        List<NV_ViPham> viPhams = user.getViPhams();
        BigDecimal sum3 = BigDecimal.ZERO;
        for (NV_ViPham viPham : viPhams) {
            if (viPham.getNgayViPham().getMonthValue() == month && viPham.getNgayViPham().getYear() == year) {
                sum3 = sum3.add(viPham.getViPham().getSoTienPhat());
            }
        }
        luong.setTienViPham(sum3);

        // Calculate total income
        BigDecimal thuNhap = BigDecimal.ZERO;
        thuNhap = thuNhap.add(sum1).add(sum2).subtract(sum3).add(BigDecimal.valueOf(sum4 * tangCa));
        thuNhap = thuNhap.add(user.getLuongHienTai());
        BigDecimal baoHiem = thuNhap.multiply(BigDecimal.valueOf(0.105));
        BigDecimal thuNhapTinhThue = thuNhap.subtract(baoHiem).subtract(BigDecimal.valueOf(11000000));
        if (thuNhapTinhThue.compareTo(BigDecimal.ZERO) < 0) {
            thuNhapTinhThue = BigDecimal.ZERO;
        }
        BigDecimal thueThuNhap = thuNhapTinhThue.multiply(BigDecimal.valueOf(0.05));
        luong.setThueThuNhap(thueThuNhap);

        thuNhap = thuNhap.subtract(thueThuNhap).subtract(baoHiem);
        luong.setTongThuNhap(thuNhap);

        if (thuNhap.compareTo(BigDecimal.ZERO) >= 0) {
            this.salaryRepository.save(luong);
        }
    }

    private BigDecimal safeGetBigDecimal(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }

    private BigDecimal safeGetBigDecimal(BigDecimal value, String fieldName, String employeeName) {
        if (value == null) {
            System.out.printf("⚠️ Warning: %s is NULL for employee %s, using 0%n", fieldName, employeeName);
            return BigDecimal.ZERO;
        }
        return value;
    }

    private void exportSalaryToExcel(List<Luong> salaries, HttpServletResponse response,
                                     String ten, String pb, String month, String year) throws IOException {

        response.setContentType("text/csv; charset=UTF-8");
        String filename = String.format("salary_report_%s.csv",
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")));
        response.setHeader("Content-Disposition", "attachment; filename=" + filename);

        PrintWriter writer = response.getWriter();
        writer.write("\uFEFF"); // UTF-8 BOM for Excel

        // Title
        writer.println("BÁO CÁO BẢNG LƯƠNG");
        writer.println("");

        // Filter info
        StringBuilder filterInfo = new StringBuilder("Điều kiện lọc: ");
        if (ten != null && !ten.isEmpty()) {
            filterInfo.append("Tìm kiếm: ").append(ten).append(" | ");
        }
        if (pb != null && !pb.isEmpty()) {
            filterInfo.append("Phòng ban: ").append(pb).append(" | ");
        }
        if (month != null && !month.isEmpty()) {
            filterInfo.append("Tháng: ").append(month).append(" | ");
        }
        if (year != null && !year.isEmpty()) {
            filterInfo.append("Năm: ").append(year).append(" | ");
        }
        if (filterInfo.toString().equals("Điều kiện lọc: ")) {
            filterInfo.append("Tất cả bảng lương");
        } else {
            filterInfo.setLength(filterInfo.length() - 3);
        }
        writer.println(filterInfo.toString());
        writer.println("Thời gian: "+LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) +"UTC | Tổng: " + salaries.size() + " bản ghi");
        writer.println("");

        // Header
        writer.println("STT,Mã bảng lương,Mã NV,Họ tên,Phòng ban,Chức vụ,Tháng/Năm,Lương cơ bản,Phụ cấp,Thưởng,Vi phạm,Tăng ca,Thuế TNCN,Bảo hiểm,Tổng nhận");

        // ✅ KHỞI TẠO CÁC BIẾN TỔNG
        int index = 1;
        BigDecimal totalLuongCoBan = BigDecimal.ZERO;
        BigDecimal totalPhuCap = BigDecimal.ZERO;
        BigDecimal totalThuong = BigDecimal.ZERO;
        BigDecimal totalViPham = BigDecimal.ZERO;
        BigDecimal totalTangCa = BigDecimal.ZERO;
        BigDecimal totalThue = BigDecimal.ZERO;
        BigDecimal totalBaoHiem = BigDecimal.ZERO;
        BigDecimal grandTotal = BigDecimal.ZERO;

        // Data
        for (Luong luong : salaries) {
            String phongBan = luong.getNhanVien().getChucVu().getPhongBan().getTenPhongBan();
            String chucVu = luong.getNhanVien().getChucVu().getTenChucVu();
            String hoTen = luong.getNhanVien().getHoTen();

            String employeeName = luong.getNhanVien().getHoTen();
            BigDecimal luongCoBan = safeGetBigDecimal(luong.getNhanVien().getLuongHienTai(), "LuongHienTai", employeeName);
            BigDecimal tienPC = safeGetBigDecimal(luong.getTienPC(), "TienPC", employeeName);
            BigDecimal tienThuong = safeGetBigDecimal(luong.getTienThuong(), "TienThuong", employeeName);
            BigDecimal tienViPham = safeGetBigDecimal(luong.getTienViPham(), "TienViPham", employeeName);
            BigDecimal tienTangCa = safeGetBigDecimal(luong.getTienTangCa(), "TienTangCa", employeeName);
            BigDecimal thueThuNhap = safeGetBigDecimal(luong.getThueThuNhap(), "ThueThuNhap", employeeName);
            BigDecimal tongThuNhap = safeGetBigDecimal(luong.getTongThuNhap(), "TongThuNhap", employeeName);

            // Calculate insurance (10.5% of total income before tax)
            BigDecimal totalBeforeTax = luongCoBan.add(tienPC).add(tienThuong).subtract(tienViPham).add(tienTangCa);
            BigDecimal baoHiem = totalBeforeTax.multiply(BigDecimal.valueOf(0.105));

            // ✅ CỘNG DỒN VÀO TỔNG
            totalLuongCoBan = totalLuongCoBan.add(luongCoBan);
            totalPhuCap = totalPhuCap.add(tienPC);
            totalThuong = totalThuong.add(tienThuong);
            totalViPham = totalViPham.add(tienViPham);
            totalTangCa = totalTangCa.add(tienTangCa);
            totalThue = totalThue.add(thueThuNhap);
            totalBaoHiem = totalBaoHiem.add(baoHiem);
            grandTotal = grandTotal.add(tongThuNhap);

            writer.printf("%d,\"%d\",\"%d\",\"%s\",\"%s\",\"%s\",\"%d/%d\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\"%n",
                    index++,
                    luong.getId(),
                    luong.getNhanVien().getId(),
                    hoTen.replace("\"", "\"\""),
                    phongBan.replace("\"", "\"\""),
                    chucVu.replace("\"", "\"\""),
                    luong.getThang(),
                    luong.getNam(),
                    String.format("%,d", luongCoBan.intValue()),
                    String.format("%,d", tienPC.intValue()),
                    String.format("%,d", tienThuong.intValue()),
                    String.format("%,d", tienViPham.intValue()),
                    String.format("%,d", tienTangCa.intValue()),
                    String.format("%,d", thueThuNhap.intValue()),
                    String.format("%,d", baoHiem.intValue()),
                    String.format("%,d", tongThuNhap.intValue())
            );
        }

        // ✅ THÊM DÒNG TỔNG
        writer.println("");
        writer.printf("\"TỔNG CỘNG (%d bản ghi)\",\"\",\"\",\"\",\"\",\"\",\"\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\"%n",
                salaries.size(),
                String.format("%,d", totalLuongCoBan.intValue()),
                String.format("%,d", totalPhuCap.intValue()),
                String.format("%,d", totalThuong.intValue()),
                String.format("%,d", totalViPham.intValue()),
                String.format("%,d", totalTangCa.intValue()),
                String.format("%,d", totalThue.intValue()),
                String.format("%,d", totalBaoHiem.intValue()),
                String.format("%,d", grandTotal.intValue())
        );


        writer.println("");
        writer.println("--- HẾT BÁO CÁO ---");

        writer.flush();
        System.out.println("Enhanced salary CSV export completed" + salaries.size() + " records with totals");
    }

    private void exportSalaryToPdf(List<Luong> salaries, HttpServletResponse response,
                                   String ten, String pb, String month, String year) throws IOException {

        response.setContentType("text/html; charset=UTF-8");
        String filename = String.format("salary_report_%s.html",
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")));
        response.setHeader("Content-Disposition", "attachment; filename=" + filename);

        PrintWriter writer = response.getWriter();
        String currentTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        writer.println("<!DOCTYPE html>");
        writer.println("<html><head>");
        writer.println("<meta charset='UTF-8'>");
        writer.println("<title>Báo cáo bảng lương - User trung2710</title>");
        writer.println("<style>");
        writer.println("body { font-family: Arial, sans-serif; margin: 15px; font-size: 11px; }");
        writer.println("table { width: 100%; border-collapse: collapse; margin-top: 20px; }");
        writer.println("th, td { border: 1px solid #ddd; padding: 4px; text-align: left; }");
        writer.println("th { background-color: #f2f2f2; font-weight: bold; text-align: center; font-size: 10px; }");
        writer.println(".header { text-align: center; margin-bottom: 20px; }");
        writer.println(".number { text-align: right; }");
        writer.println(".center { text-align: center; }");
        writer.println(".total-row { background-color: #e8f4f8; font-weight: bold; }");
        writer.println("@media print { body { margin: 0; } }");
        writer.println("</style>");
        writer.println("<script>window.onload = function() { setTimeout(function() { window.print(); }, 1000); };</script>");
        writer.println("</head><body>");

        // Header
        writer.println("<div class='header'>");
        writer.println("<h1>BÁO CÁO BẢNG LƯƠNG</h1>");

        StringBuilder filterInfo = new StringBuilder("Điều kiện lọc: ");
        if (ten != null && !ten.isEmpty()) {
            filterInfo.append("Tìm kiếm: '").append(ten).append("' | ");
        }
        if (pb != null && !pb.isEmpty()) {
            filterInfo.append("Phòng ban: '").append(pb).append("' | ");
        }
        if (month != null && !month.isEmpty()) {
            filterInfo.append("Tháng: '").append(month).append("' | ");
        }
        if (year != null && !year.isEmpty()) {
            filterInfo.append("Năm: '").append(year).append("' | ");
        }
        if (filterInfo.toString().equals("Điều kiện lọc: ")) {
            filterInfo.append("Tất cả bảng lương");
        } else {
            filterInfo.setLength(filterInfo.length() - 3);
        }
        writer.println("<p>" + filterInfo.toString() + "</p>");
        writer.println("<p>Thời gian: " + currentTime + " UTC | Tổng: " + salaries.size() + " bản ghi</p>");
        writer.println("</div>");

        // ✅ TABLE VỚI CỘT BẢO HIỂM
        writer.println("<table>");
        writer.println("<thead><tr>");
        writer.println("<th style='width: 3%;'>STT</th>");
        writer.println("<th style='width: 4%;'>Mã BL</th>");
        writer.println("<th style='width: 4%;'>Mã NV</th>");
        writer.println("<th style='width: 12%;'>Họ tên</th>");
        writer.println("<th style='width: 9%;'>Phòng ban</th>");
        writer.println("<th style='width: 8%;'>Chức vụ</th>");
        writer.println("<th style='width: 6%;'>Tháng/Năm</th>");
        writer.println("<th style='width: 7%;'>Lương CB</th>");
        writer.println("<th style='width: 6%;'>Phụ cấp</th>");
        writer.println("<th style='width: 6%;'>Thưởng</th>");
        writer.println("<th style='width: 6%;'>Vi phạm</th>");
        writer.println("<th style='width: 6%;'>Tăng ca</th>");
        writer.println("<th style='width: 6%;'>Thuế</th>");
        writer.println("<th style='width: 7%;'>Bảo hiểm</th>"); // ✅ THÊM CỘT BẢO HIỂM
        writer.println("<th style='width: 9%;'>Tổng nhận</th>");
        writer.println("</tr></thead><tbody>");

        int index = 1;
        BigDecimal grandTotal = BigDecimal.ZERO;
        // ✅ THÊM CÁC BIẾN TỔNG CHO BẢO HIỂM
        BigDecimal totalLuongCoBan = BigDecimal.ZERO;
        BigDecimal totalPhuCap = BigDecimal.ZERO;
        BigDecimal totalThuong = BigDecimal.ZERO;
        BigDecimal totalViPham = BigDecimal.ZERO;
        BigDecimal totalTangCa = BigDecimal.ZERO;
        BigDecimal totalThue = BigDecimal.ZERO;
        BigDecimal totalBaoHiem = BigDecimal.ZERO;

        for (Luong luong : salaries) {
            String phongBan = luong.getNhanVien().getChucVu().getPhongBan().getTenPhongBan();
            String chucVu = luong.getNhanVien().getChucVu().getTenChucVu();
            String hoTen = luong.getNhanVien().getHoTen();
            String employeeName = luong.getNhanVien().getHoTen();
            BigDecimal luongCoBan = safeGetBigDecimal(luong.getNhanVien().getLuongHienTai(), "LuongHienTai", employeeName);
            BigDecimal tienPC = safeGetBigDecimal(luong.getTienPC(), "TienPC", employeeName);
            BigDecimal tienThuong = safeGetBigDecimal(luong.getTienThuong(), "TienThuong", employeeName);
            BigDecimal tienViPham = safeGetBigDecimal(luong.getTienViPham(), "TienViPham", employeeName);
            BigDecimal tienTangCa = safeGetBigDecimal(luong.getTienTangCa(), "TienTangCa", employeeName);
            BigDecimal thueThuNhap = safeGetBigDecimal(luong.getThueThuNhap(), "ThueThuNhap", employeeName);
            BigDecimal tongThuNhap = safeGetBigDecimal(luong.getTongThuNhap(), "TongThuNhap", employeeName);


            // ✅ TÍNH BẢO HIỂM (10.5% OF TOTAL INCOME BEFORE TAX)
            BigDecimal totalBeforeTax = luongCoBan.add(tienPC).add(tienThuong).subtract(tienViPham).add(tienTangCa);
            BigDecimal baoHiem = totalBeforeTax.multiply(BigDecimal.valueOf(0.105));

            // ✅ CỘNG DỒN VÀO TỔNG
            grandTotal = grandTotal.add(tongThuNhap);
            totalLuongCoBan = totalLuongCoBan.add(luongCoBan);
            totalPhuCap = totalPhuCap.add(tienPC);
            totalThuong = totalThuong.add(tienThuong);
            totalViPham = totalViPham.add(tienViPham);
            totalTangCa = totalTangCa.add(tienTangCa);
            totalThue = totalThue.add(thueThuNhap);
            totalBaoHiem = totalBaoHiem.add(baoHiem);

            writer.println("<tr>");
            writer.printf("<td class='center'>%d</td>", index++);
            writer.printf("<td class='center'>%d</td>", luong.getId());
            writer.printf("<td class='center'>%d</td>", luong.getNhanVien().getId());
            writer.printf("<td>%s</td>", hoTen);
            writer.printf("<td>%s</td>", phongBan);
            writer.printf("<td>%s</td>", chucVu);
            writer.printf("<td class='center'>%d/%d</td>", luong.getThang(), luong.getNam());
            writer.printf("<td class='number'>%,d</td>", luongCoBan.intValue());
            writer.printf("<td class='number'>%,d</td>", tienPC.intValue());
            writer.printf("<td class='number'>%,d</td>", tienThuong.intValue());
            writer.printf("<td class='number'>%,d</td>", tienViPham.intValue());
            writer.printf("<td class='number'>%,d</td>", tienTangCa.intValue());
            writer.printf("<td class='number'>%,d</td>", thueThuNhap.intValue());
            writer.printf("<td class='number'>%,d</td>", baoHiem.intValue()); // ✅ HIỂN THỊ BẢO HIỂM
            writer.printf("<td class='number'><strong>%,d</strong></td>", tongThuNhap.intValue());
            writer.println("</tr>");
        }

        // ✅ SUMMARY ROW VỚI TẤT CẢ TỔNG
        writer.println("<tr class='total-row'>");
        writer.printf("<td colspan='7' class='center'><strong>TỔNG CỘNG (%d bản ghi)</strong></td>", salaries.size());
        writer.printf("<td class='number'><strong>%,d</strong></td>", totalLuongCoBan.intValue());
        writer.printf("<td class='number'><strong>%,d</strong></td>", totalPhuCap.intValue());
        writer.printf("<td class='number'><strong>%,d</strong></td>", totalThuong.intValue());
        writer.printf("<td class='number'><strong>%,d</strong></td>", totalViPham.intValue());
        writer.printf("<td class='number'><strong>%,d</strong></td>", totalTangCa.intValue());
        writer.printf("<td class='number'><strong>%,d</strong></td>", totalThue.intValue());
        writer.printf("<td class='number'><strong>%,d</strong></td>", totalBaoHiem.intValue()); // ✅ TỔNG BẢO HIỂM
        writer.printf("<td class='number'><strong>%,d VNĐ</strong></td>", grandTotal.intValue());
        writer.println("</tr>");

        writer.println("</tbody></table>");

        // ✅ THÊM THỐNG KÊ CHI TIẾT
        writer.println("<div style='margin-top: 20px;'>");
        writer.println("<h3>THỐNG KÊ TỔNG QUAN</h3>");
        writer.printf("<p><strong>Tổng số nhân viên:</strong> %d người</p>", salaries.size());
        writer.printf("<p><strong>Tổng tiền lương chi trả:</strong> %,d VNĐ</p>", grandTotal.intValue());
        writer.printf("<p><strong>Tổng tiền bảo hiểm:</strong> %,d VNĐ (%.1f%% tổng lương)</p>",
                totalBaoHiem.intValue(),
                grandTotal.compareTo(BigDecimal.ZERO) > 0 ?
                        totalBaoHiem.multiply(BigDecimal.valueOf(100)).divide(grandTotal, 2, RoundingMode.HALF_UP).doubleValue() : 0.0);

        if (salaries.size() > 0) {
            long averageSalary = grandTotal.longValue() / salaries.size();
            long averageInsurance = totalBaoHiem.longValue() / salaries.size();
            writer.printf("<p><strong>Trung bình lương/người:</strong> %,d VNĐ</p>", averageSalary);
            writer.printf("<p><strong>Trung bình bảo hiểm/người:</strong> %,d VNĐ</p>", averageInsurance);
        }
        writer.println("</div>");

        writer.println("<div style='margin-top: 30px; text-align: center;'>");
        writer.println("<p><small>Báo cáo được xuất tại " + currentTime + " UTC</small></p>");
        writer.println("<p><small>Hệ thống Quản lý Nhân sự - Tự động tạo báo cáo</small></p>");
        writer.println("</div>");

        writer.println("</body></html>");
        writer.flush();

        System.out.println("Enhanced salary PDF report completed" + salaries.size() + " records with insurance column");
    }
    // ✅ THÊM VÀO SalaryController.java
    @GetMapping("/print/salary/{id}")
    public void printSalarySlip(@PathVariable Integer id, HttpServletResponse response) throws IOException {
        System.out.printf("🖨️ User trung2710 printing salary slip for ID: %d at 2025-06-20 01:35:30 UTC%n", id);

        try {
            // Get salary record
            Optional<Luong> salaryOpt = salaryRepository.findById(id);
            if (!salaryOpt.isPresent()) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Không tìm thấy bảng lương ID: " + id);
                return;
            }

            Luong luong = salaryOpt.get();

            // Recalculate salary details to ensure accuracy
            calculateSalaryDetails(luong);

            // Generate print-friendly HTML
            generateSalarySlipHtml(luong, response);

        } catch (Exception e) {
            System.err.printf("❌ Error printing salary slip for user trung2710: %s%n", e.getMessage());
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Lỗi khi in phiếu lương: " + e.getMessage());
        }
    }

    // ✅ HELPER METHOD: GENERATE SALARY SLIP HTML
    private void generateSalarySlipHtml(Luong luong, HttpServletResponse response) throws IOException {
        response.setContentType("text/html; charset=UTF-8");
        String filename = String.format("salary_%d_%d_%d.html",
                luong.getId(), luong.getThang(), luong.getNam());
        response.setHeader("Content-Disposition", "attachment; filename=" + filename);

        PrintWriter writer = response.getWriter();

        // Safe get BigDecimal values
        String employeeName = luong.getNhanVien().getHoTen();
        BigDecimal luongCoBan = safeGetBigDecimal(luong.getNhanVien().getLuongHienTai(), "LuongHienTai", employeeName);
        BigDecimal tienPC = safeGetBigDecimal(luong.getTienPC(), "TienPC", employeeName);
        BigDecimal tienThuong = safeGetBigDecimal(luong.getTienThuong(), "TienThuong", employeeName);
        BigDecimal tienViPham = safeGetBigDecimal(luong.getTienViPham(), "TienViPham", employeeName);
        BigDecimal tienTangCa = safeGetBigDecimal(luong.getTienTangCa(), "TienTangCa", employeeName);
        BigDecimal thueThuNhap = safeGetBigDecimal(luong.getThueThuNhap(), "ThueThuNhap", employeeName);
        BigDecimal tongThuNhap = safeGetBigDecimal(luong.getTongThuNhap(), "TongThuNhap", employeeName);

        // Calculate insurance
        BigDecimal totalBeforeTax = luongCoBan.add(tienPC).add(tienThuong).subtract(tienViPham).add(tienTangCa);
        BigDecimal baoHiem = totalBeforeTax.multiply(BigDecimal.valueOf(0.105));

        String currentTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        writer.println("<!DOCTYPE html>");
        writer.println("<html><head>");
        writer.println("<meta charset='UTF-8'>");
        writer.println("<title>Phiếu lương - " + employeeName + "</title>");
        writer.println("<style>");
        writer.println("body { font-family: 'Times New Roman', serif; margin: 20px; font-size: 14px; line-height: 1.6; }");
        writer.println(".header { text-align: center; margin-bottom: 30px; border-bottom: 2px solid #333; padding-bottom: 20px; }");
        writer.println(".company-info { text-align: center; margin-bottom: 20px; }");
        writer.println(".company-name { font-size: 18px; font-weight: bold; text-transform: uppercase; }");
        writer.println(".slip-title { font-size: 16px; font-weight: bold; text-transform: uppercase; margin: 20px 0; }");
        writer.println(".info-section { margin-bottom: 20px; }");
        writer.println(".info-row { display: flex; justify-content: space-between; margin-bottom: 8px; }");
        writer.println(".info-label { font-weight: bold; width: 40%; }");
        writer.println(".info-value { width: 60%; border-bottom: 1px dotted #666; padding-left: 10px; }");
        writer.println("table { width: 100%; border-collapse: collapse; margin: 20px 0; }");
        writer.println("th, td { border: 1px solid #333; padding: 8px; text-align: left; }");
        writer.println("th { background-color: #f0f0f0; font-weight: bold; text-align: center; }");
        writer.println(".number { text-align: right; }");
        writer.println(".total-row { background-color: #e8f4f8; font-weight: bold; }");
        writer.println(".signature-section { margin-top: 40px; display: flex; justify-content: space-between; }");
        writer.println(".signature-box { text-align: center; width: 45%; }");
        writer.println(".signature-line { border-top: 1px solid #333; margin-top: 60px; padding-top: 5px; }");
        writer.println("@media print { body { margin: 0; } .no-print { display: none; } }");
        writer.println("</style>");
        writer.println("<script>window.onload = function() { setTimeout(function() { window.print(); }, 1000); };</script>");
        writer.println("</head><body>");

        // Header
        writer.println("<div class='header'>");
        writer.println("<div class='company-info'>");
        writer.println("<div class='company-name'>HỆ THỐNG QUẢN LÝ NHÂN SỰ</div>");
        writer.println("<div>Địa chỉ: 123 Đường ABC, Quận XYZ, TP.HCM</div>");
        writer.println("<div>Điện thoại: (028) 1234-5678 | Email: hr@company.com</div>");
        writer.println("</div>");
        writer.println("<div class='slip-title'>PHIẾU LƯƠNG THÁNG " + luong.getThang() + "/" + luong.getNam() + "</div>");
        writer.println("</div>");

        // Employee info
        writer.println("<div class='info-section'>");
        writer.println("<h3>THÔNG TIN NHÂN VIÊN</h3>");
        writer.println("<div class='info-row'>");
        writer.println("<span class='info-label'>Mã nhân viên:</span>");
        writer.println("<span class='info-value'>" + luong.getNhanVien().getId() + "</span>");
        writer.println("</div>");
        writer.println("<div class='info-row'>");
        writer.println("<span class='info-label'>Họ và tên:</span>");
        writer.println("<span class='info-value'>" + employeeName + "</span>");
        writer.println("</div>");
        writer.println("<div class='info-row'>");
        writer.println("<span class='info-label'>Phòng ban:</span>");
        writer.println("<span class='info-value'>" + luong.getNhanVien().getChucVu().getPhongBan().getTenPhongBan() + "</span>");
        writer.println("</div>");
        writer.println("<div class='info-row'>");
        writer.println("<span class='info-label'>Chức vụ:</span>");
        writer.println("<span class='info-value'>" + luong.getNhanVien().getChucVu().getTenChucVu() + "</span>");
        writer.println("</div>");
        writer.println("<div class='info-row'>");
        writer.println("<span class='info-label'>Kỳ lương:</span>");
        writer.println("<span class='info-value'>Tháng " + luong.getThang() + "/" + luong.getNam() + "</span>");
        writer.println("</div>");
        writer.println("</div>");

        // Salary details table
        writer.println("<h3>CHI TIẾT LƯƠNG</h3>");
        writer.println("<table>");
        writer.println("<thead>");
        writer.println("<tr>");
        writer.println("<th>STT</th>");
        writer.println("<th>Khoản mục</th>");
        writer.println("<th>Số tiền (VNĐ)</th>");
        writer.println("<th>Ghi chú</th>");
        writer.println("</tr>");
        writer.println("</thead>");
        writer.println("<tbody>");

        int stt = 1;
        writer.printf("<tr><td>%d</td><td>Lương cơ bản</td><td class='number'>%,d</td><td>Lương theo hợp đồng</td></tr>%n",
                stt++, luongCoBan.intValue());
        writer.printf("<tr><td>%d</td><td>Phụ cấp</td><td class='number'>%,d</td><td>Các khoản phụ cấp</td></tr>%n",
                stt++, tienPC.intValue());
        writer.printf("<tr><td>%d</td><td>Thưởng</td><td class='number'>%,d</td><td>Thưởng trong tháng</td></tr>%n",
                stt++, tienThuong.intValue());
        writer.printf("<tr><td>%d</td><td>Tiền tăng ca</td><td class='number'>%,d</td><td>Làm thêm giờ</td></tr>%n",
                stt++, tienTangCa.intValue());
        writer.printf("<tr><td>%d</td><td>Khấu trừ vi phạm</td><td class='number'>-%,d</td><td>Các khoản vi phạm</td></tr>%n",
                stt++, tienViPham.intValue());
        writer.printf("<tr><td>%d</td><td>Thuế TNCN</td><td class='number'>-%,d</td><td>Thuế thu nhập cá nhân</td></tr>%n",
                stt++, thueThuNhap.intValue());
        writer.printf("<tr><td>%d</td><td>Bảo hiểm</td><td class='number'>-%,d</td><td>BHXH, BHYT, BHTN (10.5%%)</td></tr>%n",
                stt++, baoHiem.intValue());

        writer.println("<tr class='total-row'>");
        writer.printf("<td colspan='2'><strong>TỔNG LƯƠNG THỰC NHẬN</strong></td>");
        writer.printf("<td class='number'><strong>%,d</strong></td>", tongThuNhap.intValue());
        writer.println("<td><strong>Số tiền nhận được</strong></td>");
        writer.println("</tr>");

        writer.println("</tbody>");
        writer.println("</table>");

        // Signature section
        writer.println("<div class='signature-section'>");
        writer.println("<div class='signature-box'>");
        writer.println("<div>Nhân viên</div>");
        writer.println("<div style='font-style: italic;'>(Ký và ghi rõ họ tên)</div>");
        writer.println("<div class='signature-line'>" + employeeName + "</div>");
        writer.println("</div>");
        writer.println("<div class='signature-box'>");
        writer.println("<div>Phòng Nhân sự</div>");
        writer.println("<div style='font-style: italic;'>(Ký và đóng dấu)</div>");
        writer.println("<div class='signature-line'></div>");
        writer.println("</div>");
        writer.println("</div>");

        writer.println("<div style='margin-top: 30px; text-align: center; font-size: 12px; color: #666;'>");
        writer.println("<p>Phiếu lương được xuất bởi user trung2710 lúc " + currentTime + " UTC</p>");
        writer.println("<p>Hệ thống Quản lý Nhân sự - Tự động tạo phiếu lương</p>");
        writer.println("</div>");

        writer.println("</body></html>");
        writer.flush();

        System.out.printf("✅ Salary slip printed successfully for user trung2710: %s (ID: %d)%n", employeeName, luong.getId());
    }
}