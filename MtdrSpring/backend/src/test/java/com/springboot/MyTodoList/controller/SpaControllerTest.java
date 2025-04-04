package com.springboot.MyTodoList.controller;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class SpaControllerTest {

    private final SpaController spaController = new SpaController();

    @Test
    void testForward() {
        String result = spaController.forward();
        assertEquals("forward:/index.html", result);
    }
}