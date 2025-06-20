package com.example.hrm.specification;

import com.example.hrm.domain.*;
import jakarta.persistence.criteria.Join;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;

public class AttendanceSpec {
    AttendanceSpec(){}
    public static Specification<ChamCong> findByName(String name){
        return (root, query, criteriaBuilder) -> {
            if(name==null || name.trim().isEmpty()){
                return criteriaBuilder.conjunction();
            }
            Join<ChamCong, NhanVien> chamCongjoin = root.join("nhanVien");
            return criteriaBuilder.like(
                    criteriaBuilder.lower(chamCongjoin.get("hoTen")),
                    "%" + name.toLowerCase() + "%"
            );
        };
    }
    public static Specification<ChamCong> findByPb(String tenPhongBan){
        return (root, query, criteriaBuilder) -> {
            if(tenPhongBan==null || tenPhongBan.trim().isEmpty()){
                return criteriaBuilder.conjunction();
            }
            Join<ChamCong, NhanVien> nhanVienjoin = root.join("nhanVien");
            Join<NhanVien, ChucVu> chucVujoin = nhanVienjoin.join("chucVu");
            Join<ChucVu, PhongBan> phongBanjoin = chucVujoin.join("phongBan");
            return criteriaBuilder.like(
                    criteriaBuilder.lower(phongBanjoin.get("tenPhongBan")),
                    "%" + tenPhongBan.toLowerCase() + "%"
            );
        };
    }
    public static  Specification<ChamCong> findByTt(String trangThai){
        return (root, query, criteriaBuilder) -> {
            if(trangThai==null || trangThai.trim().isEmpty()){
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.like(criteriaBuilder.lower(root.get("trangThai")),"%" + trangThai.toLowerCase() + "%");
        };
    }
    // Greater than or equal (lớn hơn hoặc bằng)
    public static Specification<ChamCong> dateGreaterThanOrEqual(LocalDate sta) {
        return (root, query, criteriaBuilder) -> {
            if (sta == null) return criteriaBuilder.conjunction();
            return criteriaBuilder.greaterThanOrEqualTo(root.get("ngay"), sta);
        };
    }
    // Less than or equal (nhỏ hơn hoặc bằng)
    public static Specification<ChamCong> dateLessThanOrEqual(LocalDate end) {
        return (root, query, criteriaBuilder) -> {
            if (end == null) return null;
            return criteriaBuilder.lessThanOrEqualTo(root.get("ngay"), end);
        };
    }
    public static Specification<ChamCong> findByCriteria(String name, String tenPhongBan,String trangThai, LocalDate sta, LocalDate end) {
        Specification<ChamCong> combinedSpec=Specification.where(null);
        combinedSpec=combinedSpec.and(findByName(name));
        combinedSpec=combinedSpec.and(dateGreaterThanOrEqual(sta));
        combinedSpec=combinedSpec.and(dateLessThanOrEqual(end));
        combinedSpec=combinedSpec.and(findByPb(tenPhongBan));
        combinedSpec=combinedSpec.and(findByTt(trangThai));
        return combinedSpec;
    }
}
