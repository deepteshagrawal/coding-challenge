package nz.co.westpac.accounting.services.impl;

import java.io.File;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import nz.co.westpac.accounting.models.BookKeeping;
import nz.co.westpac.accounting.models.Metrics;
import nz.co.westpac.accounting.services.AccountingMetricsInterface;

@Service
public class AccountingMetricsService implements AccountingMetricsInterface
{

    @Override
    public Metrics retrieveMetricsInformation() throws Exception
    {
        final Metrics metrics = new Metrics();

        final BookKeeping book = populateBook();

        Locale locale = new Locale("en", "NZ");
        NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(locale);
        currencyFormatter.setMaximumFractionDigits(0);

        metrics.setRevenue(currencyFormatter.format(calculateRevenue(book)));
        metrics.setExpenses(currencyFormatter.format(calculateExpenses(book)));

        NumberFormat percentFormatter = NumberFormat.getPercentInstance(locale);
        percentFormatter.setMaximumFractionDigits(1);

        metrics.setGrossProfitMargin(percentFormatter.format(calculateGrossProfitMargin(book)));
        metrics.setNetProfitMargin(percentFormatter.format(calculateNetProfitMargin(book)));
        metrics.setWorkingCapitalRatio(percentFormatter.format(calculateWorkingCapitalRatio(book)));

        return metrics;

    }

    private BookKeeping populateBook() throws Exception
    {
        final File directory = new File("src/main/resources/data.json");
        return new ObjectMapper().readValue(directory, BookKeeping.class);
    }

    private double calculateRevenue(final BookKeeping book)
    {
        return book.getData().stream().filter(account -> "revenue".equals(account.getAccount_category())).mapToDouble(account -> account.getTotal_value())
            .sum();
    }

    private double calculateExpenses(final BookKeeping book)
    {
        return book.getData().stream().filter(account -> "expense".equals(account.getAccount_category())).mapToDouble(
            account -> account.getTotal_value()).sum();
    }

    private double calculateGrossProfitMargin(final BookKeeping book)
    {
        final double revenue = calculateRevenue(book);
        final double grossProfit = book.getData().stream().filter(account -> "sales".equals(account.getAccount_type()) && "debit".equals(account.getValue_type()))
            .mapToDouble(account -> account.getTotal_value()).sum();
        return grossProfit / revenue;
    }

    private double calculateNetProfitMargin(final BookKeeping book)
    {
        final double revenue = calculateRevenue(book);
        final double expenses = calculateExpenses(book);
        final double netProfit = revenue - expenses;
        return netProfit / revenue;
    }

    private double calculateTotalAssets(final BookKeeping book)
    {
        final List<String> accountTypes = List.of("current", "bank", "current_accounts_receivable");
        final double totalDebitAssets =
                book.getData().stream()
                    .filter(account -> "assets".equals(account.getAccount_category()) && "debit".equals(account.getValue_type())
                            && accountTypes.contains(account.getAccount_type()))
                    .mapToDouble(account -> account.getTotal_value()).sum();
        final double totalCreditAssets =
                book.getData().stream()
                    .filter(account -> "assets".equals(account.getAccount_category()) && "credit".equals(account.getValue_type())
                            && accountTypes.contains(account.getAccount_type()))
                    .mapToDouble(account -> account.getTotal_value()).sum();
        return totalDebitAssets - totalCreditAssets;
    }

    private double calculateTotalLiabilities(final BookKeeping book)
    {
        final List<String> accountTypes = List.of("current", "current_accounts_payable");
        final double totalCreditLiability =
                book.getData().stream()
                    .filter(account -> "liability".equals(account.getAccount_category()) && "credit".equals(account.getValue_type())
                            && accountTypes.contains(account.getAccount_type()))
                    .mapToDouble(account -> account.getTotal_value()).sum();
        final double totalDebitLiability =
                book.getData().stream()
                    .filter(account -> "assets".equals(account.getAccount_category()) && "debit".equals(account.getValue_type())
                            && accountTypes.contains(account.getAccount_type()))
                    .mapToDouble(account -> account.getTotal_value()).sum();
        return totalCreditLiability - totalDebitLiability;
    }

    private double calculateWorkingCapitalRatio(final BookKeeping book)
    {
        return calculateTotalAssets(book) / calculateTotalLiabilities(book);
    }

}
