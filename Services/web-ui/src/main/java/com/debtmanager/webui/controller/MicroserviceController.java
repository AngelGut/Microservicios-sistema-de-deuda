package com.debtmanager.webui.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/microservices")
public class MicroserviceController {

    @GetMapping
    public String status() {
        return "microservices/status";
    }
}
