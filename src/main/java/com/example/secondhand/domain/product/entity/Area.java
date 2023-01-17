package com.example.secondhand.domain.product.entity;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class Area {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long areaId;
	private String sido;
	private String sigungu;
	private String eupmyeondong;
	private double lat;
	private double lnt;
}
