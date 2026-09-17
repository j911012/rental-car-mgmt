package com.example.rental.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;

import com.example.rental.entity.CarStatus;
import com.example.rental.form.CarSearchForm;
import com.example.rental.service.CarService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class CarListController {

	private final CarService carService;

	@GetMapping("/cars")
	public String list(@ModelAttribute CarSearchForm carSearchForm, Model model) {
		model.addAttribute("carList", carService.search(carSearchForm));
		model.addAttribute("statusList", CarStatus.values());
		return "car/list";
	}

}
