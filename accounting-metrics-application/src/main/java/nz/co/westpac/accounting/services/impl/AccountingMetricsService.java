package nz.co.westpac.accounting.services.impl;

import nz.co.westpac.accounting.models.BookKeeping;
import nz.co.westpac.accounting.models.Metrics;
import nz.co.westpac.accounting.properties.AccountingMetricsProperties;
import nz.co.westpac.accounting.services.AccountingMetricsInterface;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.io.File;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

/**
 * Implementation Service to calculate accounting metrics.
 */
@Service
public class AccountingMetricsService implements AccountingMetricsInterface
{
    @Autowired
    private AccountingMetricsProperties properties;

    @Override
    public Metrics retrieveMetricsInformation()
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
    public Metrics retrieveSpecificMetricsInformation(final String fieldName)
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
            default:
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Not a valid metrics information requested.");
        }

        return metrics;
    }

    /**
     * Get currency format based on locale defined in configuration.
     * 
     * @return numberformat with max fraction digits defined in configuration.
     */
    private NumberFormat getCurrencyFormatter()
    {
        final Locale locale = new Locale(properties.getLocale().getLanguage(), properties.getLocale().getCountry());
        final NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(locale);
        currencyFormatter.setMaximumFractionDigits(properties.getCurrencyFormatter().getMaximumFractionDigits());
        return currencyFormatter;
    }

    /**
     * Get percent format based on locale defined in configuration.
     * 
     * @return numberformat with max fraction digits defined in configuration.
     */
    private NumberFormat getPercentFormatter()
    {
        final Locale locale = new Locale(properties.getLocale().getLanguage(), properties.getLocale().getCountry());
        final NumberFormat percentFormatter = NumberFormat.getPercentInstance(locale);
        percentFormatter.setMaximumFractionDigits(properties.getPercentFormatter().getMaximumFractionDigits());
        return percentFormatter;
    }

    /**
     * Read external file and map it into {@code BookKeeping}
     * 
     * @return book keeping information.
     * @throws ResponseStatusException in case problem in reading external file, or file structure
     *         is not correct, or mappig is corrupt.
     */
    private BookKeeping populateBook()
    {
        final File directory = new File("src/main/resources/data.json");
        try {
            return new ObjectMapper().readValue(directory, BookKeeping.class);
        } catch (JsonParseException parseException) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Data file contains invalid content", parseException);
        } catch (JsonMappingException mappingException) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "data file JSON structure does not match structure expected", mappingException);
        } catch (IOException ioException) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed reading data file.", ioException);
        }
    }

    /**
     * Calculate revenue - This should be calculated by adding up all the values under total_value
     * where the account_category field is set to revenue
     * 
     * @param book to read all values based on above criteria.
     * @return revenue metrics information.
     */
    private double calculateRevenue(final BookKeeping book)
    {
        return book.getData().stream().filter(account -> "revenue".equals(account.getAccount_category())).mapToDouble(account -> account.getTotal_value())
            .sum();
    }

    /**
     * Calculate expenses - This should be calculated by adding up all the values under total_value
     * where the account_category field is set to expense
     * 
     * @param book to read all values based on above criteria.
     * @return expenses metrics information.
     */
    private double calculateExpenses(final BookKeeping book)
    {
        return book.getData().stream().filter(account -> "expense".equals(account.getAccount_category())).mapToDouble(
            account -> account.getTotal_value()).sum();
    }

    /**
     * Calculate gross profit margin - This is calculated in two steps: first by adding all the
     * total_value fields where the account_type is set to sales and the value_type is set to debit
     * ; then dividing that by the revenue value calculated earlier to generate a percentage value.
     * 
     * @param book to read all values based on above criteria.
     * @return gross profit margin metrics information.
     */
    private double calculateGrossProfitMargin(final BookKeeping book)
    {
        final double revenue = calculateRevenue(book);
        final double grossProfit =
                book.getData().stream().filter(account -> "sales".equals(account.getAccount_type()) && "debit".equals(account.getValue_type()))
                    .mapToDouble(account -> account.getTotal_value()).sum();
        return grossProfit / revenue;
    }

    /**
     * Calculate net profit margin - This metric is calculated by subtracting the expenses value
     * from the revenue value and dividing the remainder by revenue to calculate a percentage.
     * 
     * @param book to read all values based on above criteria.
     * @return net profit margin metrics information.
     */
    private double calculateNetProfitMargin(final BookKeeping book)
    {
        final double revenue = calculateRevenue(book);
        final double expenses = calculateExpenses(book);
        final double netProfit = revenue - expenses;
        return netProfit / revenue;
    }

    /**
     * Calculate total assets - 1. adding the total_value from all records where the
     * account_category is set to assets , the value_type is set to debit , and the account_type is
     * one of current , bank , or current_accounts_receivable 2. subtracting the total_value from
     * all records where the account_category is set to assets , the value_type is set to credit ,
     * and the account_type is one of current , bank , or current_accounts_receivable
     * 
     * @param book to read all values based on above criteria.
     * @return total assets metrics information.
     */
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

    /**
     * Calculate total liabilities - 1. adding the total_value from all records where the
     * account_category is set to liability , the value_type is set to credit , and the account_type
     * is one of current or current_accounts_payable 2. subtracting the total_value from all records
     * where the account_category is set to liability , the value_type is set to debit , and the
     * account_type is one current or current_accounts_payable
     * 
     * @param book to read all values based on above criteria.
     * @return total liabilities metrics information.
     */
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

    /**
     * Calculate working capital ratio - This is calculated dividing the assets by the liabilities
     * creating a percentage value.
     * 
     * @param book to read all values based on above criteria.
     * @return working capital ratio metrics information.
     */
    private double calculateWorkingCapitalRatio(final BookKeeping book)
    {
        return calculateTotalAssets(book) / calculateTotalLiabilities(book);
    }

}
