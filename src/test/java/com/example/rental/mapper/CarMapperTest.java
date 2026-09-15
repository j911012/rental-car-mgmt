package com.example.rental.mapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.mybatis.spring.boot.test.autoconfigure.MybatisTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.test.context.jdbc.Sql;

import com.example.rental.entity.Car;

@MybatisTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class CarMapperTest {

	@Autowired
	private CarMapper carMapper;

	@Test
	@Sql(statements = {
			"DELETE FROM car",
			"INSERT INTO car (car_id, car_name, number_plate, status, created_at, updated_at)"
					+ " VALUES (9901, 'プリウス', '品川500あ1234', 'AVAILABLE', '2026-01-01 10:00:00', '2026-01-02 11:00:00')",
			"INSERT INTO car (car_id, car_name, number_plate, status, created_at, updated_at)"
					+ " VALUES (9902, 'アクア', '品川500あ5678', 'RENTED', '2026-02-03 12:00:00', '2026-02-04 13:00:00')" })
	void findAll_登録済みの車両が全件マッピングされて返る() {
		List<Car> cars = carMapper.findAll();

		assertThat(cars)
				.extracting(Car::getCarId, Car::getCarName, Car::getNumberPlate, Car::getStatus, Car::getCreatedAt,
						Car::getUpdatedAt)
				.containsExactlyInAnyOrder(
						tuple(9901, "プリウス", "品川500あ1234", "AVAILABLE",
								LocalDateTime.of(2026, 1, 1, 10, 0, 0),
								LocalDateTime.of(2026, 1, 2, 11, 0, 0)),
						tuple(9902, "アクア", "品川500あ5678", "RENTED",
								LocalDateTime.of(2026, 2, 3, 12, 0, 0),
								LocalDateTime.of(2026, 2, 4, 13, 0, 0)));
	}

	@Test
	@Sql(statements = "DELETE FROM car")
	void findAll_車両が1件も無い場合は空リストを返す() {
		List<Car> cars = carMapper.findAll();

		assertThat(cars).isEmpty();
	}

}
