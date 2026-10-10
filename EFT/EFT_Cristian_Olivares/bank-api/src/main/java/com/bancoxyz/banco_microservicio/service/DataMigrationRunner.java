package com.bancoxyz.banco_microservicio.service;

import com.bancoxyz.banco_microservicio.model.CuentaAnual;
import com.bancoxyz.banco_microservicio.model.Interes;
import com.bancoxyz.banco_microservicio.model.Transaccion;
import com.bancoxyz.banco_microservicio.repository.CuentaAnualRepository;
import com.bancoxyz.banco_microservicio.repository.InteresRepository;
import com.bancoxyz.banco_microservicio.repository.TransaccionRepository;
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
    private final InteresRepository interesRepo;
    private final TransaccionRepository transaccionRepo;

    public DataMigrationRunner(CuentaAnualRepository cuentaRepo,
                               InteresRepository interesRepo,
                               TransaccionRepository transaccionRepo) {
        this.cuentaRepo = cuentaRepo;
        this.interesRepo = interesRepo;
        this.transaccionRepo = transaccionRepo;
    }

    @Override
    public void run(String... args) {
        log.info("=== Iniciando migración de datos legacy ===");
        migrarCuentasAnuales();
        migrarIntereses();
        migrarTransacciones();
        log.info("=== Migración finalizada ===");
        log.info("Cuentas anuales: {}", cuentaRepo.count());
        log.info("Intereses: {}", interesRepo.count());
        log.info("Transacciones: {}", transaccionRepo.count());
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

    private void migrarCuentasAnuales() {
        int ok = 0, err = 0;
        try (BufferedReader br = new BufferedReader(new InputStreamReader(
                new ClassPathResource("data/legacy/cuentas_anuales.csv").getInputStream(),
                StandardCharsets.UTF_8))) {
            br.readLine();
            String line;
            while ((line = br.readLine()) != null) {
                try {
                    String[] p = line.split(",", -1);
                    CuentaAnual c = new CuentaAnual();
                    c.setCuentaId(parseLong(p[0]));
                    c.setFecha(safe(p[1]));
                    c.setTransaccion(safe(p[2]));
                    c.setMonto(parseDouble(p[3]));
                    c.setDescripcion(p.length > 4 ? safe(p[4]) : "");
                    cuentaRepo.save(c);
                    ok++;
                } catch (Exception e) { err++; }
            }
        } catch (Exception e) {
            log.error("Error leyendo cuentas_anuales.csv: {}", e.getMessage());
        }
        log.info("Cuentas anuales migradas: {} (errores: {})", ok, err);
    }

    private void migrarIntereses() {
        int ok = 0, err = 0;
        try (BufferedReader br = new BufferedReader(new InputStreamReader(
                new ClassPathResource("data/legacy/intereses.csv").getInputStream(),
                StandardCharsets.UTF_8))) {
            br.readLine();
            String line;
            while ((line = br.readLine()) != null) {
                try {
                    String[] p = line.split(",", -1);
                    Interes i = new Interes();
                    i.setCuentaId(parseLong(p[0]));
                    i.setNombre(safe(p[1]));
                    i.setSaldo(parseDouble(p[2]));
                    i.setEdad(parseInt(p[3]));
                    i.setTipo(safe(p[4]));
                    interesRepo.save(i);
                    ok++;
                } catch (Exception e) { err++; }
            }
        } catch (Exception e) {
            log.error("Error leyendo intereses.csv: {}", e.getMessage());
        }
        log.info("Intereses migrados: {} (errores: {})", ok, err);
    }

    private void migrarTransacciones() {
        int ok = 0, err = 0;
        try (BufferedReader br = new BufferedReader(new InputStreamReader(
                new ClassPathResource("data/legacy/transacciones.csv").getInputStream(),
                StandardCharsets.UTF_8))) {
            br.readLine();
            String line;
            while ((line = br.readLine()) != null) {
                try {
                    String[] p = line.split(",", -1);
                    Transaccion t = new Transaccion();
                    t.setId(parseLong(p[0]));
                    t.setFecha(safe(p[1]));
                    t.setMonto(parseDouble(p[2]));
                    t.setTipo(safe(p[3]));
                    transaccionRepo.save(t);
                    ok++;
                } catch (Exception e) { err++; }
            }
        } catch (Exception e) {
            log.error("Error leyendo transacciones.csv: {}", e.getMessage());
        }
        log.info("Transacciones migradas: {} (errores: {})", ok, err);
    }
}