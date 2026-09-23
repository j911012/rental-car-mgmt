package com.example.rental.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

@WebMvcTest(CarEditController.class)
class CarEditControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Test
  void showEditForm_編集画面が表示されてURLの車両IDがModelに入る() throws Exception {
    mockMvc.perform(MockMvcRequestBuilders.get("/cars/1/edit"))
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andExpect(MockMvcResultMatchers.view().name("car/edit"))
        .andExpect(MockMvcResultMatchers.model().attribute("carId", 1));
  }

}
