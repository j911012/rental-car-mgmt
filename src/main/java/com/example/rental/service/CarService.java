package com.example.rental.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.rental.entity.Car;
import com.example.rental.mapper.CarMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CarService {

	private final CarMapper carMapper;

	public List<Car> findAll() {
		return carMapper.findAll();
	}

}
