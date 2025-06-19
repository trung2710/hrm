package com.example.hrm.specification;

import com.example.hrm.domain.ChucVu;
import com.example.hrm.domain.NhanVien;
import com.example.hrm.domain.PhongBan;
import com.fasterxml.jackson.databind.util.ClassUtil;
import jakarta.persistence.criteria.Join;
import org.springframework.data.jpa.domain.Specification;

public class EmployeeSpec {
    public EmployeeSpec() {
    }
    public static Specification<NhanVien> nameString(String name){
        return (root, query, criteriaBuilder) ->  {
            if(name != null && !name.trim().isEmpty()){
              return criteriaBuilder.like(root.get("hoTen").as(String.class), "%" + name + "%");
            }
            else{
                return criteriaBuilder.conjunction(); // Trả về tất cả nếu không có điều kiện
            }
        };
    }
    public static Specification<NhanVien> findNvByPb(String tenPhongBan) {
        return (root, query, criteriaBuilder) -> {
            if (tenPhongBan == null || tenPhongBan.trim().isEmpty()) {
                return criteriaBuilder.conjunction(); // Trả về tất cả nếu không có điều kiện
            }

            // Join NhanVien → ChucVu → PhongBan
            Join<NhanVien, ChucVu> chucVuJoin = root.join("chucVu");
            Join<ChucVu, PhongBan> phongBanJoin = chucVuJoin.join("phongBan");

            // So sánh tên phòng ban (không phân biệt hoa thường)
            return criteriaBuilder.like(
                    criteriaBuilder.lower(phongBanJoin.get("tenPhongBan")),
                    "%" + tenPhongBan.toLowerCase() + "%"
            );
        };
    }
    public static Specification<NhanVien> findNV(String ten, String tenPhongBan) {
        Specification<NhanVien> combinedSpec=Specification.where(null);
        combinedSpec=combinedSpec.and(findNvByPb(tenPhongBan));
        combinedSpec=combinedSpec.and(nameString(ten));
        return combinedSpec;
    }
}
