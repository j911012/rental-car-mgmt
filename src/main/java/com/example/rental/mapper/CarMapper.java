package com.example.rental.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.example.rental.entity.Car;

@Mapper
public interface CarMapper {

	List<Car> findAll();

}
