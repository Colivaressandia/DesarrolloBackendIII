package cl.duoc.bancoxyz.legacy.service;

import cl.duoc.bancoxyz.legacy.model.CuentaEntity;
import cl.duoc.bancoxyz.legacy.model.MovimientoAnualEntity;
import cl.duoc.bancoxyz.legacy.model.TransaccionLegacyEntity;
import cl.duoc.bancoxyz.legacy.repository.CuentaRepository;
import cl.duoc.bancoxyz.legacy.repository.MovimientoAnualRepository;
import cl.duoc.bancoxyz.legacy.repository.TransaccionLegacyRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class DataLoaderService {

    private final CuentaRepository cuentaRepository;
    private final MovimientoAnualRepository movimientoRepository;
    private final TransaccionLegacyRepository transaccionRepository;

    @Value("${legacy.data.path:../bank_legacy_data-main/data/semana_3}")
    private String dataFolderPath;

    private static final DateTimeFormatter[] DATE_FORMATTERS = new DateTimeFormatter[]{
            DateTimeFormatter.ofPattern("yyyy-MM-dd"),
            DateTimeFormatter.ofPattern("yyyy/MM/dd"),
            DateTimeFormatter.ofPattern("dd-MM-yyyy"),
            DateTimeFormatter.ofPattern("dd/MM/yyyy")
    };

    public DataLoaderService(CuentaRepository cuentaRepository,
                             MovimientoAnualRepository movimientoRepository,
                             TransaccionLegacyRepository transaccionRepository) {
        this.cuentaRepository = cuentaRepository;
        this.movimientoRepository = movimientoRepository;
        this.transaccionRepository = transaccionRepository;
    }

    @PostConstruct
    public void init() {
        if (cuentaRepository.count() == 0) {
            cargarCuentasEIntereses();
        }
        if (movimientoRepository.count() == 0) {
            cargarMovimientosAnuales();
        }
        if (transaccionRepository.count() == 0) {
            cargarTransaccionesLog();
        }
    }

    private void cargarCuentasEIntereses() {
        File file = new File(dataFolderPath, "intereses.csv");
        if (!file.exists()) {
            System.err.println("No se encontro el archivo intereses.csv en: " + file.getAbsolutePath());
            return;
        }

        List<CuentaEntity> cuentas = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line = br.readLine();
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] cols = line.split(",", -1);
                if (cols.length < 5) continue;

                Long cuentaId = parseLongOrNull(cols[0]);
                if (cuentaId == null) continue;

                String nombre = cols[1].trim();
                BigDecimal saldo = parseBigDecimalOrZero(cols[2]);
                Integer edad = parseIntOrNull(cols[3]);
                String tipo = cols[4].trim();

                boolean esValida = !nombre.equalsIgnoreCase("Unknown")
                        && !tipo.equals("-1")
                        && !tipo.equalsIgnoreCase("unknown")
                        && (edad == null || (edad >= 18 && edad <= 99));

                cuentas.add(CuentaEntity.builder()
                        .cuentaId(cuentaId)
                        .nombre(nombre.isEmpty() ? "Unknown" : nombre)
                        .saldo(saldo)
                        .edad(edad)
                        .tipo(tipo.isEmpty() ? "unknown" : tipo)
                        .esValida(esValida)
                        .build());
            }
            cuentaRepository.saveAll(cuentas);
            System.out.println("Carga completada exitosamente desde intereses.csv. Total registros: " + cuentas.size());
        } catch (Exception e) {
            System.err.println("Error al cargar intereses.csv: " + e.getMessage());
        }
    }

    private void cargarMovimientosAnuales() {
        File file = new File(dataFolderPath, "cuentas_anuales.csv");
        if (!file.exists()) {
            System.err.println("No se encontro el archivo cuentas_anuales.csv en: " + file.getAbsolutePath());
            return;
        }

        List<MovimientoAnualEntity> movimientos = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line = br.readLine();
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] cols = line.split(",", -1);
                if (cols.length < 5) continue;

                Long cuentaId = parseLongOrNull(cols[0]);
                if (cuentaId == null) continue;

                String rawFecha = cols[1].trim();
                LocalDate fecha = parseFlexibleDate(rawFecha);
                String transaccion = cols[2].trim().toLowerCase().replace("depósito", "deposito");
                BigDecimal monto = parseBigDecimalOrZero(cols[3]);
                String descripcion = cols[4].trim();

                movimientos.add(MovimientoAnualEntity.builder()
                        .cuentaId(cuentaId)
                        .fecha(fecha)
                        .fechaRaw(rawFecha)
                        .transaccion(transaccion)
                        .monto(monto)
                        .descripcion(descripcion)
                        .build());
            }
            movimientoRepository.saveAll(movimientos);
            System.out.println("Carga completada exitosamente desde cuentas_anuales.csv. Total registros: " + movimientos.size());
        } catch (Exception e) {
            System.err.println("Error al cargar cuentas_anuales.csv: " + e.getMessage());
        }
    }

    private void cargarTransaccionesLog() {
        File file = new File(dataFolderPath, "transacciones.csv");
        if (!file.exists()) {
            System.err.println("No se encontro el archivo transacciones.csv en: " + file.getAbsolutePath());
            return;
        }

        List<TransaccionLegacyEntity> logs = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line = br.readLine();
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] cols = line.split(",", -1);
                if (cols.length < 4) continue;

                Long id = parseLongOrNull(cols[0]);
                if (id == null) continue;

                String rawFecha = cols[1].trim();
                LocalDate fecha = parseFlexibleDate(rawFecha);
                BigDecimal monto = parseBigDecimalOrZero(cols[2]);
                String tipo = cols[3].trim().toLowerCase();

                String estadoRegistro = "VALIDO";
                if (fecha == null || tipo.equals("invalid") || tipo.equals("desconocido") || monto.compareTo(BigDecimal.ZERO) <= 0) {
                    estadoRegistro = "ANOMALIA";
                }

                logs.add(TransaccionLegacyEntity.builder()
                        .id(id)
                        .fecha(fecha)
                        .fechaRaw(rawFecha)
                        .monto(monto)
                        .tipo(tipo)
                        .estadoRegistro(estadoRegistro)
                        .build());
            }
            transaccionRepository.saveAll(logs);
            System.out.println("Carga completada exitosamente desde transacciones.csv. Total registros: " + logs.size());
        } catch (Exception e) {
            System.err.println("Error al cargar transacciones.csv: " + e.getMessage());
        }
    }

    private LocalDate parseFlexibleDate(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) return null;
        for (DateTimeFormatter formatter : DATE_FORMATTERS) {
            try {
                return LocalDate.parse(dateStr.trim(), formatter);
            } catch (Exception ignored) {
            }
        }
        return null;
    }

    private Long parseLongOrNull(String str) {
        try {
            return Long.parseLong(str.trim());
        } catch (Exception e) {
            return null;
        }
    }

    private Integer parseIntOrNull(String str) {
        try {
            return Integer.parseInt(str.trim());
        } catch (Exception e) {
            return null;
        }
    }

    private BigDecimal parseBigDecimalOrZero(String str) {
        try {
            return new BigDecimal(str.trim());
        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }
}