package com.example.secondhand.domain.product.entity;

import com.example.secondhand.domain.catetory.entity.Category;
import com.example.secondhand.domain.user.domain.User;
import com.example.secondhand.global.entity.BaseEntity;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Entity
@Table(name="product")
public class Product extends BaseEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "product_id")
	private Long id;

	private String title;
	private String content;
	private String imagePath;
	private Long price;
	private String transactionPlace;
	private boolean transactionStatus;

	private LocalDateTime deleteDt;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id")
	@ToString.Exclude
	private User user;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "area_id")
	@ToString.Exclude
	private Area area;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "category_id")
	@ToString.Exclude
	private Category category;

	@OneToMany(mappedBy = "product")
	@ToString.Exclude
	List<InterestProduct> interestProductList = new ArrayList<>();
}
