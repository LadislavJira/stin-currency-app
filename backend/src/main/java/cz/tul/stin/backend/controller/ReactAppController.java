package cz.tul.stin.backend.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;


@Controller
public class ReactAppController {

    @RequestMapping(value = { "/", "/login", "/dashboard", "/settings" })
    public String serveReactApp() {
        return "forward:/index.html";
    }
}