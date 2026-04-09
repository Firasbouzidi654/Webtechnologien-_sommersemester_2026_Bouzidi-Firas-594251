package de.htw_berlin.Project_Webtechnologien_sommersemester_2026;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HelloController {

    @GetMapping("/")
    public String home() {
        return "index"; // This will serve index.html from static/
    }

    @GetMapping("/{path:[^\\.]*}")
    public String forward() {
        return "forward:/index.html";
    }
}