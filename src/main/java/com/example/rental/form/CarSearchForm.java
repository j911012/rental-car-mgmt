package com.example.rental.form;

import com.example.rental.entity.CarStatus;

import lombok.Data;

@Data
public class CarSearchForm {

	private String carName;
	private CarStatus status;

}
