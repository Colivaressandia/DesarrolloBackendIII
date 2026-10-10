package com.bancoxyz.batch.controller;

import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.job.JobExecutionException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/batch")
public class BatchJobController {

    private final JobLauncher jobLauncher;
    private final Job dailyTransactionReportJob;
    private final Job monthlyInterestCalculationJob;
    private final Job annualStatementGenerationJob;
    private final Path outputDirectory;

    public BatchJobController(
            JobLauncher jobLauncher,
            @Qualifier("dailyTransactionReportJob") Job dailyTransactionReportJob,
            @Qualifier("monthlyInterestCalculationJob") Job monthlyInterestCalculationJob,
            @Qualifier("annualStatementGenerationJob") Job annualStatementGenerationJob,
            @Value("${batch.output-directory}") String outputDirectory) {
        this.jobLauncher = jobLauncher;
        this.dailyTransactionReportJob = dailyTransactionReportJob;
        this.monthlyInterestCalculationJob = monthlyInterestCalculationJob;
        this.annualStatementGenerationJob = annualStatementGenerationJob;
        this.outputDirectory = Path.of(outputDirectory).toAbsolutePath().normalize();
    }

    @PostMapping("/daily-transactions")
    public Map<String, Object> reportDailyTransactions(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) String runId) throws JobExecutionException {
        return launch(dailyTransactionReportJob, "date", date.toString(), "daily-transactions", runId);
    }

    @PostMapping("/monthly-interest")
    public Map<String, Object> calculateMonthlyInterest(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM") YearMonth month,
            @RequestParam(required = false) String runId) throws JobExecutionException {
        return launch(monthlyInterestCalculationJob, "month", month.toString(), "monthly-interest", runId);
    }

    @PostMapping("/annual-statements")
    public Map<String, Object> generateAnnualStatements(@RequestParam int year,
                                                        @RequestParam(required = false) String runId)
            throws JobExecutionException {
        if (year < 1900 || year > 9999) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El año debe estar entre 1900 y 9999");
        }
        return launch(annualStatementGenerationJob, "year", Integer.toString(year),
                "annual-statements", runId);
    }

    private Map<String, Object> launch(Job job, String parameterName, String parameterValue,
                                       String outputPrefix, String requestedRunId)
            throws JobExecutionException {
        try {
            Files.createDirectories(outputDirectory);
        } catch (IOException ex) {
            throw new IllegalStateException("No se pudo preparar el directorio de salida batch", ex);
        }

        UUID runId = parseRunId(requestedRunId);
        Path outputFile = outputDirectory.resolve(outputPrefix + "-" + runId + ".csv");
        var parameters = new JobParametersBuilder()
                .addString(parameterName, parameterValue)
                .addString("outputFile", outputFile.toString())
                .addString("run.id", runId.toString())
                .toJobParameters();
        JobExecution execution = jobLauncher.run(job, parameters);
        return Map.of(
                "jobName", job.getName(),
                "executionId", execution.getId(),
                "status", execution.getStatus().toString(),
                "outputFile", outputFile.toString());
    }

    private UUID parseRunId(String requestedRunId) {
        if (requestedRunId == null || requestedRunId.isBlank()) {
            return UUID.randomUUID();
        }
        try {
            return UUID.fromString(requestedRunId);
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "runId debe tener formato UUID para permitir reinicios seguros", ex);
        }
    }
}
