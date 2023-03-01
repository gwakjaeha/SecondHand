package com.example.secondhand.domain.area.repository;

import com.example.secondhand.domain.area.entity.Area;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AreaRepository extends JpaRepository<Area, Long> {
	List<Area> findBySidoAndSigungu(String sido, String sigungu);

}
