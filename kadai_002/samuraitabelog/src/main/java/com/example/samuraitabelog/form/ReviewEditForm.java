package com.example.samuraitabelog.form;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ReviewEditForm {
	@NotNull
    private Integer id;  
	
	@NotNull(message = "レビュースコアを選択してください。")
	private Integer score;
			
	@NotBlank(message = "コメントを入力してください。")
	private String impression;
}
