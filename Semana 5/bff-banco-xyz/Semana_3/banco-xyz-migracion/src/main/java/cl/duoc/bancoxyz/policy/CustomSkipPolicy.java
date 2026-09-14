package cl.duoc.bancoxyz.policy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.step.skip.SkipLimitExceededException;
import org.springframework.batch.core.step.skip.SkipPolicy;
import org.springframework.batch.item.file.FlatFileParseException;

/**
 * Politica personalizada de tolerancia a fallos (SkipPolicy) parametrizable.
 * Omite errores de formato de archivos y parseo numerico hasta un limite definido.
 * 
 * @author Duoc UC - Desarrollo Backend III
 * @version 3.0
 */
public class CustomSkipPolicy implements SkipPolicy {

    private static final Logger log = LoggerFactory.getLogger(CustomSkipPolicy.class);
    
    private final int maxSkipCount;

    public CustomSkipPolicy(int maxSkipCount) {
        this.maxSkipCount = maxSkipCount;
    }

    @Override
    public boolean shouldSkip(Throwable t, long skipCount) throws SkipLimitExceededException {
        if (skipCount >= maxSkipCount) {
            log.error("Limite maximo de omisiones alcanzado ({} skips). Abortando Step.", skipCount);
            return false;
        }

        if (t instanceof FlatFileParseException 
                || t instanceof NumberFormatException 
                || t instanceof IllegalArgumentException) {
            log.warn("Tolerancia a fallos [Skip #{} de {}]: {}", skipCount + 1, maxSkipCount, t.getMessage());
            return true;
        }

        return false;
    }
}