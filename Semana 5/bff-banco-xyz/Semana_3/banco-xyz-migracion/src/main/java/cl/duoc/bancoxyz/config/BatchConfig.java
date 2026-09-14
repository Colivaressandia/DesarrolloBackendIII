package cl.duoc.bancoxyz.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * Configuracion global de infraestructura para Spring Batch.
 * Define el ThreadPoolTaskExecutor parametrizado y los listeners globales.
 * 
 * @author Duoc UC - Desarrollo Backend III
 * @version 3.0
 */
@Configuration
public class BatchConfig {

    private static final Logger log = LoggerFactory.getLogger(BatchConfig.class);

    @Value("${batch.tuning.core-pool-size:3}")
    private int corePoolSize;

    @Value("${batch.tuning.max-pool-size:5}")
    private int maxPoolSize;

    @Value("${batch.tuning.queue-capacity:20}")
    private int queueCapacity;

    @Value("${batch.tuning.thread-prefix:banco-thread-}")
    private String threadPrefix;

    /**
     * Pool de hilos parametrizado para ejecucion paralela multi-threading.
     */
    @Bean(name = "batchTaskExecutor")
    public TaskExecutor batchTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(corePoolSize);
        executor.setMaxPoolSize(maxPoolSize);
        executor.setQueueCapacity(queueCapacity);
        executor.setThreadNamePrefix(threadPrefix);
        executor.initialize();
        return executor;
    }

    @Bean
    public JobExecutionListener jobExecutionListener() {
        return new JobExecutionListener() {
            @Override
            public void beforeJob(JobExecution jobExecution) {
                log.info("================================================================================");
                log.info(">>> INICIO JOB: {} | Instance ID: {}", 
                        jobExecution.getJobInstance().getJobName(), 
                        jobExecution.getJobId());
                log.info("================================================================================");
            }

            @Override
            public void afterJob(JobExecution jobExecution) {
                long duration = 0;
                if (jobExecution.getStartTime() != null && jobExecution.getEndTime() != null) {
                    duration = java.time.Duration.between(
                            jobExecution.getStartTime(), 
                            jobExecution.getEndTime()
                    ).toMillis();
                }
                log.info("================================================================================");
                log.info(">>> FIN JOB: {} | Estado: {} | Duracion: {} ms",
                        jobExecution.getJobInstance().getJobName(), 
                        jobExecution.getStatus(), 
                        duration);
                log.info("================================================================================");
            }
        };
    }
}