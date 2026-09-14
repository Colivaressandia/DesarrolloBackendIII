package cl.duoc.bancoxyz.processor;

import cl.duoc.bancoxyz.dto.TransaccionDTO;
import cl.duoc.bancoxyz.model.Transaccion;
import org.springframework.batch.item.ItemProcessor;
import java.time.LocalDate;

/**
 * Procesador que implementa ItemProcessor para transformar TransaccionDTO a la entidad Transaccion.
 * Aplica la regla de negocio para la detección de anomalías (montos negativos o en cero).
 */
public class TransaccionProcessor implements ItemProcessor<TransaccionDTO, Transaccion> {

    @Override
    public Transaccion process(TransaccionDTO item) throws Exception {
        // Regla 1: Detección y clasificación de anomalías en el monto
        String estado = "VALIDA";
        if (item.getMonto() == null || item.getMonto() == 0.0) {
            estado = "ANOMALIA_MONTO_CERO";
        } else if (item.getMonto() < 0.0) {
            estado = "ANOMALIA_MONTO_NEGATIVO";
        }

        // Regla 2: Conversión y formateo de datos
        LocalDate fecha = LocalDate.parse(item.getFecha().trim());
        String tipoNormalizado = item.getTipo() != null ? item.getTipo().trim().toLowerCase() : "desconocido";

        // Retorna el objeto transformado listo para ser escrito en la base de datos
        return new Transaccion(
            item.getId(),
            fecha,
            item.getMonto(),
            tipoNormalizado,
            estado
        );
    }
}