package nz.co.westpac.accounting;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
public class AccountingMetricsApplicationTests
{
    @Autowired
    private MockMvc mockMvc;

    @Test
    public void testAllMetricsInformation() throws Exception
    {
        this.mockMvc.perform(get("/challenge")).andDo(print())
            .andExpect(status().isOk())
            .andExpect(content().string(
                containsString(
                    "{\"revenue\":\"$32,431\",\"expenses\":\"$36,530\",\"grossProfitMargin\":\"0%\",\"netProfitMargin\":\"-12.6%\",\"workingCapitalRatio\":\"118.8%\"}")));
    }

    @Test
    public void testRevenueMetricsInformation() throws Exception
    {
        this.mockMvc.perform(get("/challenge/revenue")).andDo(print())
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("{\"revenue\":\"$32,431\"}")));
    }

    @Test
    public void testExpensesMetricsInformation() throws Exception
    {
        this.mockMvc.perform(get("/challenge/expenses")).andDo(print())
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("{\"expenses\":\"$36,530\"}")));
    }

    @Test
    public void testGrossProfitMarginMetricsInformation() throws Exception
    {
        this.mockMvc.perform(get("/challenge/grossProfitMargin")).andDo(print())
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("\"grossProfitMargin\":\"0%\"}")));
    }

    @Test
    public void testNetProfitMarginMetricsInformation() throws Exception
    {
        this.mockMvc.perform(get("/challenge/netProfitMargin")).andDo(print())
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("\"netProfitMargin\":\"-12.6%\"}")));
    }

    @Test
    public void testWorkingCapitalRatioMetricsInformation() throws Exception
    {
        this.mockMvc.perform(get("/challenge/workingCapitalRatio")).andDo(print())
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("\"workingCapitalRatio\":\"118.8%\"")));
    }

    @Test
    public void testInvalidFieldMetricsInformation() throws Exception
    {
        this.mockMvc.perform(get("/challenge/invalidField")).andDo(print())
            .andExpect(status().isNotFound())
            .andExpect(status().reason(containsString("Not a valid metrics information requested.")));
    }
}
