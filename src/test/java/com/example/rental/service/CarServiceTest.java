package com.example.rental.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.rental.entity.Car;
import com.example.rental.entity.CarStatus;
import com.example.rental.exception.BusinessException;
import com.example.rental.form.CarForm;
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

	@Test
	void regist_正常時はFormの内容でinsertされる() {
		CarForm form = carForm("フィット", "品川500あ3456", CarStatus.MAINTENANCE);
		when(carMapper.countByNumberPlate("品川500あ3456", null)).thenReturn(0);

		CarService carService = new CarService(carMapper);
		carService.regist(form);

		ArgumentCaptor<Car> captor = ArgumentCaptor.forClass(Car.class);
		verify(carMapper).insert(captor.capture());
		assertThat(captor.getValue().getCarName()).isEqualTo("フィット");
		assertThat(captor.getValue().getNumberPlate()).isEqualTo("品川500あ3456");
		assertThat(captor.getValue().getStatus()).isEqualTo(CarStatus.MAINTENANCE);
	}

	@Test
	void regist_ナンバーが重複していたら例外を投げinsertしない() {
		CarForm form = carForm("フィット", "品川500あ1234", CarStatus.AVAILABLE);
		when(carMapper.countByNumberPlate("品川500あ1234", null)).thenReturn(1);

		CarService carService = new CarService(carMapper);

		assertThatThrownBy(() -> carService.regist(form))
				.isInstanceOf(BusinessException.class)
				.satisfies(e -> {
					BusinessException be = (BusinessException) e;
					assertThat(be.getField()).isEqualTo("numberPlate");
					assertThat(be.getMessageKey()).isEqualTo("carForm.numberPlate.duplicate");
				});
		verify(carMapper, never()).insert(any());
	}

	@Test
	void regist_ステータスにRENTEDを指定したら例外を投げinsertしない() {
		CarForm form = carForm("フィット", "品川500あ3456", CarStatus.RENTED);

		CarService carService = new CarService(carMapper);

		assertThatThrownBy(() -> carService.regist(form))
				.isInstanceOf(BusinessException.class)
				.satisfies(e -> assertThat(((BusinessException) e).getField()).isEqualTo("status"));
		verify(carMapper, never()).insert(any());
	}

	private CarForm carForm(String carName, String numberPlate, CarStatus status) {
		CarForm form = new CarForm();
		form.setCarName(carName);
		form.setNumberPlate(numberPlate);
		form.setStatus(status);
		return form;
	}

}
