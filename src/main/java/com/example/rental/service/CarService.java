package com.example.rental.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.rental.entity.Car;
import com.example.rental.mapper.CarMapper;

@Service
public class CarService {

	private final CarMapper carMapper;

	public CarService(CarMapper carMapper) {
		this.carMapper = carMapper;
	}

	public List<Car> findAll() {
		return carMapper.findAll();
	}

}
