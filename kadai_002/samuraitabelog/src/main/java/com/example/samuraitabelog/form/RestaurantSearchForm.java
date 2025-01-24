package com.example.samuraitabelog.form;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RestaurantSearchForm {
	private String keyword;
	private Integer categoryId;
	private Integer price;
	private String order;
}
