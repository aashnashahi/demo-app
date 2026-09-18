package com.example;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class AppTest {

    @Test
    void greetingIsNotEmpty() {
        assertFalse(App.greeting().isEmpty());
    }

    @Test
    void greetingMentionsJenkins() {
        assertTrue(App.greeting().contains("Jenkins"));
    }
}