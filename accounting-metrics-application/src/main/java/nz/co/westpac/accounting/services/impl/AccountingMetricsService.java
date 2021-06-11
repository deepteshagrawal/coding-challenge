package nz.co.westpac.accounting.services.impl;

import nz.co.westpac.accounting.models.BookKeeping;
import nz.co.westpac.accounting.models.Metrics;
import nz.co.westpac.accounting.properties.AccountingMetricsProperties;
import nz.co.westpac.accounting.services.AccountingMetricsInterface;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

@Service
public class AccountingMetricsService implements AccountingMetricsInterface
{
    @Autowired
    private AccountingMetricsProperties properties;

    @Override
    public Metrics retrieveMetricsInformation() throws Exception
    {
        final Metrics metrics = new Metrics();
        final BookKeeping book = populateBook();

        metrics.setRevenue(getCurrencyFormatter().format(calculateRevenue(book)));
        metrics.setExpenses(getCurrencyFormatter().format(calculateExpenses(book)));
        metrics.setGrossProfitMargin(getPercentFormatter().format(calculateGrossProfitMargin(book)));
        metrics.setNetProfitMargin(getPercentFormatter().format(calculateNetProfitMargin(book)));
        metrics.setWorkingCapitalRatio(getPercentFormatter().format(calculateWorkingCapitalRatio(book)));

        return metrics;

    }

    @Override
    public Metrics retrieveSpecificMetricsInformation(final String fieldName) throws Exception
    {
        final Metrics metrics = new Metrics();
        final BookKeeping book = populateBook();

        switch (fieldName) {
            case "revenue":
                metrics.setRevenue(getCurrencyFormatter().format(calculateRevenue(book)));
                break;
            case "expenses":
                metrics.setExpenses(getCurrencyFormatter().format(calculateExpenses(book)));
                break;
            case "grossProfitMargin":
                metrics.setGrossProfitMargin(getPercentFormatter().format(calculateGrossProfitMargin(book)));
                break;
            case "netProfitMargin":
                metrics.setNetProfitMargin(getPercentFormatter().format(calculateNetProfitMargin(book)));
                break;
            case "workingCapitalRatio":
                metrics.setWorkingCapitalRatio(getPercentFormatter().format(calculateWorkingCapitalRatio(book)));
                break;
        }

        return metrics;
    }

    private NumberFormat getCurrencyFormatter()
    {
        final Locale locale = new Locale(properties.getLocale().getLanguage(), properties.getLocale().getCountry());
        final NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(locale);
        currencyFormatter.setMaximumFractionDigits(properties.getCurrencyFormatter().getMaximumFractionDigits());
        return currencyFormatter;
    }

    private NumberFormat getPercentFormatter()
    {
        final Locale locale = new Locale(properties.getLocale().getLanguage(), properties.getLocale().getCountry());
        final NumberFormat percentFormatter = NumberFormat.getPercentInstance(locale);
        percentFormatter.setMaximumFractionDigits(properties.getPercentFormatter().getMaximumFractionDigits());
        return percentFormatter;
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
