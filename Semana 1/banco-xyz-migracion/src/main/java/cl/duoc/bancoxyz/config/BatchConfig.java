package cl.duoc.bancoxyz.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Clase de configuración transversal para el ecosistema de Spring Batch.
 * Define componentes comunes compartidos entre todos los Jobs, como el
 * listener de auditoría y monitoreo del ciclo de vida de los procesos.
 */
@Configuration
public class BatchConfig {

    private static final Logger log = LoggerFactory.getLogger(BatchConfig.class);

    /**
     * Listener global que monitorea el inicio y fin de cada Job ejecutado.
     * Imprime en consola el nombre del proceso, parámetros y estado final (COMPLETED/FAILED).
     *
     * @return instancia de JobExecutionListener
     */
    @Bean
    public JobExecutionListener jobExecutionListener() {
        return new JobExecutionListener() {
            @Override
            public void beforeJob(JobExecution jobExecution) {
                log.info("===============================================================");
                log.info(">>> INICIO DEL JOB: {} | Parámetros: {}", 
                        jobExecution.getJobInstance().getJobName(), 
                        jobExecution.getJobParameters());
                log.info("===============================================================");
            }

            @Override
            public void afterJob(JobExecution jobExecution) {
                log.info("===============================================================");
                log.info(">>> FIN DEL JOB: {} | Estado: {} | Hora fin: {}", 
                        jobExecution.getJobInstance().getJobName(), 
                        jobExecution.getStatus(),
                        jobExecution.getEndTime());
                log.info("===============================================================");
            }
        };
    }
}