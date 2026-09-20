package com.example.rental.form;

import com.example.rental.entity.CarStatus;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CarForm {

	@NotBlank
	@Size(max = 50)
	private String carName;

	@NotBlank
	@Size(max = 20)
	private String numberPlate;

	@NotNull
	private CarStatus status;

}
