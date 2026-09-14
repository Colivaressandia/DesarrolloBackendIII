package cl.duoc.bancoxyz.config;

import cl.duoc.bancoxyz.dto.CuentaAnualDTO;
import cl.duoc.bancoxyz.model.CuentaAnual;
import cl.duoc.bancoxyz.processor.CuentaAnualProcessor;
import cl.duoc.bancoxyz.repository.CuentaAnualRepository;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.data.RepositoryItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * Configuración del Job 'cuentasAnualesJob' (Proceso 3: Generación de Estados de Cuenta Anuales).
 */
@Configuration
public class CuentasAnualesJobConfig {

    @Bean
    public FlatFileItemReader<CuentaAnualDTO> cuentasAnualesReader() {
        return new FlatFileItemReaderBuilder<CuentaAnualDTO>()
            .name("cuentasAnualesReader")
            .resource(new ClassPathResource("data/cuentas_anuales.csv"))
            .delimited()
            .names("cuentaId", "fecha", "transaccion", "monto", "descripcion")
            .linesToSkip(1)
            .fieldSetMapper(new BeanWrapperFieldSetMapper<>() {{
                setTargetType(CuentaAnualDTO.class);
            }})
            .build();
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
    public Step cuentasAnualesStep(JobRepository jobRepository, PlatformTransactionManager transactionManager,
                                  CuentaAnualRepository repository) {
        return new StepBuilder("cuentasAnualesStep", jobRepository)
            .<CuentaAnualDTO, CuentaAnual>chunk(5, transactionManager)
            .reader(cuentasAnualesReader())
            .processor(cuentasAnualesProcessor())
            .writer(cuentasAnualesWriter(repository))
            .build();
    }

    @Bean
    public Job cuentasAnualesJob(JobRepository jobRepository, Step cuentasAnualesStep) {
        return new JobBuilder("cuentasAnualesJob", jobRepository)
            .start(cuentasAnualesStep)
            .build();
    }
}