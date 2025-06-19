package com.example.hrm.specification;

import com.example.hrm.domain.HopDong;
import com.example.hrm.domain.NhanVien;
import jakarta.persistence.criteria.Join;
import org.springframework.data.jpa.domain.Specification;

public class ContractSpec {
    public ContractSpec(){

    }
    public static Specification<HopDong> findByTenNV(String name){
        return (root, query, criteriaBuilder) -> {
            if(name==null || name.trim().isEmpty()){
                return criteriaBuilder.conjunction();
            }
            Join<HopDong, NhanVien> hopDongjoin = root.join("nhanVien");
            return criteriaBuilder.like(
                    criteriaBuilder.lower(hopDongjoin.get("hoTen")),
                    "%" + name.toLowerCase() + "%"
            );
        };
    }
    public static Specification<HopDong> findByType(String type){
        return (root, query, criteriaBuilder) -> {
          if((type==null || type.trim().isEmpty())){
              return criteriaBuilder.conjunction();
          }
            return criteriaBuilder.like(root.get("loaiHopDong").as(String.class), "%" + type + "%");
        };
    }
    public static Specification<HopDong> findByStatus(String status){
        return (root, query, criteriaBuilder) ->  {
            if((status==null || status.trim().isEmpty())){
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.like(criteriaBuilder.lower(root.get("trangThai")), "%" + status.toLowerCase() + "%");
        };
    }
    public static Specification<HopDong> findByCriteria(String ten, String type, String status){
        Specification<HopDong> combinedSpec=Specification.where(null);
        combinedSpec=combinedSpec.and(findByTenNV(ten));
        combinedSpec=combinedSpec.and(findByStatus(status));
        combinedSpec=combinedSpec.and(findByType(type));
        return combinedSpec;
    }

}
