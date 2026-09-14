package cl.duoc.bancoxyz.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * Configuracion global de infraestructura para Spring Batch en Banco XYZ.
 * Define el ThreadPool de escalamiento concurrente (3 hilos) y listeners de auditoria.
 * 
 * @author Duoc UC - Desarrollo Backend III
 * @version 2.0
 */
@Configuration
public class BatchConfig {

    private static final Logger log = LoggerFactory.getLogger(BatchConfig.class);

    /**
     * Pool de subprocesos para escalamiento en paralelo.
     * Configurado estrictamente para 3 hilos de ejecucion concurrente segun los requerimientos.
     * 
     * @return {@link TaskExecutor} optimizado para pasos multihilo.
     */
    @Bean(name = "batchTaskExecutor")
    public TaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(3);
        executor.setMaxPoolSize(3);
        executor.setQueueCapacity(15);
        executor.setThreadNamePrefix("banco-thread-");
        executor.initialize();
        return executor;
    }

    /**
     * Listener global para registrar inicio, fin y estado final de los Jobs.
     * 
     * @return {@link JobExecutionListener} con logging estructurado.
     */
    @Bean
    public JobExecutionListener jobExecutionListener() {
        return new JobExecutionListener() {
            @Override
            public void beforeJob(JobExecution jobExecution) {
                log.info("================================================================================");
                log.info(">>> INICIO JOB: {} | Identificador: {}", 
                        jobExecution.getJobInstance().getJobName(), 
                        jobExecution.getId());
                log.info("================================================================================");
            }

            @Override
            public void afterJob(JobExecution jobExecution) {
                log.info("================================================================================");
                log.info(">>> FIN JOB: {} | Estado: {} | Duracion: {} ms", 
                        jobExecution.getJobInstance().getJobName(), 
                        jobExecution.getStatus(),
                        (System.currentTimeMillis() - jobExecution.getStartTime().atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()));
                log.info("================================================================================");
            }
        };
    }
}