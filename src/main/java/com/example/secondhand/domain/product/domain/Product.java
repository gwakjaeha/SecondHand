package com.example.secondhand.domain.product.domain;

import com.example.secondhand.global.domain.BaseEntity;
import java.time.LocalDateTime;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Entity
public class Product extends BaseEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long productId;
	@Column(unique = true)
	private Long userId;
	@Column(unique = true)
	private Long areaId;
	@Column(unique = true)
	private Long categoryId;
	private String title;
	private String content;
	private String imagePath;
	private Long price;
	private String transactionPlace;
	private boolean transactionStatus;

	private LocalDateTime deleteDt;
}
