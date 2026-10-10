package com.bancoxyz.batch;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = {
        "spring.cloud.config.enabled=false",
        "eureka.client.enabled=false",
        "spring.datasource.url=jdbc:h2:mem:batchtest;DB_CLOSE_DELAY=-1",
        "batch.input-directory=../bank-api/src/main/resources/data/legacy"
})
class BatchServiceApplicationTests {

    @Autowired
    private JobLauncher jobLauncher;

    @Autowired
    @Qualifier("dailyTransactionReportJob")
    private Job dailyTransactionReportJob;

    @Autowired
    @Qualifier("monthlyInterestCalculationJob")
    private Job monthlyInterestCalculationJob;

    @Autowired
    @Qualifier("annualStatementGenerationJob")
    private Job annualStatementGenerationJob;

    @TempDir
    private Path outputDirectory;

    @Test
    void registersAllThreeBatchJobs() {
        assertThat(dailyTransactionReportJob.getName()).isEqualTo("dailyTransactionReportJob");
        assertThat(monthlyInterestCalculationJob.getName()).isEqualTo("monthlyInterestCalculationJob");
        assertThat(annualStatementGenerationJob.getName()).isEqualTo("annualStatementGenerationJob");
    }

    @Test
    void processesTheThreeLegacyFilesAndWritesTheirReports() throws Exception {
        Path dailyOutput = outputDirectory.resolve("daily.csv");
        Path monthlyOutput = outputDirectory.resolve("monthly.csv");
        Path annualOutput = outputDirectory.resolve("annual.csv");

        JobExecution daily = jobLauncher.run(dailyTransactionReportJob, parameters(
                "date", "2024-06-30", dailyOutput));
        JobExecution monthly = jobLauncher.run(monthlyInterestCalculationJob, parameters(
                "month", "2024-06", monthlyOutput));
        JobExecution annual = jobLauncher.run(annualStatementGenerationJob, parameters(
                "year", "2024", annualOutput));

        assertThat(daily.getStatus().isUnsuccessful()).isFalse();
        assertThat(monthly.getStatus().isUnsuccessful()).isFalse();
        assertThat(annual.getStatus().isUnsuccessful()).isFalse();
        assertThat(Files.readAllLines(dailyOutput)).hasSizeGreaterThan(1);
        assertThat(Files.readAllLines(monthlyOutput)).hasSizeGreaterThan(1);
        assertThat(Files.readAllLines(annualOutput)).hasSizeGreaterThan(1);
    }

    private org.springframework.batch.core.job.parameters.JobParameters parameters(
            String parameterName, String parameterValue, Path output) {
        return new JobParametersBuilder()
                .addString(parameterName, parameterValue)
                .addString("outputFile", output.toAbsolutePath().toString())
                .addString("run.id", UUID.randomUUID().toString())
                .toJobParameters();
    }
}
