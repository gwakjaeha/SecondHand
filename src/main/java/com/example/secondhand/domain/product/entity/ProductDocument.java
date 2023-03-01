package com.example.secondhand.domain.product.entity;

import static org.springframework.data.elasticsearch.annotations.DateFormat.date_hour_minute_second_millis;
import static org.springframework.data.elasticsearch.annotations.DateFormat.epoch_millis;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.Mapping;
import org.springframework.data.elasticsearch.annotations.Setting;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(indexName = "product")
@Mapping(mappingPath = "elastic/product-mapping.json")
@Setting(settingPath = "elastic/product-setting.json")
public class ProductDocument{

	@Id
	private Long productId;
	private Long userId;
	private Long areaId;
	private Long categoryId;
	private String title;
	private String content;
	private String imagePath;
	private Long price;
	private String transactionPlace;
	private boolean transactionStatus;
	@Field(type = FieldType.Date, format = {date_hour_minute_second_millis, epoch_millis})
	private LocalDateTime createdAt;
	@Field(type = FieldType.Date, format = {date_hour_minute_second_millis, epoch_millis})
	private LocalDateTime deletedAt;

	public static ProductDocument from(Product product){
		return ProductDocument.builder()
			.productId(product.getId())
			.userId(product.getUser().getId())
			.areaId(product.getArea().getId())
			.categoryId(product.getCategory().getId())
			.title(product.getTitle())
			.content(product.getContent())
			.imagePath(product.getImagePath())
			.price(product.getPrice())
			.transactionPlace(product.getTransactionPlace())
			.transactionStatus(product.isTransactionStatus())
			.createdAt(product.getCreatedAt())
			.deletedAt(product.getDeleteAt())
			.build();
	}
}
