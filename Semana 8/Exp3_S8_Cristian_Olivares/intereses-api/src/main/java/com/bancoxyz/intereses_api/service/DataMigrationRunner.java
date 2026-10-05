package com.bancoxyz.intereses_api.service;

import com.bancoxyz.intereses_api.model.Interes;
import com.bancoxyz.intereses_api.repository.InteresRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

@Component
public class DataMigrationRunner implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataMigrationRunner.class);

    private final InteresRepository interesRepo;

    public DataMigrationRunner(InteresRepository interesRepo) {
        this.interesRepo = interesRepo;
    }

    @Override
    public void run(String... args) {
        log.info("=== Iniciando migración de intereses ===");
        int ok = 0, err = 0;

        try (BufferedReader br = new BufferedReader(new InputStreamReader(
                new ClassPathResource("data/legacy/intereses.csv").getInputStream(),
                StandardCharsets.UTF_8))) {

            br.readLine();
            String line;
            while ((line = br.readLine()) != null) {
                try {
                    String[] p = line.replace("\r", "").trim().split(",", -1);
                    Interes i = new Interes();
                    i.setCuentaId(parseLong(p[0]));
                    i.setNombre(safe(p[1]));
                    i.setSaldo(parseDouble(p[2]));
                    i.setEdad(parseInt(p[3]));
                    i.setTipo(safe(p[4]));
                    interesRepo.save(i);
                    ok++;
                } catch (Exception e) {
                    err++;
                }
            }
        } catch (Exception e) {
            log.error("Error leyendo intereses.csv: {}", e.getMessage());
        }

        log.info("Intereses migrados: {} (errores: {})", ok, err);
        log.info("Total en BD: {}", interesRepo.count());
        log.info("=== Migración finalizada ===");
    }

    private Double parseDouble(String s) {
        if (s == null || s.isBlank()) return null;
        try { return Double.parseDouble(s.trim()); } catch (NumberFormatException e) { return null; }
    }

    private Integer parseInt(String s) {
        if (s == null || s.isBlank()) return null;
        try { return Integer.parseInt(s.trim()); } catch (NumberFormatException e) { return null; }
    }

    private Long parseLong(String s) {
        if (s == null || s.isBlank()) return null;
        try { return Long.parseLong(s.trim()); } catch (NumberFormatException e) { return null; }
    }

    private String safe(String s) { return s == null ? "" : s.trim(); }
}