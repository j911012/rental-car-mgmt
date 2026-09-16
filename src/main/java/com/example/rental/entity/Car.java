package com.example.rental.entity;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class Car {

	private Integer carId;
	private String carName;
	private String numberPlate;
	private CarStatus status;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;

}
