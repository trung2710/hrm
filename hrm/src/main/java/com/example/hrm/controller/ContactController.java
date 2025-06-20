package com.example.hrm.controller;

import com.example.hrm.constant.ContractStatusEnum;
import com.example.hrm.domain.HopDong;
import com.example.hrm.domain.NhanVien;
import com.example.hrm.repository.ContractRepository;
import com.example.hrm.repository.UserRepository;
import com.example.hrm.service.CustomUserDetail;
import com.example.hrm.specification.ContractSpec;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Controller
public class ContactController {
    @Autowired
    private ContractRepository contractRepository;
    @Autowired
    private UserRepository userRepository;
    @GetMapping("/contract")
    public String getContactPage(Model model, Authentication authentication, @RequestParam("page") Optional<String> p,
                                 @RequestParam("ten") Optional<String> ten, @RequestParam("loai") Optional<String> loai,
                                 @RequestParam("tt") Optional<String> tt){
        CustomUserDetail cus=(CustomUserDetail) authentication.getPrincipal();
        NhanVien nhanVien=cus.getNhanVien();
        if(nhanVien.getChucVu().getQuyen().getTenQuyen().equals("Admin") || nhanVien.getChucVu().getQuyen().getTenQuyen().equals("Manager")){
            int page=1;
            try{
                if(p.isPresent()){
                    page=Integer.parseInt(p.get());
                }
            }catch (Exception e){

            }
            String tenParam=ten.orElse(null);
            String loaiParam=loai.orElse(null);
            String ttParam=tt.orElse(null);
            //database quan tam 2 tham so la offset vs limit
            //client:  quan tam den limit thoi
            Pageable pageable = PageRequest.of(page-1 , 20);
            Page<HopDong> hds=this.contractRepository.findAll(ContractSpec.findByCriteria(tenParam, loaiParam, ttParam), pageable);
            List<HopDong> hopDongs = hds.getContent();
            List<NhanVien> nhanVienList=this.userRepository.findAll();
            model.addAttribute("nhanViens",nhanVienList);
            int dem1=0;
            int dem2=0;
            int dem3=0;
            for(HopDong x : hopDongs){
                //logic nếu hợp đồng hết hạn thì set trạng thái của hợp đồng.
                if(x.getNgayKetThuc().isBefore(LocalDate.now())){
                    x.setTrangThai(ContractStatusEnum.EXPIRED.getValue());
                    this.contractRepository.save(x);
                }
                if (x.getTrangThai().equals(ContractStatusEnum.VALID.getValue())){
                    dem1+=1;
                }
                else if(ChronoUnit.DAYS.between(x.getNgayKetThuc(), LocalDate.now())<=60){
                    dem2+=1;
                }
                else{
                    dem3+=1;
                }
            }
            model.addAttribute("valid",dem1);
            model.addAttribute("expire",dem2);
            model.addAttribute("expired",dem3);
            model.addAttribute("hopDongs",hopDongs);
            model.addAttribute("currentPage",page);
            model.addAttribute("totalPages", hds.getTotalPages());
        }
        else{
            List<HopDong> hopDongs = contractRepository.findAll();
            List<HopDong>hp=new ArrayList<>();
            for (HopDong x : hopDongs){
                if(x.getNhanVien().getId().equals(nhanVien.getId())){
                    hp.add(x);
                }
            }
            model.addAttribute("hopDongs",hp);
        }

        return "admin/contract/show";
    }

    @PostMapping("/contract/create")
    public String postCreateContact(Model model
    , @RequestParam("ten") String ten, @RequestParam("id") Integer id
    , @RequestParam("sta") LocalDate sta, @RequestParam("end") LocalDate end
    , @RequestParam("luong")BigDecimal luongCoBan){
        HopDong hopDong=new HopDong();
        List<HopDong> hopDongs=this.contractRepository.findAll();
        boolean check=true;
        for(HopDong x:hopDongs){
            if(x.getNhanVien().getId().equals(id) && x.getNgayKetThuc().isAfter(sta)){
                check=false;
                break;
            }
        }
        if(check==true){
            hopDong.setLoaiHopDong(ten);
            hopDong.setNhanVien(this.userRepository.findById(id));
            hopDong.setNgayBatDau(sta);
            hopDong.setNgayKetThuc(end);
            hopDong.setLuongCoBan(luongCoBan);
            this.contractRepository.save(hopDong);
            NhanVien nhanVien=hopDong.getNhanVien();
            nhanVien.setLuongHienTai(hopDong.getLuongCoBan());
            this.userRepository.save(nhanVien);
        }
        return "redirect:/contract";
    }

    @PostMapping("/contract/delete")
    public String postDeleteContact(Model model, @RequestParam("id") Integer id){
        this.contractRepository.deleteById(id);
        return "redirect:/contract";
    }
    // ✅ NEW: Export contract details to HTML/PDF
    @GetMapping("/export/contract/{id}")
    public void exportContractDetails(@PathVariable Integer id, HttpServletResponse response)
            throws IOException {

        System.out.println("Requesting contract export for ID: " + id + " at 2025-06-19 19:39:23");

        // Get contract details
        Optional<HopDong> hopDongOpt = contractRepository.findById(id);
        if (!hopDongOpt.isPresent()) {
            response.sendError(404, "Hợp đồng không tồn tại");
            return;
        }

        HopDong hopDong = hopDongOpt.get();

        // Set response headers
        response.setContentType("text/html; charset=UTF-8");
        String filename = String.format("contract_details_trung2710_ID_%d_%s.html",
                id, LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")));
        response.setHeader("Content-Disposition", "attachment; filename=" + filename);

        // Generate HTML content
        exportContractToHtml(hopDong, response);
    }

    // ✅ Helper method to generate contract HTML
    private void exportContractToHtml(HopDong hopDong, HttpServletResponse response)
            throws IOException {

        PrintWriter writer = response.getWriter();
        String currentTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        writer.println("<!DOCTYPE html>");
        writer.println("<html><head>");
        writer.println("<meta charset='UTF-8'>");
        writer.println("<title>Chi tiết hợp đồng</title>");
        writer.println("<style>");
        writer.println("body { font-family: 'Times New Roman', serif; margin: 20px; line-height: 1.6; }");
        writer.println(".header { text-align: center; margin-bottom: 30px; }");
        writer.println(".company-info { text-align: center; margin-bottom: 20px; }");
        writer.println(".contract-title { font-size: 24px; font-weight: bold; text-transform: uppercase; margin: 20px 0; }");
        writer.println(".contract-content { margin: 20px 0; }");
        writer.println(".info-table { width: 100%; border-collapse: collapse; margin: 20px 0; }");
        writer.println(".info-table td { padding: 8px; border: 1px solid #ddd; }");
        writer.println(".info-table .label { background-color: #f8f9fa; font-weight: bold; width: 30%; }");
        writer.println(".signature-section { margin-top: 50px; }");
        writer.println(".signature-box { display: inline-block; width: 45%; text-align: center; margin: 20px 0; }");
        writer.println(".print-info { position: fixed; bottom: 10px; right: 10px; font-size: 10px; color: #666; }");
        writer.println("@media print { body { margin: 0; } .print-info { display: block; } }");
        writer.println("</style>");

        // Auto-print script
        writer.println("<script>");
        writer.println("window.onload = function() {");
        writer.println("  setTimeout(function() { window.print(); }, 1000);");
        writer.println("};");
        writer.println("</script>");
        writer.println("</head><body>");

        // Company header
        writer.println("<div class='company-info'>");
        writer.println("<h2>CÔNG TY ABC</h2>");
        writer.println("<p>Địa chỉ: 123 Đường ABC, Quận XYZ, TP.HCM</p>");
        writer.println("<p>Điện thoại: (028) 1234-5678 | Email: info@company.com</p>");
        writer.println("</div>");

        // Contract title
        writer.println("<div class='header'>");
        writer.println("<h1 class='contract-title'>HỢP ĐỒNG LAO ĐỘNG</h1>");
        writer.println("<p><strong>Số:</strong> " + hopDong.getId() + "/" + LocalDate.now().getYear() + "-HĐLĐ</p>");
        writer.println("</div>");

        // Contract info
        writer.println("<div class='contract-content'>");
        writer.println("<p><strong>Căn cứ:</strong> Bộ luật lao động số 45/2019/QH14 được Quốc hội nước Cộng hòa xã hội chủ nghĩa Việt Nam thông qua ngày 20/11/2019.</p>");
        writer.println("<p><strong>Căn cứ:</strong> Các quy định pháp luật có liên quan.</p>");

        // Basic information
        writer.println("<h3>I. THÔNG TIN CƠ BẢN</h3>");
        writer.println("<table class='info-table'>");

        // Contract info
        writer.println("<tr>");
        writer.println("<td class='label'>Mã hợp đồng:</td>");
        writer.println("<td>" + hopDong.getId() + "</td>");
        writer.println("</tr>");

        writer.println("<tr>");
        writer.println("<td class='label'>Loại hợp đồng:</td>");
        writer.println("<td>" + hopDong.getLoaiHopDong() + "</td>");
        writer.println("</tr>");

        writer.println("<tr>");
        writer.println("<td class='label'>Trạng thái:</td>");
        writer.println("<td>" + hopDong.getTrangThai() + "</td>");
        writer.println("</tr>");

        writer.println("</table>");

        // Employee information
        writer.println("<h3>II. THÔNG TIN NGƯỜI LAO ĐỘNG</h3>");
        writer.println("<table class='info-table'>");

        writer.println("<tr>");
        writer.println("<td class='label'>Họ và tên:</td>");
        writer.println("<td>" + hopDong.getNhanVien().getHoTen() + "</td>");
        writer.println("</tr>");

        writer.println("<tr>");
        writer.println("<td class='label'>CCCD/CMND:</td>");
        writer.println("<td>" + (hopDong.getNhanVien().getCccd() != null ? hopDong.getNhanVien().getCccd() : "Chưa cập nhật") + "</td>");
        writer.println("</tr>");

        writer.println("<tr>");
        writer.println("<td class='label'>Email:</td>");
        writer.println("<td>" + (hopDong.getNhanVien().getEmail() != null ? hopDong.getNhanVien().getEmail() : "Chưa cập nhật") + "</td>");
        writer.println("</tr>");

        writer.println("<tr>");
        writer.println("<td class='label'>Số điện thoại:</td>");
        writer.println("<td>" + (hopDong.getNhanVien().getSoDienThoai() != null ? hopDong.getNhanVien().getSoDienThoai() : "Chưa cập nhật") + "</td>");
        writer.println("</tr>");

        writer.println("<tr>");
        writer.println("<td class='label'>Phòng ban:</td>");
        writer.println("<td>" + hopDong.getNhanVien().getChucVu().getPhongBan().getTenPhongBan() + "</td>");
        writer.println("</tr>");

        writer.println("<tr>");
        writer.println("<td class='label'>Chức vụ:</td>");
        writer.println("<td>" + hopDong.getNhanVien().getChucVu().getTenChucVu() + "</td>");
        writer.println("</tr>");

        writer.println("</table>");

        // Contract terms
        writer.println("<h3>III. ĐIỀU KHOẢN HỢP ĐỒNG</h3>");
        writer.println("<table class='info-table'>");

        writer.println("<tr>");
        writer.println("<td class='label'>Ngày bắt đầu:</td>");
        writer.println("<td>" + hopDong.getNgayBatDau().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + "</td>");
        writer.println("</tr>");

        writer.println("<tr>");
        writer.println("<td class='label'>Ngày kết thúc:</td>");
        writer.println("<td>" + hopDong.getNgayKetThuc().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + "</td>");
        writer.println("</tr>");

        // Calculate duration
        long months = ChronoUnit.MONTHS.between(hopDong.getNgayBatDau(), hopDong.getNgayKetThuc());
        writer.println("<tr>");
        writer.println("<td class='label'>Thời hạn hợp đồng:</td>");
        writer.println("<td>" + months + " tháng</td>");
        writer.println("</tr>");

        writer.println("<tr>");
        writer.println("<td class='label'>Lương cơ bản:</td>");
        writer.println("<td>" + String.format("%,d", hopDong.getLuongCoBan().intValue()) + " VNĐ</td>");
        writer.println("</tr>");

        writer.println("</table>");

        // Contract clauses
        writer.println("<h3>IV. QUYỀN VÀ NGHĨA VỤ CỦA CÁC BÊN</h3>");
        writer.println("<h4>1. Quyền và nghĩa vụ của Công ty:</h4>");
        writer.println("<ul>");
        writer.println("<li>Trả lương đầy đủ, đúng hạn theo thỏa thuận.</li>");
        writer.println("<li>Bảo đảm điều kiện làm việc an toàn, vệ sinh lao động.</li>");
        writer.println("<li>Tôn trọng nhân phẩm, danh dự của người lao động.</li>");
        writer.println("</ul>");

        writer.println("<h4>2. Quyền và nghĩa vụ của Người lao động:</h4>");
        writer.println("<ul>");
        writer.println("<li>Thực hiện đúng, đầy đủ công việc được giao.</li>");
        writer.println("<li>Tuân thủ nội quy, quy chế của Công ty.</li>");
        writer.println("<li>Bảo vệ tài sản và bí mật kinh doanh của Công ty.</li>");
        writer.println("</ul>");

        // Signature section
        writer.println("<h3>V. CAM KẾT</h3>");
        writer.println("<p>Hai bên cam kết thực hiện đúng các điều khoản đã thỏa thuận trong hợp đồng này.</p>");

        writer.println("<div class='signature-section'>");
        writer.println("<div style='display: flex; justify-content: space-between;'>");

        writer.println("<div class='signature-box'>");
        writer.println("<p><strong>NGƯỜI LAO ĐỘNG</strong></p>");
        writer.println("<p><em>(Ký tên và ghi rõ họ tên)</em></p>");
        writer.println("<br><br><br>");
        writer.println("<p>" + hopDong.getNhanVien().getHoTen() + "</p>");
        writer.println("</div>");

        writer.println("<div class='signature-box'>");
        writer.println("<p><strong>NGƯỜI TUYỂN DỤNG</strong></p>");
        writer.println("<p><em>(Ký tên, đóng dấu)</em></p>");
        writer.println("<br><br><br>");
        writer.println("<p>Giám đốc</p>");
        writer.println("</div>");

        writer.println("</div>");
        writer.println("</div>");

        // Footer info
        writer.println("<div class='print-info'>");
        writer.println("<p>Xuất bởi: trung2710 | Thời gian: " + currentTime + " UTC</p>");
        writer.println("<p>Hệ thống Quản lý Nhân sự - Công ty ABC</p>");
        writer.println("</div>");

        writer.println("</div>"); // End contract-content
        writer.println("</body></html>");
        writer.flush();

        System.out.println("Contract export completed - Contract ID: " + hopDong.getId());
    }
}
