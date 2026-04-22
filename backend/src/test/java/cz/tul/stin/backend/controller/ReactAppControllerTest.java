package cz.tul.stin.backend.controller;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ReactAppControllerTest {

    @Test
    void serveReactApp_ShouldReturnForwardToIndex() {
        ReactAppController controller = new ReactAppController();
        String viewName = controller.serveReactApp();
        assertEquals("forward:/index.html", viewName, "Kontroler musí správně přesměrovat React routy na index.html");
    }
}