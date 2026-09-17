package com.example.rental.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.rental.entity.Car;
import com.example.rental.entity.CarStatus;
import com.example.rental.form.CarSearchForm;
import com.example.rental.mapper.CarMapper;

@ExtendWith(MockitoExtension.class)
class CarServiceTest {

	@Mock
	private CarMapper carMapper;

	@Test
	void search_受け取ったFormをMapperに渡し結果をそのまま返す() {
		CarSearchForm form = new CarSearchForm();
		form.setCarName("プリウス");
		form.setStatus(CarStatus.AVAILABLE);

		Car car = new Car();
		car.setCarId(1);
		car.setCarName("プリウス");
		car.setNumberPlate("品川500あ1234");
		car.setStatus(CarStatus.AVAILABLE);
		List<Car> expected = List.of(car);
		when(carMapper.search(form)).thenReturn(expected);

		CarService carService = new CarService(carMapper);
		List<Car> actual = carService.search(form);

		assertThat(actual).isEqualTo(expected);
	}

}
