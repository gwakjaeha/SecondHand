package com.example.secondhand.domain.product.repository;

import com.example.secondhand.domain.product.entity.Area;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AreaRepository extends JpaRepository<Area, Long> {
	Optional<Area> findByAreaId(Long areaId);
	List<Area> findBySidoAndSigungu(String sido, String sigungu);
}
