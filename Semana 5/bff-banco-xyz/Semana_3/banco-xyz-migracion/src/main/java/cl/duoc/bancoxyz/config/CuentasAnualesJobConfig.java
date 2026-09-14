package cl.duoc.bancoxyz.config;

import cl.duoc.bancoxyz.dto.CuentaAnualDTO;
import cl.duoc.bancoxyz.listener.StepMetricsListener;
import cl.duoc.bancoxyz.model.CuentaAnual;
import cl.duoc.bancoxyz.policy.CustomRetryPolicy;
import cl.duoc.bancoxyz.policy.CustomSkipPolicy;
import cl.duoc.bancoxyz.processor.CuentaAnualProcessor;
import cl.duoc.bancoxyz.repository.CuentaAnualRepository;
import cl.duoc.bancoxyz.tasklet.CuentasAnualesAggregationTasklet;
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
 * Configuracion del Job para Estados de Cuenta Anuales (Proceso 3).
 * Incluye procesamiento paralelo en Step 1 y agregacion contable en Step 2.
 * 
 * @author Duoc UC - Desarrollo Backend III
 * @version 3.0
 */
@Configuration
public class CuentasAnualesJobConfig {

    @Value("${batch.tuning.chunk-size:5}")
    private int chunkSize;

    @Value("${batch.tuning.max-skip-count:5}")
    private int maxSkipCount;

    @Value("${batch.tuning.max-retry-attempts:3}")
    private int maxRetryAttempts;

    @Bean
    public FlatFileItemReader<CuentaAnualDTO> rawCuentasAnualesReader() {
        return new FlatFileItemReaderBuilder<CuentaAnualDTO>()
                .name("rawCuentasAnualesReader")
                .resource(new ClassPathResource("data/cuentas_anuales.csv"))
                .delimited()
                .names("cuentaId", "fecha", "tipoTransaccion", "monto", "descripcion")
                .linesToSkip(1)
                .saveState(false)
                .fieldSetMapper(new BeanWrapperFieldSetMapper<>() {{
                    setTargetType(CuentaAnualDTO.class);
                }})
                .build();
    }

    @Bean
    public SynchronizedItemStreamReader<CuentaAnualDTO> cuentasAnualesReader() {
        SynchronizedItemStreamReader<CuentaAnualDTO> reader = new SynchronizedItemStreamReader<>();
        reader.setDelegate(rawCuentasAnualesReader());
        return reader;
    }

    @Bean
    public CuentaAnualProcessor cuentasAnualesProcessor() {
        return new CuentaAnualProcessor();
    }

    @Bean
    public RepositoryItemWriter<CuentaAnual> cuentasAnualesWriter(CuentaAnualRepository repository) {
        RepositoryItemWriter<CuentaAnual> writer = new RepositoryItemWriter<>();
        writer.setRepository(repository);
        writer.setMethodName("save");
        return writer;
    }

    @Bean
    public Step cuentasAnualesStep(JobRepository jobRepository,
                                  PlatformTransactionManager transactionManager,
                                  CuentaAnualRepository repository,
                                  StepMetricsListener stepMetricsListener,
                                  @Qualifier("batchTaskExecutor") TaskExecutor taskExecutor) {
        return new StepBuilder("cuentasAnualesStep", jobRepository)
                .<CuentaAnualDTO, CuentaAnual>chunk(chunkSize, transactionManager)
                .reader(cuentasAnualesReader())
                .processor(cuentasAnualesProcessor())
                .writer(cuentasAnualesWriter(repository))
                .faultTolerant()
                .skipPolicy(new CustomSkipPolicy(maxSkipCount))
                .retryPolicy(new CustomRetryPolicy(maxRetryAttempts))
                .taskExecutor(taskExecutor)
                .listener(stepMetricsListener)
                .build();
    }

    @Bean
    public Step cuentasAnualesResumenStep(JobRepository jobRepository,
                                         PlatformTransactionManager transactionManager,
                                         CuentasAnualesAggregationTasklet aggregationTasklet,
                                         StepMetricsListener stepMetricsListener) {
        return new StepBuilder("cuentasAnualesResumenStep", jobRepository)
                .tasklet(aggregationTasklet, transactionManager)
                .listener(stepMetricsListener)
                .build();
    }

    @Bean
    public Job cuentasAnualesJob(JobRepository jobRepository, 
                                 Step cuentasAnualesStep,
                                 Step cuentasAnualesResumenStep,
                                 JobExecutionListener jobExecutionListener) {
        return new JobBuilder("cuentasAnualesJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .listener(jobExecutionListener)
                .start(cuentasAnualesStep)
                .next(cuentasAnualesResumenStep)
                .build();
    }
}