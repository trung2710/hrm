package com.example.hrm.controller;

import com.example.hrm.constant.NhanVienStatusEnum;
import com.example.hrm.domain.BaoHiem;
import com.example.hrm.domain.HopDong;
import com.example.hrm.domain.NhanVien;
import com.example.hrm.domain.PhongBan;
import com.example.hrm.repository.*;
import com.example.hrm.service.UploadService;
import com.example.hrm.specification.EmployeeSpec;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
    public String getUserPage(Model model, @RequestParam("page") Optional<String> p, @RequestParam("ten") Optional<String> ten, @RequestParam("pb") Optional<String> pb) {
        //Optional<String> trong Java được sử dụng để biểu diễn một đối tượng có thể chứa hoặc không chứa giá trị.
        // Đây là một cách để xử lý trường hợp null một cách an toàn và rõ ràng hơn.
        int page=1;
        try{
            if(p.isPresent()){
                page=Integer.parseInt(p.get());
            }
        }catch (Exception e){

        }
        String tenParam = ten.orElse(null);
        String pbParam = pb.orElse(null);
        //database quan tam 2 tham so la offset vs limit
        //client:  quan tam den limit thoi
        Pageable pageable = PageRequest.of(page-1 , 20);
        Page<NhanVien> users=this.userRepository.findAll(EmployeeSpec.findNV(tenParam, pbParam), pageable);
        List<NhanVien> listUsers=users.getContent();
        List<PhongBan> pbs=this.departmentRepository.findAll();
        model.addAttribute("pbs",pbs);
        model.addAttribute("users",listUsers);
        model.addAttribute("currentPage",page);
        model.addAttribute("totalPages", users.getTotalPages());
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
            List<HopDong> hdong=new ArrayList<>();
            for(HopDong x : this.contractRepository.findAll()){
                if (x.getNhanVien().getId().equals(nhanVien.getId())){
                    hdong.add(x);
                }
            }
            boolean check=false;
            for(HopDong  x : hdong){
                if(x.getNgayKetThuc().isAfter(LocalDate.now())){
                    check=true;
                    break;
                }
            }
            if(!check){
                nhanVien.setTrangThai(NhanVienStatusEnum.RETIRE.getValue());
            }
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

    //Xuat file bao cao dang pdf va excel
    @GetMapping("/export/employee/pdf")  // ✅ FIXED: Thêm /export
    public void exportEmployeePdf(
            @RequestParam(required = false) String ten,
            @RequestParam(required = false) String pb,
            HttpServletResponse response) throws IOException {

        System.out.println("Filters: ten=" + ten + ", pb=" + pb);

        String tenParam = (ten != null && !ten.isEmpty()) ? ten : null;
        String pbParam = (pb != null && !pb.isEmpty()) ? pb : null;

        // Get ALL employees (no pagination)
        Page<NhanVien> allEmployeesPage = userRepository.findAll(
                EmployeeSpec.findNV(tenParam, pbParam),
                Pageable.unpaged()
        );
        List<NhanVien> allEmployees = allEmployeesPage.getContent();

        System.out.println("Found " + allEmployees.size() + " employees for PDF export");

        // ✅ Simple PDF export (HTML format)
        exportToPdf(allEmployees, response, tenParam, pbParam);
    }

    @GetMapping("/export/employee/excel")  // ✅ FIXED: Thêm /export
    public void exportEmployeeExcel(
            @RequestParam(required = false) String ten,
            @RequestParam(required = false) String pb,
            HttpServletResponse response) throws IOException {

        System.out.println("Filters: ten=" + ten + ", pb=" + pb);

        String tenParam = (ten != null && !ten.isEmpty()) ? ten : null;
        String pbParam = (pb != null && !pb.isEmpty()) ? pb : null;

        Page<NhanVien> allEmployeesPage = userRepository.findAll(
                EmployeeSpec.findNV(tenParam, pbParam),
                Pageable.unpaged()
        );
        List<NhanVien> allEmployees = allEmployeesPage.getContent();

        System.out.println("Found " + allEmployees.size() + " employees for Excel export");

        // ✅ Simple Excel export (CSV format)
        exportToExcel(allEmployees, response, tenParam, pbParam);
    }

    private void exportToExcel(List<NhanVien> employees, HttpServletResponse response,
                               String ten, String pb) throws IOException {

        // ✅ FIXED: Sử dụng CSV format với đúng extension
        response.setContentType("text/csv; charset=UTF-8");
        String filename = String.format("employee_report_%s.csv",
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")));
        response.setHeader("Content-Disposition", "attachment; filename=" + filename);

        PrintWriter writer = response.getWriter();

        // ✅ UTF-8 BOM for Excel compatibility
        writer.write("\uFEFF");

        // Title
        writer.println("BÁO CÁO DANH SÁCH NHÂN VIÊN");
        writer.println("");

        // Filter info
        StringBuilder filterInfo = new StringBuilder("Điều kiện lọc: ");
        if (ten != null && !ten.isEmpty()) {
            filterInfo.append("Tìm kiếm: ").append(ten).append(" | ");
        }
        if (pb != null && !pb.isEmpty()) {
            filterInfo.append("Phòng ban: ").append(pb).append(" | ");
        }
        if (filterInfo.toString().equals("Điều kiện lọc: ")) {
            filterInfo.append("Tất cả nhân viên");
        } else {
            filterInfo.setLength(filterInfo.length() - 3);
        }
        writer.println(filterInfo.toString());
        writer.println("Xuất bởi: trung2710 | Thời gian: "+LocalDateTime.now() +" UTC | Tổng: " + employees.size() + " nhân viên");
        writer.println("");

        // ✅ FIXED: Sử dụng comma-separated values thay vì tab
        writer.println("STT,Mã NV,Họ tên,Phòng ban,Chức vụ,Email,Số điện thoại,Trạng thái");

        // Data
        int index = 1;
        for (NhanVien emp : employees) {
            String phongBan = emp.getChucVu() != null && emp.getChucVu().getPhongBan() != null ?
                    emp.getChucVu().getPhongBan().getTenPhongBan() : "Chưa có";
            String chucVu = emp.getChucVu() != null ? emp.getChucVu().getTenChucVu() : "Chưa có";
            String email = emp.getEmail() != null ? emp.getEmail() : "Chưa có";
            String sdt = emp.getSoDienThoai() != null ? emp.getSoDienThoai() : "Chưa có";
            String trangThai = emp.getTrangThai() != null ? emp.getTrangThai() : "Chưa xác định";
            String hoTen = emp.getHoTen() != null ? emp.getHoTen() : "";

            // ✅ FIXED: Escape commas in data và sử dụng quotes
            writer.printf("%d,\"%d\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\"%n",
                    index++,
                    emp.getId(),
                    hoTen.replace("\"", "\"\""), // Escape quotes
                    phongBan.replace("\"", "\"\""),
                    chucVu.replace("\"", "\"\""),
                    email.replace("\"", "\"\""),
                    sdt.replace("\"", "\"\""),
                    trangThai.replace("\"", "\"\"")
            );
        }

        writer.flush();
        System.out.println("CSV export completed  " + employees.size() + " employees");
    }

    private void exportToPdf(List<NhanVien> employees, HttpServletResponse response,
                             String ten, String pb) throws IOException {

        response.setContentType("text/html; charset=UTF-8");
        response.setHeader("Content-Disposition", "inline; filename=employee_report_trung2710.html");

        PrintWriter writer = response.getWriter();

        writer.println("<!DOCTYPE html>");
        writer.println("<html><head>");
        writer.println("<meta charset='UTF-8'>");
        writer.println("<title>Báo cáo nhân viên</title>");
        writer.println("<style>");
        writer.println("body { font-family: Arial, sans-serif; margin: 20px; }");
        writer.println("table { width: 100%; border-collapse: collapse; margin-top: 20px; }");
        writer.println("th, td { border: 1px solid #ddd; padding: 8px; text-align: left; }");
        writer.println("th { background-color: #f2f2f2; font-weight: bold; }");
        writer.println(".header { text-align: center; margin-bottom: 20px; }");
        writer.println("@media print { body { margin: 0; } }");
        writer.println("</style>");
        writer.println("</head><body>");

        // Header
        writer.println("<div class='header'>");
        writer.println("<h1>BÁO CÁO DANH SÁCH NHÂN VIÊN</h1>");

        StringBuilder filterInfo = new StringBuilder("Điều kiện lọc: ");
        if (ten != null && !ten.isEmpty()) {
            filterInfo.append("Tìm kiếm: '").append(ten).append("' | ");
        }
        if (pb != null && !pb.isEmpty()) {
            filterInfo.append("Phòng ban: '").append(pb).append("' | ");
        }
        if (filterInfo.toString().equals("Điều kiện lọc: ")) {
            filterInfo.append("Tất cả nhân viên");
        } else {
            filterInfo.setLength(filterInfo.length() - 3);
        }
        writer.println("<p>" + filterInfo.toString() + "</p>");
        writer.println("<p>Thời gian: "+LocalDateTime.now()+ " UTC | Tổng: " + employees.size() + " nhân viên</p>");
        writer.println("</div>");

        // Table
        writer.println("<table>");
        writer.println("<thead><tr>");
        writer.println("<th>STT</th><th>Mã NV</th><th>Họ tên</th><th>Phòng ban</th><th>Chức vụ</th>");
        writer.println("<th>Email</th><th>Số điện thoại</th><th>Trạng thái</th>");
        writer.println("</tr></thead><tbody>");

        int index = 1;
        for (NhanVien emp : employees) {
            String phongBan = emp.getChucVu() != null && emp.getChucVu().getPhongBan() != null ?
                    emp.getChucVu().getPhongBan().getTenPhongBan() : "Chưa có";
            String chucVu = emp.getChucVu() != null ? emp.getChucVu().getTenChucVu() : "Chưa có";
            String email = emp.getEmail() != null ? emp.getEmail() : "Chưa có";
            String sdt = emp.getSoDienThoai() != null ? emp.getSoDienThoai() : "Chưa có";
            String trangThai = emp.getTrangThai() != null ? emp.getTrangThai() : "Chưa xác định";

            writer.println("<tr>");
            writer.printf("<td>%d</td><td>%d</td><td>%s</td><td>%s</td><td>%s</td><td>%s</td><td>%s</td><td>%s</td>%n",
                    index++, emp.getId(), emp.getHoTen() != null ? emp.getHoTen() : "",
                    phongBan, chucVu, email, sdt, trangThai
            );
            writer.println("</tr>");
        }

        writer.println("</tbody></table>");
        writer.println("</body></html>");
        writer.flush();
    }
}
