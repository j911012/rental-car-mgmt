package com.example.rental.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.example.rental.service.CarService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class CarListController {

	private final CarService carService;

	@GetMapping("/cars")
	public String list(Model model) {
		model.addAttribute("carList", carService.findAll());
		return "car/list";
	}

}
