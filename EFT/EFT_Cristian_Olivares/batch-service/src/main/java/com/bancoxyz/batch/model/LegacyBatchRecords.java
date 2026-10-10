package com.bancoxyz.batch.model;

import java.math.BigDecimal;

public final class LegacyBatchRecords {

    private LegacyBatchRecords() {
    }

    public record TransactionInput(String id, String date, String amount, String type) {
    }

    public record InterestInput(String accountId, String name, String balance, String age, String type) {
    }

    public record AnnualAccountInput(String accountId, String date, String transaction,
                                     String amount, String description) {
    }

    public record DailyTransactionRow(String id, String date, String amount, String type, String anomaly) {
    }

    public record MonthlyInterestRow(long accountId, String period, String type, BigDecimal balance,
                                     BigDecimal rate, BigDecimal interest, BigDecimal total) {
    }

    public record AnnualStatementRow(long accountId, int year, String date, String transaction,
                                    BigDecimal amount, String description) {
    }
}
