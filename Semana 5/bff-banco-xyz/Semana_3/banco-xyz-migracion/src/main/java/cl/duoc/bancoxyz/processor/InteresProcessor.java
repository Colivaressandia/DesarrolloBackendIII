package cl.duoc.bancoxyz.processor;

import cl.duoc.bancoxyz.dto.InteresDTO;
import cl.duoc.bancoxyz.model.InteresCuenta;
import org.springframework.batch.item.ItemProcessor;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Procesador que calcula el interes mensual y el saldo final para cada cuenta.
 * Si una cuenta tiene saldo o edad invalida, se filtra retornando 'null'.
 * Implementa precision financiera formal mediante BigDecimal a 2 decimales.
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
        // Se consideran validas edades entre 1 y 120 anos.
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

        // Regla 4: Calculo financiero de alta precision (BigDecimal con redondeo a 2 decimales).
        BigDecimal saldoInicialBD = BigDecimal.valueOf(item.getSaldo());
        BigDecimal tasaBD = BigDecimal.valueOf(tasa);

        BigDecimal interesCalculadoBD = saldoInicialBD.multiply(tasaBD)
                .setScale(2, RoundingMode.HALF_UP);

        BigDecimal saldoFinalBD = saldoInicialBD.add(interesCalculadoBD)
                .setScale(2, RoundingMode.HALF_UP);

        double interes = interesCalculadoBD.doubleValue();
        double saldoFinal = saldoFinalBD.doubleValue();

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