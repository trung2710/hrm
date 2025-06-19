package com.example.hrm.specification;

import com.example.hrm.domain.*;
import jakarta.persistence.criteria.Join;
import org.springframework.data.jpa.domain.Specification;

public class SalarySpec {
    SalarySpec(){

    }
    public static Specification<Luong> findByTenNV(String name){
        return (root, query, criteriaBuilder) -> {
            if(name==null || name.trim().isEmpty()){
                return criteriaBuilder.conjunction();
            }
            Join<Luong, NhanVien> hopDongjoin = root.join("nhanVien");
            return criteriaBuilder.like(
                    criteriaBuilder.lower(hopDongjoin.get("hoTen")),
                    "%" + name.toLowerCase() + "%"
            );
        };
    }
    public static Specification<Luong> findByMonth(String month){
        return (root, query, criteriaBuilder) -> {
            if(month==null || month.trim().isEmpty()){
                return criteriaBuilder.conjunction();
            }
            Integer m=Integer.parseInt(month);
            return criteriaBuilder.equal(root.get("thang"), m);
        };
    }
    public static  Specification<Luong> findByYear(String year){
        return (root, query, criteriaBuilder) -> {
            if(year==null || year.trim().isEmpty()){
                return criteriaBuilder.conjunction();
            }
            Integer y=Integer.parseInt(year);
            return criteriaBuilder.equal(root.get("nam"), y);
        };
    }
    public static Specification<Luong> findByPb(String tenPhongBan){
        return (root, query, criteriaBuilder) -> {
            if(tenPhongBan==null || tenPhongBan.trim().isEmpty()){
                return criteriaBuilder.conjunction();
            }
            Join<Luong, NhanVien> nhanVienjoin = root.join("nhanVien");
            Join<NhanVien, ChucVu> chucVujoin = nhanVienjoin.join("chucVu");
            Join<ChucVu, PhongBan> phongBanjoin = chucVujoin.join("phongBan");
            return criteriaBuilder.like(
                    criteriaBuilder.lower(phongBanjoin.get("tenPhongBan")),
                    "%" + tenPhongBan.toLowerCase() + "%"
            );
        };
    }
    public static Specification<Luong> findByCriteria(String name, String month, String year, String tenPhongBan){
        Specification<Luong> combinedSpec=Specification.where(null);
        combinedSpec=combinedSpec.and(findByTenNV(name));
        combinedSpec=combinedSpec.and(findByMonth(month));
        combinedSpec=combinedSpec.and(findByYear(year));
        combinedSpec=combinedSpec.and(findByPb(tenPhongBan));
        return combinedSpec;
    }
}
