package com.example.rental.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import com.example.rental.entity.Car;
import com.example.rental.entity.CarStatus;
import com.example.rental.form.CarSearchForm;
import com.example.rental.service.CarService;

@WebMvcTest(CarListController.class)
class CarListControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private CarService carService;

	@Test
	void list_条件なしで一覧画面に必要な属性が入る() throws Exception {
		Car car = new Car();
		car.setCarId(1);
		car.setCarName("プリウス");
		car.setNumberPlate("品川500あ1234");
		car.setStatus(CarStatus.AVAILABLE);
		when(carService.search(any(CarSearchForm.class))).thenReturn(List.of(car));

		mockMvc.perform(MockMvcRequestBuilders.get("/cars"))
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.view().name("car/list"))
				.andExpect(MockMvcResultMatchers.model().attributeExists("carList", "statusList", "carSearchForm"));
	}

	@Test
	void list_検索条件がFormにバインドされてServiceに渡る() throws Exception {
		when(carService.search(any(CarSearchForm.class))).thenReturn(List.of());

		mockMvc.perform(MockMvcRequestBuilders.get("/cars")
				.param("carName", "プリ")
				.param("status", "RENTED"))
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.view().name("car/list"));

		ArgumentCaptor<CarSearchForm> captor = ArgumentCaptor.forClass(CarSearchForm.class);
		verify(carService).search(captor.capture());
		assertThat(captor.getValue().getCarName()).isEqualTo("プリ");
		assertThat(captor.getValue().getStatus()).isEqualTo(CarStatus.RENTED);
	}

}
