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
import java.math.BigDecimal;
import java.math.RoundingMode;
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

    @Autowired
    private ObjectMapper objectMapper;

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
            return objectMapper.readValue(directory, BookKeeping.class);
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
    private BigDecimal calculateRevenue(final BookKeeping book)
    {
        return book.getData().stream().filter(account -> "revenue".equals(account.getAccount_category())).map(account -> account.getTotal_value())
            .reduce((x, y) -> x.add(y)).orElse(new BigDecimal(0));

    }

    /**
     * Calculate expenses - This should be calculated by adding up all the values under total_value
     * where the account_category field is set to expense
     * 
     * @param book to read all values based on above criteria.
     * @return expenses metrics information.
     */
    private BigDecimal calculateExpenses(final BookKeeping book)
    {
        return book.getData().stream().filter(account -> "expense".equals(account.getAccount_category())).map(account -> account.getTotal_value())
            .reduce((x, y) -> x.add(y)).orElse(new BigDecimal(0));
    }

    /**
     * Calculate gross profit margin - This is calculated in two steps: first by adding all the
     * total_value fields where the account_type is set to sales and the value_type is set to debit
     * ; then dividing that by the revenue value calculated earlier to generate a percentage value.
     * 
     * @param book to read all values based on above criteria.
     * @return gross profit margin metrics information.
     */
    private BigDecimal calculateGrossProfitMargin(final BookKeeping book)
    {
        final BigDecimal revenue = calculateRevenue(book);
        final BigDecimal grossProfit =
                book.getData().stream().filter(account -> "sales".equals(account.getAccount_type()) && "debit".equals(account.getValue_type()))
                    .map(account -> account.getTotal_value()).reduce((x, y) -> x.add(y)).orElse(new BigDecimal(0));

        return grossProfit.divide(revenue, properties.getPercentFormatter().getMaximumFractionDigits(), RoundingMode.HALF_EVEN);
    }

    /**
     * Calculate net profit margin - This metric is calculated by subtracting the expenses value
     * from the revenue value and dividing the remainder by revenue to calculate a percentage.
     * 
     * @param book to read all values based on above criteria.
     * @return net profit margin metrics information.
     */
    private BigDecimal calculateNetProfitMargin(final BookKeeping book)
    {
        final BigDecimal revenue = calculateRevenue(book);
        final BigDecimal expenses = calculateExpenses(book);
        final BigDecimal netProfit = revenue.subtract(expenses);

        return netProfit.divide(revenue, properties.getPercentFormatter().getMaximumFractionDigits(), RoundingMode.HALF_EVEN);
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
    private BigDecimal calculateTotalAssets(final BookKeeping book)
    {
        final List<String> accountTypes = List.of("current", "bank", "current_accounts_receivable");
        final BigDecimal totalDebitAssets =
                book.getData().stream()
                    .filter(account -> "assets".equals(account.getAccount_category()) && "debit".equals(account.getValue_type())
                            && accountTypes.contains(account.getAccount_type()))
                    .map(account -> account.getTotal_value()).reduce((x, y) -> x.add(y)).orElse(new BigDecimal(0));
        final BigDecimal totalCreditAssets =
                book.getData().stream()
                    .filter(account -> "assets".equals(account.getAccount_category()) && "credit".equals(account.getValue_type())
                            && accountTypes.contains(account.getAccount_type()))
                    .map(account -> account.getTotal_value()).reduce((x, y) -> x.add(y)).orElse(new BigDecimal(0));
        return totalDebitAssets.subtract(totalCreditAssets);
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
    private BigDecimal calculateTotalLiabilities(final BookKeeping book)
    {
        final List<String> accountTypes = List.of("current", "current_accounts_payable");
        final BigDecimal totalCreditLiability =
                book.getData().stream()
                    .filter(account -> "liability".equals(account.getAccount_category()) && "credit".equals(account.getValue_type())
                            && accountTypes.contains(account.getAccount_type()))
                    .map(account -> account.getTotal_value()).reduce((x, y) -> x.add(y)).orElse(new BigDecimal(0));
        final BigDecimal totalDebitLiability =
                book.getData().stream()
                    .filter(account -> "assets".equals(account.getAccount_category()) && "debit".equals(account.getValue_type())
                            && accountTypes.contains(account.getAccount_type()))
                    .map(account -> account.getTotal_value()).reduce((x, y) -> x.add(y)).orElse(new BigDecimal(0));
        return totalCreditLiability.subtract(totalDebitLiability);
    }

    /**
     * Calculate working capital ratio - This is calculated dividing the assets by the liabilities
     * creating a percentage value.
     * 
     * @param book to read all values based on above criteria.
     * @return working capital ratio metrics information.
     */
    private BigDecimal calculateWorkingCapitalRatio(final BookKeeping book)
    {
        final BigDecimal totalAssets = calculateTotalAssets(book);
        final BigDecimal totalLiabilities = calculateTotalLiabilities(book);

        return totalAssets.divide(totalLiabilities, properties.getPercentFormatter().getMaximumFractionDigits(), RoundingMode.HALF_EVEN);
    }

}
