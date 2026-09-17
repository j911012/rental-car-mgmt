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
import com.example.rental.entity.CarStatus;
import com.example.rental.form.CarSearchForm;

@MybatisTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Sql(statements = { "DELETE FROM car", CarMapperTest.INSERT_PRIUS, CarMapperTest.INSERT_AQUA,
		CarMapperTest.INSERT_PRIUS_ALPHA })
class CarMapperTest {

	// 全テスト共通のデータ(車種名・ステータスの組み合わせで検索の分岐ごとに結果が変わるようにする)
	static final String INSERT_PRIUS = "INSERT INTO car (car_id, car_name, number_plate, status, created_at, updated_at)"
			+ " VALUES (9901, 'プリウス', '品川500あ1234', 'AVAILABLE', '2026-01-01 10:00:00', '2026-01-02 11:00:00')";

	static final String INSERT_AQUA = "INSERT INTO car (car_id, car_name, number_plate, status, created_at, updated_at)"
			+ " VALUES (9902, 'アクア', '品川500あ5678', 'RENTED', '2026-02-03 12:00:00', '2026-02-04 13:00:00')";

	static final String INSERT_PRIUS_ALPHA = "INSERT INTO car (car_id, car_name, number_plate, status, created_at, updated_at)"
			+ " VALUES (9903, 'プリウスα', '品川500あ9012', 'RENTED', '2026-03-05 14:00:00', '2026-03-06 15:00:00')";

	@Autowired
	private CarMapper carMapper;

	@Test
	void search_条件なしの場合は全件を車両ID順にマッピングして返す() {
		List<Car> cars = carMapper.search(new CarSearchForm());

		assertThat(cars)
				.extracting(Car::getCarId, Car::getCarName, Car::getNumberPlate, Car::getStatus, Car::getCreatedAt,
						Car::getUpdatedAt)
				.containsExactly(
						tuple(9901, "プリウス", "品川500あ1234", CarStatus.AVAILABLE,
								LocalDateTime.of(2026, 1, 1, 10, 0, 0),
								LocalDateTime.of(2026, 1, 2, 11, 0, 0)),
						tuple(9902, "アクア", "品川500あ5678", CarStatus.RENTED,
								LocalDateTime.of(2026, 2, 3, 12, 0, 0),
								LocalDateTime.of(2026, 2, 4, 13, 0, 0)),
						tuple(9903, "プリウスα", "品川500あ9012", CarStatus.RENTED,
								LocalDateTime.of(2026, 3, 5, 14, 0, 0),
								LocalDateTime.of(2026, 3, 6, 15, 0, 0)));
	}

	@Test
	void search_車種名のみ指定した場合は部分一致で絞り込む() {
		CarSearchForm form = new CarSearchForm();
		form.setCarName("プリウス");

		List<Car> cars = carMapper.search(form);

		assertThat(cars).extracting(Car::getCarId).containsExactly(9901, 9903);
	}

	@Test
	void search_ステータスのみ指定した場合は完全一致で絞り込む() {
		CarSearchForm form = new CarSearchForm();
		form.setStatus(CarStatus.RENTED);

		List<Car> cars = carMapper.search(form);

		assertThat(cars).extracting(Car::getCarId).containsExactly(9902, 9903);
	}

	@Test
	void search_車種名とステータスの両方を指定した場合はAND条件で絞り込む() {
		CarSearchForm form = new CarSearchForm();
		form.setCarName("プリウス");
		form.setStatus(CarStatus.RENTED);

		List<Car> cars = carMapper.search(form);

		assertThat(cars).extracting(Car::getCarId).containsExactly(9903);
	}

	@Test
	void search_車種名が空文字の場合は条件から除外される() {
		CarSearchForm form = new CarSearchForm();
		form.setCarName("");

		List<Car> cars = carMapper.search(form);

		assertThat(cars).extracting(Car::getCarId).containsExactly(9901, 9902, 9903);
	}

	@Test
	void search_該当する車両が無い場合は空リストを返す() {
		CarSearchForm form = new CarSearchForm();
		form.setCarName("フィット");

		List<Car> cars = carMapper.search(form);

		assertThat(cars).isEmpty();
	}

}
