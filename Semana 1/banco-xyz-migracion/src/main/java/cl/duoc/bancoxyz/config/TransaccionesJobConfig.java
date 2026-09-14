package cl.duoc.bancoxyz.config;

import cl.duoc.bancoxyz.dto.TransaccionDTO;
import cl.duoc.bancoxyz.model.Transaccion;
import cl.duoc.bancoxyz.processor.TransaccionProcessor;
import cl.duoc.bancoxyz.repository.TransaccionRepository;
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
 * Configuración del Job 'transaccionesJob' (Proceso 1: Reporte de Transacciones Diarias).
 * Implementa el patrón Reader -> Processor -> Writer con procesamiento por bloques (Chunk de 5).
 */
@Configuration
public class TransaccionesJobConfig {

    /**
     * ItemReader: Lee el archivo CSV de transacciones desde resources/data/transacciones.csv.
     */
    @Bean
    public FlatFileItemReader<TransaccionDTO> transaccionesReader() {
        return new FlatFileItemReaderBuilder<TransaccionDTO>()
            .name("transaccionesReader")
            .resource(new ClassPathResource("data/transacciones.csv"))
            .delimited()
            .names("id", "fecha", "monto", "tipo") // Nombres exactos de las columnas en el CSV
            .linesToSkip(1) // Ignora la cabecera (header)
            .fieldSetMapper(new BeanWrapperFieldSetMapper<>() {{
                setTargetType(TransaccionDTO.class);
            }})
            .build();
    }

    /**
     * ItemProcessor: Aplica las validaciones y detección de anomalías.
     */
    @Bean
    public TransaccionProcessor transaccionesProcessor() {
        return new TransaccionProcessor();
    }

    /**
     * ItemWriter: Guarda las entidades procesadas en MySQL a través del repositorio JPA.
     */
    @Bean
    public RepositoryItemWriter<Transaccion> transaccionesWriter(TransaccionRepository repository) {
        RepositoryItemWriter<Transaccion> writer = new RepositoryItemWriter<>();
        writer.setRepository(repository);
        writer.setMethodName("save");
        return writer;
    }

    /**
     * Step: Ensambla Reader, Processor y Writer con un tamaño de chunk de 5 elementos bajo una transacción.
     */
    @Bean
    public Step transaccionesStep(JobRepository jobRepository, PlatformTransactionManager transactionManager,
                                 TransaccionRepository repository) {
        return new StepBuilder("transaccionesStep", jobRepository)
            .<TransaccionDTO, Transaccion>chunk(5, transactionManager)
            .reader(transaccionesReader())
            .processor(transaccionesProcessor())
            .writer(transaccionesWriter(repository))
            .build();
    }

    /**
     * Job: Contenedor principal que ejecuta el step de transacciones.
     */
    @Bean
    public Job transaccionesJob(JobRepository jobRepository, Step transaccionesStep) {
        return new JobBuilder("transaccionesJob", jobRepository)
            .start(transaccionesStep)
            .build();
    }
}