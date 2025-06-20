package com.example.hrm.controller;

import com.example.hrm.constant.ChamCongStatusEnum;
import com.example.hrm.domain.ChamCong;
import com.example.hrm.domain.NV_ViPham;
import com.example.hrm.domain.NhanVien;
import com.example.hrm.domain.PhongBan;
import com.example.hrm.repository.*;
import com.example.hrm.service.CustomUserDetail;
import com.example.hrm.specification.AttendanceSpec;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cglib.core.Local;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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
    @Autowired
    private DepartmentRepository departmentRepository;
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
    public String getAttendancePage(Model model, Authentication authentication, @RequestParam("page") Optional<String> p
    ,@RequestParam("ten") Optional<String> ten, @RequestParam("pb")  Optional<String> pb
    ,@RequestParam("sta") Optional<LocalDate> sta,  @RequestParam("end") Optional<LocalDate> end, @RequestParam("tt") Optional<String> tt) {
        int page=1;
        CustomUserDetail cus=(CustomUserDetail) authentication.getPrincipal();
        NhanVien nhanVien=cus.getNhanVien();
        try{
            page=Integer.parseInt(p.get());
        }catch(Exception e){
            //
        }
        String tenParam=ten.orElse(null);
        String pbParam=pb.orElse(null);
        String  ttParam=tt.orElse(null);
        LocalDate staParam=sta.orElse(null);
        LocalDate endParam=end.orElse(null);
        Pageable pageable= PageRequest.of(page-1,20);
        Page<ChamCong> ccs=this.attendanceRepository.findAll(AttendanceSpec.findByCriteria(tenParam, pbParam,ttParam, staParam, endParam), pageable);
        List<ChamCong> chamCongs = ccs.getContent();
//        for(ChamCong x:chamCongs){
//            if(x.getGioRa().isAfter(LocalTime.of(17,0)) && !x.getTrangThai().equals(ChamCongStatusEnum.LATE.getValue())){
//                double hours=calculateHoursDifference(LocalTime.of(17,0), x.getGioRa());
//                x.setSoGioTangCa(hours);
//            }
//            this.attendanceRepository.save(x);
//        }
        for(ChamCong chamCong:chamCongs){
            if(chamCong.getGioVao().isAfter(LocalTime.of(8,0))){
                chamCong.setTrangThai(ChamCongStatusEnum.LATE.getValue());
                NV_ViPham nV_ViPham=new NV_ViPham();
                nV_ViPham.setNhanVien(chamCong.getNhanVien());
                nV_ViPham.setViPham(violationRepository.findById(1));
                nV_ViPham.setNgayViPham(LocalDate.now());
                long min=Duration.between(LocalTime.of(8,0),chamCong.getGioVao()).toMinutes();
                nV_ViPham.setMoTa("Đi làm muộn "+min +"phút");
                nV_ViPham.setNguoiRaQuyetDinh(this.userRepository.findById(2));
                this.nV_ViPhamRepository.save(nV_ViPham);
            }
            boolean check=true;
            if(chamCong.getGioRa()==null || chamCong.getGioVao()==null){
                check=false;
            }
            if(check==false){
                chamCong.setTrangThai("Vắng");
            }
            if(chamCong.getGioRa().isAfter(LocalTime.of(17,0)) && check==true){
                double hours=calculateHoursDifference(LocalTime.of(17,0), chamCong.getGioRa());
                chamCong.setSoGioTangCa(hours);
            }
            else{
                chamCong.setSoGioTangCa(0.0);
            }
            this.attendanceRepository.save(chamCong);
        }
        if(nhanVien.getChucVu().getQuyen().getTenQuyen().equals("Admin") || nhanVien.getChucVu().getQuyen().getTenQuyen().equals("Manager")){
            model.addAttribute("chamCongs",chamCongs);
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", ccs.getTotalPages());
            List<PhongBan> pbs=this.departmentRepository.findAll();
            model.addAttribute("pbs",pbs);
        }
        else{
            List<ChamCong> chamCongs1=new java.util.ArrayList<>();
            for(ChamCong chamCong:chamCongs){
                if(chamCong.getNhanVien().getId().equals(nhanVien.getId())){
                    chamCongs1.add(chamCong);
                }
            }
            model.addAttribute("chamCongs",chamCongs1);
        }
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
        List<ChamCong> chamCongs = this.attendanceRepository.findAll();
        boolean check=true;
        for(ChamCong x:chamCongs){
            if(x.getNhanVien().getId().equals(id) && x.getNgay().equals(ngay)){
                check=false;
                break;
            }
        }
        if(check==true){
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
        }
        return "redirect:/attendance";
    }
    @PostMapping("/attendance/update")
    public String getAttendanceUpdatePage(Model model
    ,@RequestParam("mcc") Integer mcc, @RequestParam("ngay") LocalDate ngay
    , @RequestParam("in") LocalTime in, @RequestParam("out") LocalTime out){
        ChamCong chamCong=this.attendanceRepository.findById(mcc).get();
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
        if(chamCong.getGioRa().isAfter(LocalTime.of(17,0)) && check==true){
            double hours=calculateHoursDifference(LocalTime.of(17,0), chamCong.getGioRa());
            chamCong.setSoGioTangCa(hours);
        }
        else{
            chamCong.setSoGioTangCa(0.0);
        }
        this.attendanceRepository.save(chamCong);

        return "redirect:/attendance";
    }
    @PostMapping("/attendance/delete")
    public String getAttendanceDeletePage(Model model, @RequestParam("id") Integer id){
        this.attendanceRepository.deleteById(id);
        return "redirect:/attendance";
    }
    @GetMapping("/export/attendance/excel")
    public void exportAttendanceToExcel(HttpServletResponse response,
                                        @RequestParam("ten") Optional<String> ten,
                                        @RequestParam("pb") Optional<String> pb,
                                        @RequestParam("tt") Optional<String> tt,
                                        @RequestParam("sta") Optional<LocalDate> sta,
                                        @RequestParam("end") Optional<LocalDate> end) throws IOException {

        System.out.printf("📊 User trung2710 exporting attendance to Excel at 2025-06-20 02:05:26 UTC%n");

        try {
            // Get filtered data
            String tenParam = ten.orElse(null);
            String pbParam = pb.orElse(null);
            String ttParam = tt.orElse(null);
            LocalDate staParam = sta.orElse(null);
            LocalDate endParam = end.orElse(null);

            List<ChamCong> chamCongs = this.attendanceRepository.findAll(
                    AttendanceSpec.findByCriteria(tenParam, pbParam, ttParam,staParam, endParam)
            );

            // Filter by status if provided
            if (ttParam != null && !ttParam.isEmpty()) {
                chamCongs = chamCongs.stream()
                        .filter(cc -> cc.getTrangThai().equals(ttParam))
                        .collect(java.util.stream.Collectors.toList());
            }

            generateAttendanceExcel(chamCongs, response, tenParam, pbParam, ttParam, staParam, endParam);

        } catch (Exception e) {
            System.err.printf("❌ Error exporting attendance Excel for user trung2710: %s%n", e.getMessage());
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Lỗi khi xuất Excel: " + e.getMessage());
        }
    }

    /**
     * Export attendance data to PDF
     */
    @GetMapping("/export/attendance/pdf")
    public void exportAttendanceToPdf(HttpServletResponse response,
                                      @RequestParam("ten") Optional<String> ten,
                                      @RequestParam("pb") Optional<String> pb,
                                      @RequestParam("tt") Optional<String> tt,
                                      @RequestParam("sta") Optional<LocalDate> sta,
                                      @RequestParam("end") Optional<LocalDate> end) throws IOException {

        System.out.printf("📋 User trung2710 exporting attendance to PDF at 2025-06-20 02:05:26 UTC%n");

        try {
            // Get filtered data
            String tenParam = ten.orElse(null);
            String pbParam = pb.orElse(null);
            String ttParam = tt.orElse(null);
            LocalDate staParam = sta.orElse(null);
            LocalDate endParam = end.orElse(null);

            List<ChamCong> chamCongs = this.attendanceRepository.findAll(
                    AttendanceSpec.findByCriteria(tenParam, pbParam,ttParam, staParam, endParam)
            );

            // Filter by status if provided
            if (ttParam != null && !ttParam.isEmpty()) {
                chamCongs = chamCongs.stream()
                        .filter(cc -> cc.getTrangThai().equals(ttParam))
                        .collect(java.util.stream.Collectors.toList());
            }

            generateAttendancePdf(chamCongs, response, tenParam, pbParam, ttParam, staParam, endParam);

        } catch (Exception e) {
            System.err.printf("❌ Error exporting attendance PDF for user trung2710: %s%n", e.getMessage());
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Lỗi khi xuất PDF: " + e.getMessage());
        }
    }

    // ✅ HELPER METHOD: GENERATE EXCEL
    private void generateAttendanceExcel(List<ChamCong> chamCongs, HttpServletResponse response,
                                         String tenParam, String pbParam, String ttParam,
                                         LocalDate staParam, LocalDate endParam) throws IOException {

        // Set response headers
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        String filename = String.format("attendance_report_%s.xlsx",
                java.time.LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")));
        response.setHeader("Content-Disposition", "attachment; filename=" + filename);

        // Create workbook
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Báo cáo chấm công");

        // Create styles
        CellStyle headerStyle = workbook.createCellStyle();
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerFont.setFontHeightInPoints((short) 12);
        headerFont.setColor(IndexedColors.WHITE.getIndex());
        headerStyle.setFont(headerFont);
        headerStyle.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        headerStyle.setBorderBottom(BorderStyle.THIN);
        headerStyle.setBorderTop(BorderStyle.THIN);
        headerStyle.setBorderRight(BorderStyle.THIN);
        headerStyle.setBorderLeft(BorderStyle.THIN);
        headerStyle.setAlignment(HorizontalAlignment.CENTER);

        CellStyle dataStyle = workbook.createCellStyle();
        dataStyle.setBorderBottom(BorderStyle.THIN);
        dataStyle.setBorderTop(BorderStyle.THIN);
        dataStyle.setBorderRight(BorderStyle.THIN);
        dataStyle.setBorderLeft(BorderStyle.THIN);
        dataStyle.setAlignment(HorizontalAlignment.CENTER);

        CellStyle statusOnTimeStyle = workbook.createCellStyle();
        statusOnTimeStyle.cloneStyleFrom(dataStyle);
        statusOnTimeStyle.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
        statusOnTimeStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        CellStyle statusLateStyle = workbook.createCellStyle();
        statusLateStyle.cloneStyleFrom(dataStyle);
        statusLateStyle.setFillForegroundColor(IndexedColors.YELLOW.getIndex());
        statusLateStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        CellStyle statusAbsentStyle = workbook.createCellStyle();
        statusAbsentStyle.cloneStyleFrom(dataStyle);
        statusAbsentStyle.setFillForegroundColor(IndexedColors.LIGHT_ORANGE.getIndex());
        statusAbsentStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        int rowNum = 0;

        // Title row
        Row titleRow = sheet.createRow(rowNum++);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("BÁO CÁO CHẤM CÔNG - HỆ THỐNG QUẢN LÝ NHÂN SỰ");
        CellStyle titleStyle = workbook.createCellStyle();
        Font titleFont = workbook.createFont();
        titleFont.setBold(true);
        titleFont.setFontHeightInPoints((short) 16);
        titleFont.setColor(IndexedColors.DARK_BLUE.getIndex());
        titleStyle.setFont(titleFont);
        titleStyle.setAlignment(HorizontalAlignment.CENTER);
        titleCell.setCellStyle(titleStyle);
        sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(0, 0, 0, 9));

        // Info rows
        rowNum++;
        Row infoRow1 = sheet.createRow(rowNum++);
        infoRow1.createCell(0).setCellValue("Xuất bởi: User trung2710");
        infoRow1.createCell(5).setCellValue("Thời gian: " + java.time.LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + " UTC");

        // Filter info
        Row filterRow = sheet.createRow(rowNum++);
        StringBuilder filterInfo = new StringBuilder("Bộ lọc: ");
        if (tenParam != null) filterInfo.append("Tên: ").append(tenParam).append(" | ");
        if (pbParam != null) filterInfo.append("Phòng ban: ").append(pbParam).append(" | ");
        if (ttParam != null) filterInfo.append("Trạng thái: ").append(ttParam).append(" | ");
        if (staParam != null) filterInfo.append("Từ: ").append(staParam).append(" | ");
        if (endParam != null) filterInfo.append("Đến: ").append(endParam);
        filterRow.createCell(0).setCellValue(filterInfo.toString());
        sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(rowNum-1, rowNum-1, 0, 9));

        rowNum++;

        // Header row
        Row headerRow = sheet.createRow(rowNum++);
        String[] headers = {"STT", "Mã NV", "Họ tên", "Phòng ban", "Chức vụ", "Ngày", "Giờ vào", "Giờ ra", "Trạng thái", "Tăng ca (giờ)"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        // Data rows
        int stt = 1;
        for (ChamCong cc : chamCongs) {
            Row row = sheet.createRow(rowNum++);

            row.createCell(0).setCellValue(stt++);
            row.createCell(1).setCellValue(cc.getNhanVien().getId());
            row.createCell(2).setCellValue(cc.getNhanVien().getHoTen());
            row.createCell(3).setCellValue(cc.getNhanVien().getChucVu().getPhongBan().getTenPhongBan());
            row.createCell(4).setCellValue(cc.getNhanVien().getChucVu().getTenChucVu());
            row.createCell(5).setCellValue(cc.getNgay().toString());
            row.createCell(6).setCellValue(cc.getGioVao() != null ? cc.getGioVao().toString() : "");
            row.createCell(7).setCellValue(cc.getGioRa() != null ? cc.getGioRa().toString() : "");

            // Status cell with color
            Cell statusCell = row.createCell(8);
            statusCell.setCellValue(cc.getTrangThai());
            switch (cc.getTrangThai()) {
                case "Đúng giờ":
                    statusCell.setCellStyle(statusOnTimeStyle);
                    break;
                case "Muộn":
                    statusCell.setCellStyle(statusLateStyle);
                    break;
                case "Vắng":
                    statusCell.setCellStyle(statusAbsentStyle);
                    break;
                default:
                    statusCell.setCellStyle(dataStyle);
            }

            row.createCell(9).setCellValue(cc.getSoGioTangCa() != null ? cc.getSoGioTangCa() : 0.0);

            // Apply data style to other cells
            for (int i = 0; i < 10; i++) {
                if (i != 8) { // Skip status cell
                    Cell cell = row.getCell(i);
                    if (cell != null) {
                        cell.setCellStyle(dataStyle);
                    }
                }
            }
        }

        // Auto-size columns
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }

        // Summary row
        rowNum++;
        Row summaryRow = sheet.createRow(rowNum++);
        Cell summaryCell = summaryRow.createCell(0);
        summaryCell.setCellValue(String.format("Tổng cộng: %d bản ghi chấm công", chamCongs.size()));
        CellStyle summaryStyle = workbook.createCellStyle();
        Font summaryFont = workbook.createFont();
        summaryFont.setBold(true);
        summaryStyle.setFont(summaryFont);
        summaryCell.setCellStyle(summaryStyle);
        sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(rowNum-1, rowNum-1, 0, 4));

        // Calculate statistics
        long onTimeCount = chamCongs.stream().filter(cc -> "Đúng giờ".equals(cc.getTrangThai())).count();
        long lateCount = chamCongs.stream().filter(cc -> "Muộn".equals(cc.getTrangThai())).count();
        long absentCount = chamCongs.stream().filter(cc -> "Vắng".equals(cc.getTrangThai())).count();
        double totalOvertime = chamCongs.stream().mapToDouble(cc -> cc.getSoGioTangCa() != null ? cc.getSoGioTangCa() : 0.0).sum();

        Row statsRow1 = sheet.createRow(rowNum++);
        statsRow1.createCell(0).setCellValue(String.format("Đúng giờ: %d | Muộn: %d | Vắng: %d | Tổng tăng ca: %.1f giờ",
                onTimeCount, lateCount, absentCount, totalOvertime));
        sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(rowNum-1, rowNum-1, 0, 9));

        // Write to response
        workbook.write(response.getOutputStream());
        workbook.close();

        System.out.printf("✅ Attendance Excel exported successfully for user trung2710: %d records%n", chamCongs.size());
    }
    private void generateAttendancePdf(List<ChamCong> chamCongs, HttpServletResponse response,
                                       String tenParam, String pbParam, String ttParam,
                                       LocalDate staParam, LocalDate endParam) throws IOException {

        // ✅ HTML RESPONSE INSTEAD OF PDF - SAME AS SALARY METHOD
        response.setContentType("text/html; charset=UTF-8");
        String filename = String.format("attendance_report_%s.html",
                java.time.LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")));
        response.setHeader("Content-Disposition", "attachment; filename=" + filename);

        PrintWriter writer = response.getWriter();
        String currentTime = java.time.LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        writer.println("<!DOCTYPE html>");
        writer.println("<html><head>");
        writer.println("<meta charset='UTF-8'>");
        writer.println("<title>Báo cáo chấm công - User trung2710</title>");
        writer.println("<style>");
        writer.println("body { font-family: Arial, sans-serif; margin: 15px; font-size: 11px; }");
        writer.println("table { width: 100%; border-collapse: collapse; margin-top: 20px; }");
        writer.println("th, td { border: 1px solid #ddd; padding: 6px; text-align: left; }");
        writer.println("th { background-color: #2c3e50; color: white; font-weight: bold; text-align: center; font-size: 10px; }");
        writer.println(".header { text-align: center; margin-bottom: 20px; }");
        writer.println(".number { text-align: right; }");
        writer.println(".center { text-align: center; }");
        writer.println(".status-ontime { background-color: #d4edda; color: #155724; font-weight: bold; }");
        writer.println(".status-late { background-color: #fff3cd; color: #856404; font-weight: bold; }");
        writer.println(".status-absent { background-color: #f8d7da; color: #721c24; font-weight: bold; }");
        writer.println(".summary-row { background-color: #e8f4f8; font-weight: bold; }");
        writer.println("@media print { body { margin: 0; } }");
        writer.println("</style>");
        writer.println("<script>window.onload = function() { setTimeout(function() { window.print(); }, 1000); };</script>");
        writer.println("</head><body>");

        // ✅ HEADER
        writer.println("<div class='header'>");
        writer.println("<h1>BÁO CÁO CHẤM CÔNG</h1>");
        writer.println("<h3>Hệ Thống Quản Lý Nhân Sự</h3>");

        // ✅ FILTER INFO
        StringBuilder filterInfo = new StringBuilder("Điều kiện lọc: ");
        if (tenParam != null && !tenParam.isEmpty()) {
            filterInfo.append("Tên: '").append(tenParam).append("' | ");
        }
        if (pbParam != null && !pbParam.isEmpty()) {
            filterInfo.append("Phòng ban: '").append(pbParam).append("' | ");
        }
        if (ttParam != null && !ttParam.isEmpty()) {
            filterInfo.append("Trạng thái: '").append(ttParam).append("' | ");
        }
        if (staParam != null) {
            filterInfo.append("Từ ngày: '").append(staParam).append("' | ");
        }
        if (endParam != null) {
            filterInfo.append("Đến ngày: '").append(endParam).append("' | ");
        }

        if (filterInfo.toString().equals("Điều kiện lọc: ")) {
            filterInfo.append("Tất cả dữ liệu chấm công");
        } else {
            filterInfo.setLength(filterInfo.length() - 3);
        }

        writer.println("<p>" + filterInfo.toString() + "</p>");
        writer.println("<p>Thời gian: " + currentTime + " UTC | Tổng: " + chamCongs.size() + " bản ghi</p>");
        writer.println("</div>");

        // ✅ TABLE
        writer.println("<table>");
        writer.println("<thead><tr>");
        writer.println("<th style='width: 5%;'>STT</th>");
        writer.println("<th style='width: 8%;'>Mã NV</th>");
        writer.println("<th style='width: 18%;'>Họ tên</th>");
        writer.println("<th style='width: 15%;'>Phòng ban</th>");
        writer.println("<th style='width: 12%;'>Chức vụ</th>");
        writer.println("<th style='width: 10%;'>Ngày</th>");
        writer.println("<th style='width: 8%;'>Giờ vào</th>");
        writer.println("<th style='width: 8%;'>Giờ ra</th>");
        writer.println("<th style='width: 10%;'>Trạng thái</th>");
        writer.println("<th style='width: 6%;'>Tăng ca (h)</th>");
        writer.println("</tr></thead><tbody>");

        // ✅ DATA ROWS
        int index = 1;
        long onTimeCount = 0;
        long lateCount = 0;
        long absentCount = 0;
        double totalOvertime = 0.0;

        for (ChamCong cc : chamCongs) {
            String status = cc.getTrangThai();
            String statusClass = "";

            // Count statistics
            switch (status) {
                case "Đúng giờ":
                    onTimeCount++;
                    statusClass = "status-ontime";
                    break;
                case "Muộn":
                    lateCount++;
                    statusClass = "status-late";
                    break;
                case "Vắng":
                    absentCount++;
                    statusClass = "status-absent";
                    break;
            }

            double overtime = cc.getSoGioTangCa() != null ? cc.getSoGioTangCa() : 0.0;
            totalOvertime += overtime;

            writer.println("<tr>");
            writer.printf("<td class='center'>%d</td>", index++);
            writer.printf("<td class='center'>%d</td>", cc.getNhanVien().getId());
            writer.printf("<td>%s</td>", cc.getNhanVien().getHoTen());
            writer.printf("<td>%s</td>", cc.getNhanVien().getChucVu().getPhongBan().getTenPhongBan());
            writer.printf("<td>%s</td>", cc.getNhanVien().getChucVu().getTenChucVu());
            writer.printf("<td class='center'>%s</td>", cc.getNgay().toString());
            writer.printf("<td class='center'>%s</td>", cc.getGioVao() != null ? cc.getGioVao().toString() : "-");
            writer.printf("<td class='center'>%s</td>", cc.getGioRa() != null ? cc.getGioRa().toString() : "-");
            writer.printf("<td class='center %s'>%s</td>", statusClass, status);
            writer.printf("<td class='number'>%.1f</td>", overtime);
            writer.println("</tr>");
        }

        // ✅ SUMMARY ROW
        writer.println("<tr class='summary-row'>");
        writer.printf("<td colspan='6' class='center'><strong>TỔNG CỘNG (%d bản ghi)</strong></td>", chamCongs.size());
        writer.printf("<td class='center'><strong>Đúng giờ: %d</strong></td>", onTimeCount);
        writer.printf("<td class='center'><strong>Muộn: %d</strong></td>", lateCount);
        writer.printf("<td class='center'><strong>Vắng: %d</strong></td>", absentCount);
        writer.printf("<td class='number'><strong>%.1f giờ</strong></td>", totalOvertime);
        writer.println("</tr>");

        writer.println("</tbody></table>");

        // ✅ DETAILED STATISTICS
        writer.println("<div style='margin-top: 20px;'>");
        writer.println("<h3>THỐNG KÊ TỔNG QUAN</h3>");
        writer.printf("<p><strong>Tổng số bản ghi chấm công:</strong> %d bản ghi</p>", chamCongs.size());
        writer.printf("<p><strong>Đúng giờ:</strong> %d bản ghi (%.1f%%)</p>",
                onTimeCount, chamCongs.size() > 0 ? (onTimeCount * 100.0 / chamCongs.size()) : 0.0);
        writer.printf("<p><strong>Đi muộn:</strong> %d bản ghi (%.1f%%)</p>",
                lateCount, chamCongs.size() > 0 ? (lateCount * 100.0 / chamCongs.size()) : 0.0);
        writer.printf("<p><strong>Vắng mặt:</strong> %d bản ghi (%.1f%%)</p>",
                absentCount, chamCongs.size() > 0 ? (absentCount * 100.0 / chamCongs.size()) : 0.0);
        writer.printf("<p><strong>Tổng giờ tăng ca:</strong> %.1f giờ</p>", totalOvertime);

        if (chamCongs.size() > 0) {
            double avgOvertime = totalOvertime / chamCongs.size();
            writer.printf("<p><strong>Trung bình tăng ca/người:</strong> %.1f giờ</p>", avgOvertime);
        }
        writer.println("</div>");

        // ✅ FOOTER
        writer.println("<div style='margin-top: 30px; text-align: center;'>");
        writer.println("<p><small>Báo cáo được xuất tại " + currentTime + " UTC</small></p>");
        writer.println("<p><small>Hệ thống Quản lý Nhân sự - User trung2710</small></p>");
        writer.println("<p><small>--- Hết ---</small></p>");
        writer.println("</div>");

        writer.println("</body></html>");
        writer.flush();

        System.out.printf("✅ Attendance HTML report exported successfully : %d records at 2025-06-20 02:47:24 UTC%n", chamCongs.size());
    }
}
