package com.example.secondhand.domain.user.domain;

import com.example.secondhand.domain.area.entity.Area;
import com.example.secondhand.domain.interest_product.entity.InterestProduct;
import com.example.secondhand.domain.product.entity.Product;
import com.example.secondhand.global.status.UserStatusCode;
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
@ToString
@Table(name = "user")
public class User extends BaseEntity implements UserStatusCode {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "user_id")
	private Long id;

	@Column(unique = true)
	private String email;
	private String password;
	private String userName;
	@Column(unique = true)
	private String phone;
	private boolean isAdmin;
	private String status;
	@Column(unique = true)
	private String emailAuthKey;
	private boolean admin;

	private LocalDateTime deleteAt;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "area_id")
	@ToString.Exclude
	private Area area;

	@OneToMany(mappedBy = "user")
	@ToString.Exclude
	List<InterestProduct> interestProductList = new ArrayList<>();

	@OneToMany(mappedBy = "user")
	@ToString.Exclude
	List<Product> productList = new ArrayList<>();
}
