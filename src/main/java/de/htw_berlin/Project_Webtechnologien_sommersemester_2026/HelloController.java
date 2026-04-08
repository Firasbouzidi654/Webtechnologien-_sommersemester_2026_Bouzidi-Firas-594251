package de.htw_berlin.Project_Webtechnologien_sommersemester_2026;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {

    @GetMapping("/")
    public String home() {
        return "Spring works";
    }
}