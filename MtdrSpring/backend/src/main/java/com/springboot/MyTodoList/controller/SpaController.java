package com.springboot.MyTodoList.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class SpaController {
    
    /**
     * Forward all requests that are not REST API calls, static resources, or Swagger UI to the index.html
     * This allows the frontend router to handle client-side routing
     */
    @RequestMapping(value = "/{path:[^.]*}")
    public String forward() {
        return "forward:/index.html";
    }
    
    /**
     * Handle Swagger UI root path redirect
     */
    @RequestMapping("/swagger-ui")
    public String swaggerRedirect() {
        return "redirect:/swagger-ui/index.html";
    }
}
