package com.example.secondhand.domain.product.entity;

import com.example.secondhand.global.entity.BaseEntity;
import java.time.LocalDateTime;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Entity
@EntityListeners(AuditingEntityListener.class)
public class InterestProduct {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long interestProductId;
	private Long userId;
	private Long productId;
	@CreatedDate
	private LocalDateTime createdDt;
}
