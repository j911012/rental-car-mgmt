package com.example.rental.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.example.rental.service.CarService;

@Controller
public class C1000ListController {

	private final CarService carService;

	public C1000ListController(CarService carService) {
		this.carService = carService;
	}

	@GetMapping("/c1000list")
	public String list(Model model) {
		model.addAttribute("carList", carService.findAll());
		return "c1000/c1000list";
	}

}
