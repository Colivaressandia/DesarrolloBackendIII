package cl.duoc.bancoxyz.processor;

import cl.duoc.bancoxyz.dto.CuentaAnualDTO;
import cl.duoc.bancoxyz.model.CuentaAnual;
import org.springframework.batch.item.ItemProcessor;
import java.time.LocalDate;

/**
 * Procesador que clasifica los movimientos anuales con fines de auditoría financiera.
 */
public class CuentaAnualProcessor implements ItemProcessor<CuentaAnualDTO, CuentaAnual> {

    @Override
    public CuentaAnual process(CuentaAnualDTO item) throws Exception {
        double monto = item.getMonto() != null ? item.getMonto() : 0.0;

        // Regla 1: Clasificación de auditoría según la naturaleza del movimiento
        String clasificacion;
        if (monto == 0.0) {
            clasificacion = "MONTO_NULO";
        } else if (monto < 0.0) {
            clasificacion = "EGRESO";
        } else {
            clasificacion = "INGRESO";
        }

        LocalDate fecha = LocalDate.parse(item.getFecha().trim());
        String tipoTransaccion = item.getTransaccion() != null ? item.getTransaccion().trim().toLowerCase() : "general";
        String descripcion = item.getDescripcion() != null ? item.getDescripcion().trim() : "";

        return new CuentaAnual(
            item.getCuentaId(),
            fecha,
            tipoTransaccion,
            monto,
            descripcion,
            clasificacion
        );
    }
}