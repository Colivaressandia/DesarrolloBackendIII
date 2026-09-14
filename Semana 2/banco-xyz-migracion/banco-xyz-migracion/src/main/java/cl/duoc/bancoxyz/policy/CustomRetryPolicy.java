package cl.duoc.bancoxyz.policy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.step.item.KeyGenerator;
import org.springframework.retry.RetryContext;
import org.springframework.retry.RetryPolicy;
import org.springframework.retry.context.RetryContextSupport;
import org.springframework.dao.TransientDataAccessException;

import java.sql.SQLException;

/**
 * Politica personalizada de reintentos (RetryPolicy) para Banco XYZ.
 * Controla el reintento automatico ante bloqueos transitorios o interrupciones de base de datos.
 * 
 * @author Duoc UC - Desarrollo Backend III
 * @version 2.0
 */
public class CustomRetryPolicy implements RetryPolicy {

    private static final Logger log = LoggerFactory.getLogger(CustomRetryPolicy.class);
    private static final int MAX_ATTEMPTS = 3;

    @Override
    public boolean canRetry(RetryContext context) {
        Throwable lastThrowable = context.getLastThrowable();
        if (lastThrowable == null) {
            return true;
        }
        
        if (context.getRetryCount() < MAX_ATTEMPTS) {
            if (lastThrowable instanceof TransientDataAccessException || lastThrowable instanceof SQLException) {
                log.warn("Reintentando operacion batch tras fallo transitorio. Intento #{}/{}", 
                        context.getRetryCount() + 1, MAX_ATTEMPTS);
                return true;
            }
        }
        return false;
    }

    @Override
    public RetryContext open(RetryContext parent) {
        return new RetryContextSupport(parent);
    }

    @Override
    public void close(RetryContext context) {
        // Cierre de contexto de reintento
    }

    @Override
    public void registerThrowable(RetryContext context, Throwable throwable) {
        ((RetryContextSupport) context).registerThrowable(throwable);
    }
}