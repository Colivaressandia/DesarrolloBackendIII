package com.bancoxyz.batch.config;

import com.bancoxyz.batch.model.LegacyBatchRecords.AnnualAccountInput;
import com.bancoxyz.batch.model.LegacyBatchRecords.AnnualStatementRow;
import com.bancoxyz.batch.model.LegacyBatchRecords.DailyTransactionRow;
import com.bancoxyz.batch.model.LegacyBatchRecords.InterestInput;
import com.bancoxyz.batch.model.LegacyBatchRecords.MonthlyInterestRow;
import com.bancoxyz.batch.model.LegacyBatchRecords.TransactionInput;
import com.bancoxyz.batch.support.Csv;
import com.bancoxyz.batch.support.LegacyDates;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.listener.SkipListener;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.infrastructure.item.ItemProcessor;
import org.springframework.batch.infrastructure.item.file.FlatFileItemReader;
import org.springframework.batch.infrastructure.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.infrastructure.item.file.builder.FlatFileItemWriterBuilder;
import org.springframework.batch.infrastructure.item.file.transform.LineAggregator;
import org.springframework.dao.TransientDataAccessException;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.transaction.PlatformTransactionManager;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.YearMonth;

@Configuration
public class BatchJobsConfiguration {

    private static final Logger log = LoggerFactory.getLogger(BatchJobsConfiguration.class);
    private static final int CHUNK_SIZE = 100;

    @Bean
    SkipListener<Object, Object> batchSkipListener() {
        return new SkipListener<>() {
            @Override
            public void onSkipInRead(Throwable error) {
                log.warn("Se omitió una fila ilegible del archivo legacy ({})",
                        error.getClass().getSimpleName());
            }

            @Override
            public void onSkipInProcess(Object item, Throwable error) {
                log.warn("Se omitió un registro legacy inválido de tipo {} ({})",
                        item.getClass().getSimpleName(), error.getClass().getSimpleName());
            }

            @Override
            public void onSkipInWrite(Object item, Throwable error) {
                log.error("No se pudo escribir un registro batch de tipo {} ({})",
                        item.getClass().getSimpleName(), error.getClass().getSimpleName());
            }
        };
    }

    @Bean
    @StepScope
    FlatFileItemReader<TransactionInput> transactionReader(
            @Value("${batch.input-directory}") String inputDirectory) {
        return new FlatFileItemReaderBuilder<TransactionInput>()
                .name("legacyTransactionReader")
                .resource(input(inputDirectory, "transacciones.csv"))
                .linesToSkip(1)
                .delimited()
                .names("id", "fecha", "monto", "tipo")
                .fieldSetMapper(fields -> new TransactionInput(
                        fields.readString("id"), fields.readString("fecha"),
                        fields.readString("monto"), fields.readString("tipo")))
                .strict(true)
                .build();
    }

    @Bean
    @StepScope
    FlatFileItemReader<InterestInput> interestReader(
            @Value("${batch.input-directory}") String inputDirectory) {
        return new FlatFileItemReaderBuilder<InterestInput>()
                .name("legacyInterestReader")
                .resource(input(inputDirectory, "intereses.csv"))
                .linesToSkip(1)
                .delimited()
                .names("accountId", "name", "balance", "age", "type")
                .fieldSetMapper(fields -> new InterestInput(
                        fields.readString("accountId"), fields.readString("name"),
                        fields.readString("balance"), fields.readString("age"),
                        fields.readString("type")))
                .strict(true)
                .build();
    }

    @Bean
    @StepScope
    FlatFileItemReader<AnnualAccountInput> annualAccountReader(
            @Value("${batch.input-directory}") String inputDirectory) {
        return new FlatFileItemReaderBuilder<AnnualAccountInput>()
                .name("legacyAnnualAccountReader")
                .resource(input(inputDirectory, "cuentas_anuales.csv"))
                .linesToSkip(1)
                .delimited()
                .names("accountId", "date", "transaction", "amount", "description")
                .fieldSetMapper(fields -> new AnnualAccountInput(
                        fields.readString("accountId"), fields.readString("date"),
                        fields.readString("transaction"), fields.readString("amount"),
                        fields.readString("description")))
                .strict(true)
                .build();
    }

    @Bean
    @StepScope
    ItemProcessor<TransactionInput, DailyTransactionRow> dailyTransactionProcessor(
            @Value("#{jobParameters['date']}") String date) {
        LocalDate targetDate = LocalDate.parse(date);
        return item -> dailyRow(item, targetDate);
    }

    @Bean
    @StepScope
    ItemProcessor<InterestInput, MonthlyInterestRow> monthlyInterestProcessor(
            @Value("#{jobParameters['month']}") String month,
            @Value("${batch.rates.savings-monthly:0.005}") BigDecimal savingsRate,
            @Value("${batch.rates.loan-monthly:0.020}") BigDecimal loanRate) {
        YearMonth targetMonth = YearMonth.parse(month);
        return item -> monthlyInterest(item, targetMonth, savingsRate, loanRate);
    }

    @Bean
    @StepScope
    ItemProcessor<AnnualAccountInput, AnnualStatementRow> annualStatementProcessor(
            @Value("#{jobParameters['year']}") String year) {
        int targetYear = Integer.parseInt(year);
        return item -> annualStatementRow(item, targetYear);
    }

    @Bean
    @StepScope
    org.springframework.batch.infrastructure.item.file.FlatFileItemWriter<DailyTransactionRow> dailyTransactionWriter(
            @Value("#{jobParameters['outputFile']}") String outputFile) {
        return writer(outputFile, "id,fecha,monto,tipo,anomalia",
                row -> Csv.line(row.id(), row.date(), row.amount(), row.type(), row.anomaly()));
    }

    @Bean
    @StepScope
    org.springframework.batch.infrastructure.item.file.FlatFileItemWriter<MonthlyInterestRow> monthlyInterestWriter(
            @Value("#{jobParameters['outputFile']}") String outputFile) {
        return writer(outputFile, "cuenta_id,periodo,tipo,saldo,tasa_mensual,interes,total",
                row -> Csv.line(row.accountId(), row.period(), row.type(), row.balance(),
                        row.rate(), row.interest(), row.total()));
    }

    @Bean
    @StepScope
    org.springframework.batch.infrastructure.item.file.FlatFileItemWriter<AnnualStatementRow> annualStatementWriter(
            @Value("#{jobParameters['outputFile']}") String outputFile) {
        return writer(outputFile,
                "cuenta_id,anio,fecha,transaccion,monto,descripcion",
                row -> Csv.line(row.accountId(), row.year(), row.date(), row.transaction(),
                        row.amount(), row.description()));
    }

    @Bean
    Step dailyTransactionReportStep(JobRepository repository,
                                    PlatformTransactionManager transactionManager,
                                    @Qualifier("transactionReader") FlatFileItemReader<TransactionInput> reader,
                                    @Qualifier("dailyTransactionProcessor")
                                    ItemProcessor<TransactionInput, DailyTransactionRow> processor,
                                    @Qualifier("dailyTransactionWriter")
                                    org.springframework.batch.infrastructure.item.file.FlatFileItemWriter<DailyTransactionRow> writer,
                                    SkipListener<Object, Object> batchSkipListener) {
        return new StepBuilder("dailyTransactionReportStep", repository)
                .<TransactionInput, DailyTransactionRow>chunk(CHUNK_SIZE, transactionManager)
                .reader(reader)
                .processor(processor)
                .writer(writer)
                .faultTolerant()
                .retry(IOException.class)
                .retry(TransientDataAccessException.class)
                .retryLimit(3)
                .skip(IllegalArgumentException.class)
                .skipLimit(10000)
                .listener(batchSkipListener)
                .build();
    }

    @Bean
    Step monthlyInterestCalculationStep(JobRepository repository,
                                        PlatformTransactionManager transactionManager,
                                        @Qualifier("interestReader") FlatFileItemReader<InterestInput> reader,
                                        @Qualifier("monthlyInterestProcessor")
                                        ItemProcessor<InterestInput, MonthlyInterestRow> processor,
                                        @Qualifier("monthlyInterestWriter")
                                        org.springframework.batch.infrastructure.item.file.FlatFileItemWriter<MonthlyInterestRow> writer,
                                        SkipListener<Object, Object> batchSkipListener) {
        return new StepBuilder("monthlyInterestCalculationStep", repository)
                .<InterestInput, MonthlyInterestRow>chunk(CHUNK_SIZE, transactionManager)
                .reader(reader)
                .processor(processor)
                .writer(writer)
                .faultTolerant()
                .retry(IOException.class)
                .retry(TransientDataAccessException.class)
                .retryLimit(3)
                .skip(IllegalArgumentException.class)
                .skipLimit(10000)
                .listener(batchSkipListener)
                .build();
    }

    @Bean
    Step annualStatementGenerationStep(JobRepository repository,
                                       PlatformTransactionManager transactionManager,
                                       @Qualifier("annualAccountReader") FlatFileItemReader<AnnualAccountInput> reader,
                                       @Qualifier("annualStatementProcessor")
                                       ItemProcessor<AnnualAccountInput, AnnualStatementRow> processor,
                                       @Qualifier("annualStatementWriter")
                                       org.springframework.batch.infrastructure.item.file.FlatFileItemWriter<AnnualStatementRow> writer,
                                       SkipListener<Object, Object> batchSkipListener) {
        return new StepBuilder("annualStatementGenerationStep", repository)
                .<AnnualAccountInput, AnnualStatementRow>chunk(CHUNK_SIZE, transactionManager)
                .reader(reader)
                .processor(processor)
                .writer(writer)
                .faultTolerant()
                .retry(IOException.class)
                .retry(TransientDataAccessException.class)
                .retryLimit(3)
                .skip(IllegalArgumentException.class)
                .skipLimit(10000)
                .listener(batchSkipListener)
                .build();
    }

    @Bean
    Job dailyTransactionReportJob(JobRepository repository,
                                 @Qualifier("dailyTransactionReportStep") Step step) {
        return new JobBuilder("dailyTransactionReportJob", repository).start(step).build();
    }

    @Bean
    Job monthlyInterestCalculationJob(JobRepository repository,
                                     @Qualifier("monthlyInterestCalculationStep") Step step) {
        return new JobBuilder("monthlyInterestCalculationJob", repository).start(step).build();
    }

    @Bean
    Job annualStatementGenerationJob(JobRepository repository,
                                     @Qualifier("annualStatementGenerationStep") Step step) {
        return new JobBuilder("annualStatementGenerationJob", repository).start(step).build();
    }

    static DailyTransactionRow dailyRow(TransactionInput input, LocalDate targetDate) {
        String anomaly = "";
        LocalDate date = null;
        try {
            date = LegacyDates.parse(input.date());
        } catch (IllegalArgumentException ex) {
            anomaly = "INVALID_DATE";
        }
        if (date != null && !date.equals(targetDate)) {
            return null;
        }

        BigDecimal amount = decimalOrNull(input.amount());
        if (amount == null || amount.signum() <= 0) {
            anomaly = appendAnomaly(anomaly, "INVALID_AMOUNT");
        }
        String type = input.type() == null ? "" : input.type().trim();
        if (!type.equalsIgnoreCase("credito") && !type.equalsIgnoreCase("debito")) {
            anomaly = appendAnomaly(anomaly, "INVALID_TYPE");
        }
        String normalizedDate = date == null ? input.date() : date.toString();
        return new DailyTransactionRow(input.id(), normalizedDate,
                amount == null ? input.amount() : amount.toPlainString(), type, anomaly);
    }

    static MonthlyInterestRow monthlyInterest(InterestInput input, YearMonth period) {
        return monthlyInterest(input, period, new BigDecimal("0.005"), new BigDecimal("0.020"));
    }

    private static MonthlyInterestRow monthlyInterest(InterestInput input, YearMonth period,
                                                      BigDecimal savingsRate, BigDecimal loanRate) {
        long accountId;
        BigDecimal balance;
        try {
            accountId = Long.parseLong(input.accountId().trim());
            balance = new BigDecimal(input.balance().trim());
        } catch (RuntimeException ex) {
            throw new IllegalArgumentException("cuenta o saldo inválido", ex);
        }
        if (balance.signum() < 0) {
            throw new IllegalArgumentException("el saldo no puede ser negativo");
        }
        String normalizedType = input.type() == null ? "" : input.type().trim().toLowerCase();
        BigDecimal rate = switch (normalizedType) {
            case "ahorro", "savings" -> savingsRate;
            case "prestamo", "hipoteca", "loan", "mortgage" -> loanRate;
            default -> throw new IllegalArgumentException("tipo de cuenta no reconocido");
        };
        BigDecimal interest = balance.multiply(rate).setScale(2, RoundingMode.HALF_UP);
        return new MonthlyInterestRow(accountId, period.toString(), normalizedType,
                balance.setScale(2, RoundingMode.HALF_UP), rate, interest,
                balance.add(interest).setScale(2, RoundingMode.HALF_UP));
    }

    static AnnualStatementRow annualStatementRow(AnnualAccountInput input, int targetYear) {
        long accountId;
        LocalDate date;
        BigDecimal amount;
        try {
            accountId = Long.parseLong(input.accountId().trim());
            date = LegacyDates.parse(input.date());
            amount = new BigDecimal(input.amount().trim());
        } catch (RuntimeException ex) {
            throw new IllegalArgumentException("cuenta, fecha o monto inválido", ex);
        }
        if (amount.signum() < 0) {
            throw new IllegalArgumentException("el monto no puede ser negativo");
        }
        if (date.getYear() != targetYear) {
            return null;
        }
        return new AnnualStatementRow(accountId, targetYear, date.toString(),
                input.transaction(), amount.setScale(2, RoundingMode.HALF_UP),
                input.description());
    }

    private static BigDecimal decimalOrNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return new BigDecimal(value.trim());
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private static String appendAnomaly(String current, String next) {
        return current.isBlank() ? next : current + "|" + next;
    }

    private static Resource input(String directory, String filename) {
        return new FileSystemResource(Path.of(directory, filename));
    }

    private static <T> org.springframework.batch.infrastructure.item.file.FlatFileItemWriter<T> writer(
            String outputFile, String header, LineAggregator<T> aggregator) {
        return new FlatFileItemWriterBuilder<T>()
                .name("csvWriter-" + Path.of(outputFile).getFileName())
                .resource(new FileSystemResource(outputFile))
                .headerCallback(out -> out.write(header))
                .lineAggregator(aggregator)
                .build();
    }
}
