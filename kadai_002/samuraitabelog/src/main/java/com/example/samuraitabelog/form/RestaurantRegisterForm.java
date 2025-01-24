package com.example.samuraitabelog.form;

import java.time.LocalTime;
import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RestaurantRegisterForm {
	@NotBlank(message = "お店の名前を入力してください。")
	private String name;
	
	private MultipartFile imageFile;
	
	@NotNull(message = "ジャンルを選択してください")
    private Integer categoryId;
	
	@NotBlank(message = "お店の説明を入力してください。")
	private String description;
	
	@NotNull(message = "価格帯の下限を入力してください。")
	@Min(value = 1, message = "価格帯は1円以上に設定してください。")
	private Integer lowestPrice;
	
	@NotNull(message = "価格帯の上限を入力してください。")
	@Min(value = 1, message = "価格帯は1円以上に設定してください。")
	private Integer highestPrice;
	
	@NotNull(message = "開店時間を入力してください。")
	private LocalTime openingTime;
	
	@NotNull(message = "閉店時間を入力してください。")
	private LocalTime closingTime;
	
	private List<Integer> weeklyHolidayIds;
	
	private Boolean crossDay;
		
	@NotNull(message = "座席数を入力してください")
	@Min(value = 1, message = "座席数は1つ以上に設定してください。")
	private Integer numberOfSeats;
	
	@NotBlank(message = "郵便番号を入力してください。")
	private String postalCode;
	
	@NotBlank(message = "住所を入力してください。")
	private String address;
	
	@NotBlank(message = "電話番号を入力してください。")
	private String phoneNumber;
	
	
}
