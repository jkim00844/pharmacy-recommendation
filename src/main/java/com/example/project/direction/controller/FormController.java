package com.example.project.direction.controller;

import com.example.project.direction.dto.InputDto;
import com.example.project.pharmacy.service.PharmacyRecommendationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequiredArgsConstructor
public class FormController {

    private final PharmacyRecommendationService pharmacyRecommendationService;
    @GetMapping("/")
    public String main(){
        return "main";
    }

    @GetMapping("/search")
    public ModelAndView postDirection(@ModelAttribute InputDto inputDto){

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("output");
        modelAndView.addObject("outputList",
            pharmacyRecommendationService.recommendPharmacyList(inputDto.getAddress()));
        return modelAndView;
    }
}
