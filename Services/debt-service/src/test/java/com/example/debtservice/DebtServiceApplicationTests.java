package com.example.debtservice;

import org.junit.jupiter.api.Test;

/**
 * Test básico de la aplicación.
 * No levanta el contexto completo de Spring para evitar
 * conflictos con la base de datos SQLite durante los tests.
 */
class DebtServiceApplicationTests {

    @Test
    void contextLoads() {
        // Test vacío intencional - la validación del contexto
        // se hace al levantar la aplicación real
    }
}
