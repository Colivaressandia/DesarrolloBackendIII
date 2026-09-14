package cl.duoc.bancoxyz.config;

import cl.duoc.bancoxyz.dto.InteresDTO;
import cl.duoc.bancoxyz.listener.StepMetricsListener;
import cl.duoc.bancoxyz.model.InteresCuenta;
import cl.duoc.bancoxyz.policy.CustomRetryPolicy;
import cl.duoc.bancoxyz.policy.CustomSkipPolicy;
import cl.duoc.bancoxyz.processor.InteresProcessor;
import cl.duoc.bancoxyz.repository.InteresCuentaRepository;
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
 * Configuracion del Job para Calculo de Intereses Mensuales (Proceso 2).
 * Aplica reglas de tasa dinamica segun producto y actualizacion de saldo.
 * 
 * @author Duoc UC - Desarrollo Backend III
 * @version 3.0
 */
@Configuration
public class InteresesJobConfig {

    @Value("${batch.tuning.chunk-size:5}")
    private int chunkSize;

    @Value("${batch.tuning.max-skip-count:5}")
    private int maxSkipCount;

    @Value("${batch.tuning.max-retry-attempts:3}")
    private int maxRetryAttempts;

    @Bean
    public FlatFileItemReader<InteresDTO> rawInteresesReader() {
        return new FlatFileItemReaderBuilder<InteresDTO>()
                .name("rawInteresesReader")
                .resource(new ClassPathResource("data/intereses.csv"))
                .delimited()
                .names("cuentaId", "nombre", "saldo", "edad", "tipo")
                .linesToSkip(1)
                .saveState(false)
                .fieldSetMapper(new BeanWrapperFieldSetMapper<>() {{
                    setTargetType(InteresDTO.class);
                }})
                .build();
    }

    @Bean
    public SynchronizedItemStreamReader<InteresDTO> interesesReader() {
        SynchronizedItemStreamReader<InteresDTO> reader = new SynchronizedItemStreamReader<>();
        reader.setDelegate(rawInteresesReader());
        return reader;
    }

    @Bean
    public InteresProcessor interesesProcessor() {
        return new InteresProcessor();
    }

    @Bean
    public RepositoryItemWriter<InteresCuenta> interesesWriter(InteresCuentaRepository repository) {
        RepositoryItemWriter<InteresCuenta> writer = new RepositoryItemWriter<>();
        writer.setRepository(repository);
        writer.setMethodName("save");
        return writer;
    }

    @Bean
    public Step interesesStep(JobRepository jobRepository,
                              PlatformTransactionManager transactionManager,
                              InteresCuentaRepository repository,
                              StepMetricsListener stepMetricsListener,
                              @Qualifier("batchTaskExecutor") TaskExecutor taskExecutor) {
        return new StepBuilder("interesesStep", jobRepository)
                .<InteresDTO, InteresCuenta>chunk(chunkSize, transactionManager)
                .reader(interesesReader())
                .processor(interesesProcessor())
                .writer(interesesWriter(repository))
                .faultTolerant()
                .skipPolicy(new CustomSkipPolicy(maxSkipCount))
                .retryPolicy(new CustomRetryPolicy(maxRetryAttempts))
                .taskExecutor(taskExecutor)
                .listener(stepMetricsListener)
                .build();
    }

    @Bean
    public Job interesesJob(JobRepository jobRepository,
                            Step interesesStep,
                            JobExecutionListener jobExecutionListener) {
        return new JobBuilder("interesesJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .listener(jobExecutionListener)
                .start(interesesStep)
                .build();
    }
}