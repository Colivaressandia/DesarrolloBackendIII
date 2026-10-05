package com.bancoxyz.cuentas_api.service;

import com.bancoxyz.cuentas_api.model.CuentaAnual;
import com.bancoxyz.cuentas_api.repository.CuentaAnualRepository;
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

    private final CuentaAnualRepository cuentaRepo;

    public DataMigrationRunner(CuentaAnualRepository cuentaRepo) {
        this.cuentaRepo = cuentaRepo;
    }

    @Override
    public void run(String... args) {
        log.info("=== Iniciando migración de cuentas anuales ===");
        int ok = 0, err = 0;

        try (BufferedReader br = new BufferedReader(new InputStreamReader(
                new ClassPathResource("data/legacy/cuentas_anuales.csv").getInputStream(),
                StandardCharsets.UTF_8))) {

            br.readLine(); // saltar encabezado
            String line;
            while ((line = br.readLine()) != null) {
                try {
                    String[] p = line.replace("\r", "").trim().split(",", -1);
                    CuentaAnual c = new CuentaAnual();
                    c.setCuentaId(parseLong(p[0]));
                    c.setFecha(safe(p[1]));
                    c.setTransaccion(safe(p[2]));
                    c.setMonto(parseDouble(p[3]));
                    c.setDescripcion(p.length > 4 ? safe(p[4]) : "");
                    cuentaRepo.save(c);
                    ok++;
                } catch (Exception e) {
                    err++;
                }
            }
        } catch (Exception e) {
            log.error("Error leyendo cuentas_anuales.csv: {}", e.getMessage());
        }

        log.info("Cuentas anuales migradas: {} (errores: {})", ok, err);
        log.info("Total en BD: {}", cuentaRepo.count());
        log.info("=== Migración finalizada ===");
    }

    private Double parseDouble(String s) {
        if (s == null || s.isBlank()) return null;
        try { return Double.parseDouble(s.trim()); } catch (NumberFormatException e) { return null; }
    }

    private Long parseLong(String s) {
        if (s == null || s.isBlank()) return null;
        try { return Long.parseLong(s.trim()); } catch (NumberFormatException e) { return null; }
    }

    private String safe(String s) { return s == null ? "" : s.trim(); }
}