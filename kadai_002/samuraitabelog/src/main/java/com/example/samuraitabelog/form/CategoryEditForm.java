package com.example.samuraitabelog.form;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CategoryEditForm {
	@NotNull
    private Integer id;  
	
	@NotBlank(message = "名前を入力してください。")
	private String name;

}