package cl.duoc.bancoxyz.processor;

import cl.duoc.bancoxyz.dto.InteresDTO;
import cl.duoc.bancoxyz.model.InteresCuenta;
import org.springframework.batch.item.ItemProcessor;

/**
 * Procesador que calcula el interés mensual y el saldo final para cada cuenta.
 * Si una cuenta tiene saldo inválido (menor o igual a cero), se filtra retornando 'null'.
 */
public class InteresProcessor implements ItemProcessor<InteresDTO, InteresCuenta> {

    @Override
    public InteresCuenta process(InteresDTO item) throws Exception {
        // Regla 1: Validación y filtrado. En Spring Batch, retornar 'null' omite el registro
        if (item.getSaldo() == null || item.getSaldo() <= 0.0) {
            return null;
        }

        // Regla 2: Determinación de la tasa de interés según tipo de producto bancario
        String tipo = item.getTipo() != null ? item.getTipo().trim().toLowerCase() : "general";
        double tasa = switch (tipo) {
            case "ahorro"   -> 0.05;  // 5% de interés
            case "prestamo" -> 0.08;  // 8% de interés
            case "hipoteca" -> 0.035; // 3.5% de interés
            default         -> 0.02;  // 2% base para otros productos
        };

        // Regla 3: Cálculo financiero de interés y saldo final
        double interes = item.getSaldo() * tasa;
        double saldoFinal = item.getSaldo() + interes;

        return new InteresCuenta(
            item.getCuentaId(),
            item.getNombre().trim(),
            item.getSaldo(),
            item.getEdad(),
            tipo,
            tasa,
            interes,
            saldoFinal
        );
    }
}