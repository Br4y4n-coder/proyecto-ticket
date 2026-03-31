package com.proyectoarquitectura.app.controller;

import com.proyectoarquitectura.app.models.dto.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/healthChecksController")
public class HealthChecksController {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public HealthChecksController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @PostMapping("/health-database")
    public ResponseEntity<ApiResponse<Map<String, Object>>> healthDatabase() {
        long start = System.currentTimeMillis();

        try {
            Integer result = jdbcTemplate.queryForObject("SELECT 1", Integer.class);

            long time = System.currentTimeMillis() - start;

            // 🔥 validación real
            if (result == null || result != 1) {
                return ResponseEntity.status(500).body(
                        ApiResponse.<Map<String, Object>>builder()
                                .status(500)
                                .message("Respuesta inválida de la base de datos")
                                .data(Map.of("database", "DOWN"))
                                .timestamp(Instant.now().toEpochMilli())
                                .build()
                );
            }

            // ⚠️ detección de degradación
            String status = time > 2000 ? "DEGRADED" : "UP";

            return ResponseEntity.ok(
                    ApiResponse.<Map<String, Object>>builder()
                            .status(200)
                            .message("Base de datos operativa")
                            .data(Map.of("database", status, "responseTimeMs", time))
                            .timestamp(Instant.now().toEpochMilli())
                            .build()
            );
        } catch (Exception e) {
            log.error(e.getMessage());
            return ResponseEntity.status(500).body(
                    ApiResponse.<Map<String, Object>>builder()
                            .status(500)
                            .message("Error de conexión con la base de datos")
                            .data(Map.of(
                                    "database", "DOWN"
                            ))
                            .timestamp(Instant.now().toEpochMilli())
                            .build()
            );
        }
    }


}
