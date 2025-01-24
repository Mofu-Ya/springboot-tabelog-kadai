package com.example.samuraitabelog.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.samuraitabelog.entity.Gender;

public interface GenderRepository extends JpaRepository<Gender, Integer>{

}
