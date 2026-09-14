package cl.duoc.bancoxyz.tasklet;

import cl.duoc.bancoxyz.model.CuentaAnual;
import cl.duoc.bancoxyz.model.ResumenCuentaAnual;
import cl.duoc.bancoxyz.repository.CuentaAnualRepository;
import cl.duoc.bancoxyz.repository.ResumenCuentaAnualRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Tasklet encargado del Step 2 en cuentasAnualesJob.
 * Agrega y totaliza los movimientos anuales por cuenta para generar
 * el balance consolidado requerido para auditorias.
 * 
 * @author Duoc UC - Desarrollo Backend III
 * @version 3.0
 */
@Component
public class CuentasAnualesAggregationTasklet implements Tasklet {

    private static final Logger log = LoggerFactory.getLogger(CuentasAnualesAggregationTasklet.class);

    private final CuentaAnualRepository cuentaAnualRepository;
    private final ResumenCuentaAnualRepository resumenRepository;

    public CuentasAnualesAggregationTasklet(CuentaAnualRepository cuentaAnualRepository,
                                           ResumenCuentaAnualRepository resumenRepository) {
        this.cuentaAnualRepository = cuentaAnualRepository;
        this.resumenRepository = resumenRepository;
    }

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        log.info(">>> Iniciando agregacion y balance consolidado por cuenta...");

        List<CuentaAnual> movimientos = cuentaAnualRepository.findAll();
        if (movimientos.isEmpty()) {
            log.warn("No se encontraron movimientos anuales para procesar.");
            return RepeatStatus.FINISHED;
        }

        // Agrupar movimientos por cuentaId
        Map<Long, List<CuentaAnual>> movimientosPorCuenta = movimientos.stream()
                .collect(Collectors.groupingBy(CuentaAnual::getCuentaId));

        for (Map.Entry<Long, List<CuentaAnual>> entry : movimientosPorCuenta.entrySet()) {
            Long cuentaId = entry.getKey();
            List<CuentaAnual> lista = entry.getValue();

            BigDecimal totalIngresos = BigDecimal.ZERO;
            BigDecimal totalEgresos = BigDecimal.ZERO;

            for (CuentaAnual c : lista) {
                if (c.getMonto() != null) {
                    BigDecimal montoDecimal = BigDecimal.valueOf(c.getMonto());
                    if (montoDecimal.compareTo(BigDecimal.ZERO) > 0) {
                        totalIngresos = totalIngresos.add(montoDecimal);
                    } else if (montoDecimal.compareTo(BigDecimal.ZERO) < 0) {
                        totalEgresos = totalEgresos.add(montoDecimal.abs());
                    }
                }
            }

            BigDecimal balanceNeto = totalIngresos.subtract(totalEgresos);
            String estadoAuditoria = balanceNeto.compareTo(BigDecimal.ZERO) >= 0 ? "SUPERAVIT" : "DEFICIT";

            ResumenCuentaAnual resumen = new ResumenCuentaAnual(
                    String.valueOf(cuentaId),
                    totalIngresos,
                    totalEgresos,
                    balanceNeto,
                    lista.size(),
                    estadoAuditoria
            );

            resumenRepository.save(resumen);
            log.info("   * Cuenta {}: Ingresos={}, Egresos={}, Balance Neto={}, Movimientos={}, Estado={}",
                    cuentaId, totalIngresos, totalEgresos, balanceNeto, lista.size(), estadoAuditoria);
        }

        log.info(">>> Agregacion anual completada con exito. Total cuentas consolidadas: {}", movimientosPorCuenta.size());
        return RepeatStatus.FINISHED;
    }
}