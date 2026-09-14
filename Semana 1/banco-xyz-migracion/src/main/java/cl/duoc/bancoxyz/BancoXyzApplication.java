package cl.duoc.bancoxyz;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

/**
 * Clase principal de inicio para la aplicación Spring Boot de Banco XYZ.
 * Al implementar un CommandLineRunner, ejecuta los 3 Jobs de forma secuencial
 * mostrando el progreso y logs en consola al levantar el proyecto.
 */
@SpringBootApplication
public class BancoXyzApplication {

    public static void main(String[] args) {
        SpringApplication.run(BancoXyzApplication.class, args);
    }

    /**
     * Runner encargado de disparar la ejecución de los 3 procesos batch en orden.
     */
    @Bean
    public CommandLineRunner runJobs(
            JobLauncher jobLauncher,
            @Qualifier("transaccionesJob") Job transaccionesJob,
            @Qualifier("interesesJob") Job interesesJob,
            @Qualifier("cuentasAnualesJob") Job cuentasAnualesJob) {
        return args -> {
            // Parámetro dinámico con timestamp para asegurar unicidad en cada corrida de Spring Batch
            JobParameters params = new JobParametersBuilder()
                    .addLong("timestamp", System.currentTimeMillis())
                    .toJobParameters();

            System.out.println("=================================================");
            System.out.println(">>> INICIANDO PROCESO 1: TRANSACCIONES DIARIAS <<<");
            System.out.println("=================================================");
            jobLauncher.run(transaccionesJob, params);

            System.out.println("=================================================");
            System.out.println(">>> INICIANDO PROCESO 2: CÁLCULO DE INTERESES  <<<");
            System.out.println("=================================================");
            jobLauncher.run(interesesJob, params);

            System.out.println("=================================================");
            System.out.println(">>> INICIANDO PROCESO 3: ESTADOS DE CUENTA     <<<");
            System.out.println("=================================================");
            jobLauncher.run(cuentasAnualesJob, params);

            System.out.println("=================================================");
            System.out.println(">>>   MIGRACIÓN BATCH COMPLETADA CON ÉXITO    <<<");
            System.out.println("=================================================");
        };
    }
}