package com.example.samuraitabelog.common;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ValidateReserveError {
    private String field;   // エラーが発生したフィールド名
    private String message; // エラーメッセージ

}

