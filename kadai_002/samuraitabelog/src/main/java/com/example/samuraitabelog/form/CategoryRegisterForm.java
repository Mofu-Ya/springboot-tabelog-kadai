package com.example.samuraitabelog.form;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CategoryRegisterForm {
	@NotBlank(message = "名前を入力してください。")
	private String name;
}
