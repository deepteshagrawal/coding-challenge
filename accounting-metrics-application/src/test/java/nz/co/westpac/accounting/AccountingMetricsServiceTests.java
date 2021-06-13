package nz.co.westpac.accounting;

import nz.co.westpac.accounting.models.Accounting;
import nz.co.westpac.accounting.models.BookKeeping;
import nz.co.westpac.accounting.models.Metrics;
import nz.co.westpac.accounting.services.impl.AccountingMetricsService;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.io.File;
import java.math.BigDecimal;
import java.util.List;

@SpringBootTest
public class AccountingMetricsServiceTests
{
    @Autowired
    private AccountingMetricsService accountingMetricsService;

    @MockBean
    private ObjectMapper objectMapper;

    @Test
    public void testCurrencyFormatting() throws Exception
    {
        final BookKeeping book = new BookKeeping();
        final Accounting accounting = new Accounting();
        book.setData(List.of(accounting));
        accounting.setAccount_category("revenue");
        accounting.setTotal_value(new BigDecimal(123456789.56789));

        Mockito.doReturn(book).when(objectMapper).readValue(Mockito.any(File.class), Mockito.eq(BookKeeping.class));

        final Metrics metrics = accountingMetricsService.retrieveSpecificMetricsInformation("revenue");

        Assertions.assertEquals("$123,456,790", metrics.getRevenue(),
            "Currency Format - Prefixed with $, A comma is used to separate every 3 digits, Cents are removed.");
    }

    @Test
    public void testPercentFormatting() throws Exception
    {
        final BookKeeping book = new BookKeeping();
        final Accounting revenue = new Accounting();
        revenue.setAccount_category("revenue");
        revenue.setTotal_value(new BigDecimal(128));
        final Accounting expense = new Accounting();
        expense.setAccount_category("expense");
        expense.setTotal_value(new BigDecimal(150));
        book.setData(List.of(revenue, expense));

        Mockito.doReturn(book).when(objectMapper).readValue(Mockito.any(File.class), Mockito.eq(BookKeeping.class));

        final Metrics metrics = accountingMetricsService.retrieveSpecificMetricsInformation("netProfitMargin");

        Assertions.assertEquals("-17.2%", metrics.getNetProfitMargin(),
            "Percent Format - percentage value formatted to one decimal digit and prefixed with a % sign.");
    }

}
