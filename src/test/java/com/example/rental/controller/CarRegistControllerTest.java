package com.example.rental.controller;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import com.example.rental.entity.CarStatus;
import com.example.rental.exception.BusinessException;
import com.example.rental.form.CarForm;
import com.example.rental.service.CarService;

@WebMvcTest(CarRegistController.class)
class CarRegistControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private CarService carService;

	@Test
	void showForm_登録画面が表示され選択肢にRENTEDが含まれない() throws Exception {
		mockMvc.perform(MockMvcRequestBuilders.get("/cars/new"))
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.view().name("car/regist"))
				.andExpect(MockMvcResultMatchers.model().attributeExists("carForm"))
				.andExpect(MockMvcResultMatchers.model().attribute("statusList",
						CarStatus.selectableValues()))
				.andExpect(MockMvcResultMatchers.model().attribute("statusList",
						not(hasItem(CarStatus.RENTED))));
	}

	@Test
	void regist_入力エラーならリダイレクトせず登録画面に戻る() throws Exception {
		mockMvc.perform(MockMvcRequestBuilders.post("/cars/new")
				.param("carName", "")
				.param("numberPlate", "品川500あ3456")
				.param("status", "AVAILABLE"))
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.view().name("car/regist"))
				.andExpect(MockMvcResultMatchers.model().attributeHasFieldErrors("carForm", "carName"))
				.andExpect(MockMvcResultMatchers.model().attribute("statusList",
						CarStatus.selectableValues()));

		verify(carService, never()).regist(any());
	}

	@Test
	void regist_業務エラーならリダイレクトせず登録画面に戻る() throws Exception {
		doThrow(new BusinessException("numberPlate", "carForm.numberPlate.duplicate"))
				.when(carService).regist(any(CarForm.class));

		mockMvc.perform(MockMvcRequestBuilders.post("/cars/new")
				.param("carName", "フィット")
				.param("numberPlate", "品川500あ1234")
				.param("status", "AVAILABLE"))
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.view().name("car/regist"))
				.andExpect(MockMvcResultMatchers.model().attributeHasFieldErrors("carForm", "numberPlate"))
				.andExpect(MockMvcResultMatchers.model().attribute("statusList",
						CarStatus.selectableValues()));
	}

	@Test
	void regist_成功したら一覧へリダイレクトし完了メッセージを渡す() throws Exception {
		mockMvc.perform(MockMvcRequestBuilders.post("/cars/new")
				.param("carName", "フィット")
				.param("numberPlate", "品川500あ3456")
				.param("status", "MAINTENANCE"))
				.andExpect(MockMvcResultMatchers.status().is3xxRedirection())
				.andExpect(MockMvcResultMatchers.redirectedUrl("/cars"))
				.andExpect(MockMvcResultMatchers.flash().attribute("completeMessage", "carRegist.complete"));

		verify(carService).regist(any(CarForm.class));
	}

}
