package cl.duoc.bancoxyz.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.stereotype.Component;

/**
 * Listener encargado de recolectar y reportar metricas de ejecucion por Step.
 * Registra formalmente lecturas, escrituras, commits, rollbacks y skips.
 * 
 * @author Duoc UC - Desarrollo Backend III
 * @version 3.0
 */
@Component
public class StepMetricsListener implements StepExecutionListener {

    private static final Logger log = LoggerFactory.getLogger(StepMetricsListener.class);

    @Override
    public void beforeStep(StepExecution stepExecution) {
        log.info(">>> INICIANDO STEP: [{}] | Job ID: {}", 
                stepExecution.getStepName(), 
                stepExecution.getJobExecution().getId());
    }

    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        log.info("--------------------------------------------------------------------------------");
        log.info(">>> RESUMEN DE METRICAS DEL STEP: [{}]", stepExecution.getStepName());
        log.info("    * Estado Final     : {}", stepExecution.getExitStatus().getExitCode());
        log.info("    * Registros Leidos : {}", stepExecution.getReadCount());
        log.info("    * Registros Escritos: {}", stepExecution.getWriteCount());
        log.info("    * Registros Omitidos (Skip): Total={}, ReadSkip={}, ProcessSkip={}, WriteSkip={}",
                stepExecution.getSkipCount(),
                stepExecution.getReadSkipCount(),
                stepExecution.getProcessSkipCount(),
                stepExecution.getWriteSkipCount());
        log.info("    * Commits          : {}", stepExecution.getCommitCount());
        log.info("    * Rollbacks        : {}", stepExecution.getRollbackCount());
        log.info("--------------------------------------------------------------------------------");

        return stepExecution.getExitStatus();
    }
}