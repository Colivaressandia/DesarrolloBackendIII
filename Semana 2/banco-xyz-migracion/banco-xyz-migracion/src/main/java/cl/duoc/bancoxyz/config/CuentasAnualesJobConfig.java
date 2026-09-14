package cl.duoc.bancoxyz.config;

import cl.duoc.bancoxyz.dto.CuentaAnualDTO;
import cl.duoc.bancoxyz.model.CuentaAnual;
import cl.duoc.bancoxyz.policy.CustomRetryPolicy;
import cl.duoc.bancoxyz.policy.CustomSkipPolicy;
import cl.duoc.bancoxyz.processor.CuentaAnualProcessor;
import cl.duoc.bancoxyz.repository.CuentaAnualRepository;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.data.RepositoryItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.support.SynchronizedItemStreamReader;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.task.TaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * Configuracion del Job para la Generacion de Estados de Cuenta Anuales (Proceso 3).
 * Clasifica los movimientos contables para auditoria con escalamiento y control de excepciones.
 * 
 * @author Duoc UC - Desarrollo Backend III
 * @version 2.0
 */
@Configuration
public class CuentasAnualesJobConfig {

    @Bean
    public FlatFileItemReader<CuentaAnualDTO> rawCuentasAnualesReader() {
        return new FlatFileItemReaderBuilder<CuentaAnualDTO>()
                .name("rawCuentasAnualesReader")
                .resource(new ClassPathResource("data/cuentas_anuales.csv"))
                .delimited()
                .names("cuentaId", "fecha", "tipoTransaccion", "monto", "descripcion")
                .linesToSkip(1)
                .fieldSetMapper(new BeanWrapperFieldSetMapper<>() {{
                    setTargetType(CuentaAnualDTO.class);
                }})
                .build();
    }

    /**
     * Reader sincronizado (Thread-Safe) para distribucion segura de lineas CSV a 3 hilos.
     */
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
                                  @Qualifier("batchTaskExecutor") TaskExecutor taskExecutor) {
        return new StepBuilder("cuentasAnualesStep", jobRepository)
                .<CuentaAnualDTO, CuentaAnual>chunk(5, transactionManager)
                .reader(cuentasAnualesReader())
                .processor(cuentasAnualesProcessor())
                .writer(cuentasAnualesWriter(repository))
                .faultTolerant()
                .skipPolicy(new CustomSkipPolicy())
                .retryPolicy(new CustomRetryPolicy())
                .taskExecutor(taskExecutor)
                .build();
    }

    @Bean
    public Job cuentasAnualesJob(JobRepository jobRepository, 
                                 Step cuentasAnualesStep,
                                 JobExecutionListener jobExecutionListener) {
        return new JobBuilder("cuentasAnualesJob", jobRepository)
                .listener(jobExecutionListener)
                .start(cuentasAnualesStep)
                .build();
    }
}