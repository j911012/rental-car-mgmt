package com.example.rental.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.rental.entity.Car;
import com.example.rental.entity.CarStatus;
import com.example.rental.exception.BusinessException;
import com.example.rental.form.CarForm;
import com.example.rental.form.CarSearchForm;
import com.example.rental.mapper.CarMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CarService {

	private final CarMapper carMapper;

	public List<Car> search(CarSearchForm form) {
		return carMapper.search(form);
	}

	public void regist(CarForm form) {
		if (form.getStatus() == CarStatus.RENTED) {
			throw new BusinessException("status", "carForm.status.rentedNotSelectable");
		}
		if (carMapper.countByNumberPlate(form.getNumberPlate(), null) > 0) {
			throw new BusinessException("numberPlate", "carForm.numberPlate.duplicate");
		}

		Car car = new Car();
		car.setCarName(form.getCarName());
		car.setNumberPlate(form.getNumberPlate());
		car.setStatus(form.getStatus());
		carMapper.insert(car);
	}

}
