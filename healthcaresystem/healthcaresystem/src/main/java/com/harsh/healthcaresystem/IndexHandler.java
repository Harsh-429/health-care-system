package com.harsh.healthcaresystem;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class IndexHandler {
    @GetMapping("/")
    public String Home() {
        return "redirect:/index.html";
    }
}
