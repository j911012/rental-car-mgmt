package com.example.rental.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class CarEditController {

  @GetMapping("/cars/{carId}/edit")
  public String showEditForm(@PathVariable Integer carId, Model model) {
    model.addAttribute("carId", carId);
    return "car/edit";
  }
}
