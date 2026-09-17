package com.example.rental.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.rental.entity.Car;
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

}
