package com.example.rental.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.rental.entity.CarStatus;
import com.example.rental.exception.BusinessException;
import com.example.rental.form.CarForm;
import com.example.rental.service.CarService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class CarRegistController {

	private final CarService carService;

	/**
	 * ステータスの選択肢。入力エラーで画面に戻すときも必要になるため、
	 * 各メソッドで詰めずに@ModelAttributeメソッドでModelに載せる。
	 */
	@ModelAttribute("statusList")
	public List<CarStatus> statusList() {
		return CarStatus.selectableValues();
	}

	@GetMapping("/cars/new")
	public String showForm(@ModelAttribute CarForm carForm) {
		return "car/regist";
	}

	@PostMapping("/cars/new")
	public String regist(@Valid @ModelAttribute CarForm carForm, BindingResult bindingResult,
			RedirectAttributes redirectAttributes) {
		if (bindingResult.hasErrors()) {
			return "car/regist";
		}

		try {
			carService.regist(carForm);
		} catch (BusinessException e) {
			if (e.getField() == null) {
				bindingResult.reject(e.getMessageKey());
			} else {
				bindingResult.rejectValue(e.getField(), e.getMessageKey());
			}
			return "car/regist";
		}

		redirectAttributes.addFlashAttribute("completeMessage", "carRegist.complete");
		return "redirect:/cars";
	}

}
