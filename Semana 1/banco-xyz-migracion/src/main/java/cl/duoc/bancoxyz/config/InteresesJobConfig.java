package cl.duoc.bancoxyz.config;

import cl.duoc.bancoxyz.dto.InteresDTO;
import cl.duoc.bancoxyz.model.InteresCuenta;
import cl.duoc.bancoxyz.processor.InteresProcessor;
import cl.duoc.bancoxyz.repository.InteresCuentaRepository;
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
 * Configuración del Job 'interesesJob' (Proceso 2: Cálculo de Intereses Mensuales).
 */
@Configuration
public class InteresesJobConfig {

    @Bean
    public FlatFileItemReader<InteresDTO> interesesReader() {
        return new FlatFileItemReaderBuilder<InteresDTO>()
            .name("interesesReader")
            .resource(new ClassPathResource("data/intereses.csv"))
            .delimited()
            .names("cuentaId", "nombre", "saldo", "edad", "tipo")
            .linesToSkip(1)
            .fieldSetMapper(new BeanWrapperFieldSetMapper<>() {{
                setTargetType(InteresDTO.class);
            }})
            .build();
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
    public Step interesesStep(JobRepository jobRepository, PlatformTransactionManager transactionManager,
                             InteresCuentaRepository repository) {
        return new StepBuilder("interesesStep", jobRepository)
            .<InteresDTO, InteresCuenta>chunk(5, transactionManager)
            .reader(interesesReader())
            .processor(interesesProcessor())
            .writer(interesesWriter(repository))
            .build();
    }

    @Bean
    public Job interesesJob(JobRepository jobRepository, Step interesesStep) {
        return new JobBuilder("interesesJob", jobRepository)
            .start(interesesStep)
            .build();
    }
}