package cl.duoc.bancoxyz.policy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.step.skip.SkipLimitExceededException;
import org.springframework.batch.core.step.skip.SkipPolicy;
import org.springframework.batch.item.file.FlatFileParseException;

/**
 * Politica personalizada de tolerancia a fallos (SkipPolicy) para Banco XYZ.
 * Define las excepciones que seran omitidas durante el procesamiento por lotes
 * y el limite maximo de registros corruptos tolerados por Step.
 * 
 * @author Duoc UC - Desarrollo Backend III
 * @version 2.0
 */
public class CustomSkipPolicy implements SkipPolicy {

    private static final Logger log = LoggerFactory.getLogger(CustomSkipPolicy.class);
    
    /**
     * Limite maximo de registros invalidos que pueden ser omitidos antes de abortar el Job.
     */
    private static final int MAX_SKIP_COUNT = 5;

    /**
     * Evalua si una excepcion ocurrida durante la lectura, procesamiento o escritura debe ser omitida.
     *
     * @param t Excepcion lanzada durante el ciclo batch.
     * @param skipCount Cantidad acumulada de skips realizados hasta el momento.
     * @return {@code true} si el registro debe ser omitido; {@code false} si debe fallar el Step.
     * @throws SkipLimitExceededException Si se excede el limite maximo configurado.
     */
    @Override
    public boolean shouldSkip(Throwable t, long skipCount) throws SkipLimitExceededException {
        if (skipCount >= MAX_SKIP_COUNT) {
            log.error("Se ha excedido el limite maximo de tolerancia a fallos ({} registros omitidos). Abortando Step.", skipCount);
            return false;
        }

        if (t instanceof FlatFileParseException 
                || t instanceof NumberFormatException 
                || t instanceof IllegalArgumentException) {
            log.warn("Tolerancia a fallos activada [Skip #{}]: Registro omitido por error: {}", skipCount + 1, t.getMessage());
            return true;
        }

        return false;
    }
}