package cl.duoc.bancoxyz.config;

import cl.duoc.bancoxyz.dto.TransaccionDTO;
import cl.duoc.bancoxyz.listener.StepMetricsListener;
import cl.duoc.bancoxyz.model.Transaccion;
import cl.duoc.bancoxyz.policy.CustomRetryPolicy;
import cl.duoc.bancoxyz.policy.CustomSkipPolicy;
import cl.duoc.bancoxyz.processor.TransaccionProcessor;
import cl.duoc.bancoxyz.repository.TransaccionRepository;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.data.RepositoryItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.support.SynchronizedItemStreamReader;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.task.TaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * Configuracion del Job para Reporte de Transacciones Diarias (Proceso 1).
 * Implementa lectura concurrente, deteccion de anomalias y persistencia transaccional.
 * 
 * @author Duoc UC - Desarrollo Backend III
 * @version 3.0
 */
@Configuration
public class TransaccionesJobConfig {

    @Value("${batch.tuning.chunk-size:5}")
    private int chunkSize;

    @Value("${batch.tuning.max-skip-count:5}")
    private int maxSkipCount;

    @Value("${batch.tuning.max-retry-attempts:3}")
    private int maxRetryAttempts;

    @Bean
    public FlatFileItemReader<TransaccionDTO> rawTransaccionesReader() {
        return new FlatFileItemReaderBuilder<TransaccionDTO>()
                .name("rawTransaccionesReader")
                .resource(new ClassPathResource("data/transacciones.csv"))
                .delimited()
                .names("id", "fecha", "monto", "tipo")
                .linesToSkip(1)
                .saveState(false)
                .fieldSetMapper(new BeanWrapperFieldSetMapper<>() {{
                    setTargetType(TransaccionDTO.class);
                }})
                .build();
    }

    @Bean
    public SynchronizedItemStreamReader<TransaccionDTO> transaccionesReader() {
        SynchronizedItemStreamReader<TransaccionDTO> reader = new SynchronizedItemStreamReader<>();
        reader.setDelegate(rawTransaccionesReader());
        return reader;
    }

    @Bean
    public TransaccionProcessor transaccionesProcessor() {
        return new TransaccionProcessor();
    }

    @Bean
    public RepositoryItemWriter<Transaccion> transaccionesWriter(TransaccionRepository repository) {
        RepositoryItemWriter<Transaccion> writer = new RepositoryItemWriter<>();
        writer.setRepository(repository);
        writer.setMethodName("save");
        return writer;
    }

    @Bean
    public Step transaccionesStep(JobRepository jobRepository,
                                  PlatformTransactionManager transactionManager,
                                  TransaccionRepository repository,
                                  StepMetricsListener stepMetricsListener,
                                  @Qualifier("batchTaskExecutor") TaskExecutor taskExecutor) {
        return new StepBuilder("transaccionesStep", jobRepository)
                .<TransaccionDTO, Transaccion>chunk(chunkSize, transactionManager)
                .reader(transaccionesReader())
                .processor(transaccionesProcessor())
                .writer(transaccionesWriter(repository))
                .faultTolerant()
                .skipPolicy(new CustomSkipPolicy(maxSkipCount))
                .retryPolicy(new CustomRetryPolicy(maxRetryAttempts))
                .taskExecutor(taskExecutor)
                .listener(stepMetricsListener)
                .build();
    }

    @Bean
    public Job transaccionesJob(JobRepository jobRepository,
                                Step transaccionesStep,
                                JobExecutionListener jobExecutionListener) {
        return new JobBuilder("transaccionesJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .listener(jobExecutionListener)
                .start(transaccionesStep)
                .build();
    }
}