package cl.duoc.bancoxyz.processor;

import cl.duoc.bancoxyz.dto.InteresDTO;
import cl.duoc.bancoxyz.model.InteresCuenta;
import org.springframework.batch.item.ItemProcessor;

/**
 * Procesador que calcula el interes mensual y el saldo final para cada cuenta.
 * Si una cuenta tiene saldo o edad invalida, se filtra retornando 'null'.
 */
public class InteresProcessor implements ItemProcessor<InteresDTO, InteresCuenta> {

    @Override
    public InteresCuenta process(InteresDTO item) throws Exception {

        // Regla 1: Validacion de saldo.
        // En Spring Batch, retornar 'null' omite el registro.
        if (item.getSaldo() == null || item.getSaldo() <= 0.0) {
            return null;
        }

        // Regla 2: Validacion de edad.
        // Se consideran validas edades entre 1 y 120 años.
        if (item.getEdad() == null || item.getEdad() <= 0 || item.getEdad() > 120) {
            return null;
        }

        // Regla 3: Determinacion de la tasa de interes segun tipo de producto bancario.
        String tipo = item.getTipo() != null
                ? item.getTipo().trim().toLowerCase()
                : "general";

        double tasa = switch (tipo) {
            case "ahorro"   -> 0.05;  // 5% de interes
            case "prestamo" -> 0.08;  // 8% de interes
            case "hipoteca" -> 0.035; // 3.5% de interes
            default         -> 0.02;  // 2% base para otros productos
        };

        // Regla 4: Calculo financiero de interes y saldo final.
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