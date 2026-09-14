package cl.duoc.bancoxyz.policy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.retry.RetryContext;
import org.springframework.retry.RetryPolicy;
import org.springframework.retry.context.RetryContextSupport;
import org.springframework.dao.TransientDataAccessException;

import java.net.SocketTimeoutException;
import java.sql.SQLException;

/**
 * Politica personalizada de reintentos (RetryPolicy) parametrizable.
 * Maneja reintentos ante fallas transitorias de red o bloqueos de base de datos.
 * 
 * @author Duoc UC - Desarrollo Backend III
 * @version 3.0
 */
public class CustomRetryPolicy implements RetryPolicy {

    private static final Logger log = LoggerFactory.getLogger(CustomRetryPolicy.class);

    private final int maxAttempts;

    public CustomRetryPolicy(int maxAttempts) {
        this.maxAttempts = maxAttempts;
    }

    @Override
    public boolean canRetry(RetryContext context) {
        Throwable lastThrowable = context.getLastThrowable();
        if (lastThrowable == null) {
            return true;
        }

        int currentAttempt = context.getRetryCount();

        if (currentAttempt < maxAttempts && isRetryableException(lastThrowable)) {
            log.warn("Reintento de operacion [Intento #{} de {}] debido a error transitorio: {}", 
                    currentAttempt + 1, maxAttempts, lastThrowable.getMessage());
            return true;
        }

        return false;
    }

    private boolean isRetryableException(Throwable t) {
        return t instanceof TransientDataAccessException 
                || t instanceof SocketTimeoutException 
                || t instanceof SQLException;
    }

    @Override
    public RetryContext open(RetryContext parent) {
        return new RetryContextSupport(parent);
    }

    @Override
    public void close(RetryContext context) {
    }

    @Override
    public void registerThrowable(RetryContext context, Throwable throwable) {
        ((RetryContextSupport) context).registerThrowable(throwable);
    }
}