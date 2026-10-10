package com.bancoxyz.batch.config;

import com.bancoxyz.batch.model.LegacyBatchRecords.AnnualAccountInput;
import com.bancoxyz.batch.model.LegacyBatchRecords.DailyTransactionRow;
import com.bancoxyz.batch.model.LegacyBatchRecords.InterestInput;
import com.bancoxyz.batch.model.LegacyBatchRecords.MonthlyInterestRow;
import com.bancoxyz.batch.model.LegacyBatchRecords.TransactionInput;
import com.bancoxyz.batch.support.LegacyDates;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

class BatchJobsConfigurationTest {

    @Test
    void parsesSupportedLegacyDateFormatsAndRejectsInvalidDates() {
        assertThat(LegacyDates.parse("2024-06-30")).isEqualTo(LocalDate.of(2024, 6, 30));
        assertThat(LegacyDates.parse("03-04-2024")).isEqualTo(LocalDate.of(2024, 4, 3));
        assertThat(LegacyDates.parse("04/05/2024")).isEqualTo(LocalDate.of(2024, 5, 4));
        assertThat(LegacyDates.parse("2024/04/09")).isEqualTo(LocalDate.of(2024, 4, 9));
        assertThatIllegalArgumentException().isThrownBy(() -> LegacyDates.parse("2024-13-01"));
    }

    @Test
    void dailyReportFiltersByDateAndFlagsInvalidOperationData() {
        var matching = BatchJobsConfiguration.dailyRow(
                new TransactionInput("21", "2024-06-30", "1250", "credito"),
                LocalDate.of(2024, 6, 30));
        var filtered = BatchJobsConfiguration.dailyRow(
                new TransactionInput("22", "2024-06-29", "1250", "credito"),
                LocalDate.of(2024, 6, 30));
        DailyTransactionRow invalid = BatchJobsConfiguration.dailyRow(
                new TransactionInput("23", "2024-13-01", "", "unknown"),
                LocalDate.of(2024, 6, 30));

        assertThat(matching.anomaly()).isEmpty();
        assertThat(filtered).isNull();
        assertThat(invalid.anomaly()).isEqualTo("INVALID_DATE|INVALID_AMOUNT|INVALID_TYPE");
    }

    @Test
    void monthlyInterestUsesConfiguredRatesAndCurrencyRounding() {
        MonthlyInterestRow row = BatchJobsConfiguration.monthlyInterest(
                new InterestInput("137", "Bob", "7000", "30", "prestamo"),
                YearMonth.of(2024, 6));

        assertThat(row.rate()).isEqualByComparingTo("0.020");
        assertThat(row.interest()).isEqualByComparingTo("140.00");
        assertThat(row.total()).isEqualByComparingTo(new BigDecimal("7140.00"));
    }

    @Test
    void annualStatementSkipsRecordsOutsideRequestedYear() {
        var row = BatchJobsConfiguration.annualStatementRow(
                new AnnualAccountInput("103", "08-03-2024", "deposito", "3000", "Ingreso mensual"),
                2024);
        var filtered = BatchJobsConfiguration.annualStatementRow(
                new AnnualAccountInput("103", "08-03-2024", "deposito", "3000", ""),
                2023);

        assertThat(row.date()).isEqualTo("2024-03-08");
        assertThat(row.amount()).isEqualByComparingTo("3000.00");
        assertThat(filtered).isNull();
    }
}
