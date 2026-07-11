package com.Gestion.PolleriaLatina.controller;

import com.Gestion.PolleriaLatina.service.DashboardService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class DashboardController {

  private final DashboardService dashboardService;

  @GetMapping({ "/", "/dashboard" })
  public String dashboard(Model model) {
    var dashboard = dashboardService.buildSummary();
    String dashboardJson;
    try {
      dashboardJson = new ObjectMapper().writeValueAsString(dashboard);
    } catch (JsonProcessingException e) {
      dashboardJson = "{}";
    }
    model.addAttribute("dashboard", dashboard);
    model.addAttribute("dashboardJson", dashboardJson);
    model.addAttribute("titulo", "Dashboard");
    return "modules/dashboard";
  }
}
