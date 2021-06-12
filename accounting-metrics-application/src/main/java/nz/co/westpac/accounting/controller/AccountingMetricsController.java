package nz.co.westpac.accounting.controller;

import nz.co.westpac.accounting.models.Metrics;
import nz.co.westpac.accounting.services.AccountingMetricsInterface;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

/**
 * Rest Controller to handle the accounting metrics application service requests.
 */
@RestController
public class AccountingMetricsController
{
    @Autowired
    private AccountingMetricsInterface accountingMetricsService;

    /**
     * Request mapping to get all non null metrics information.
     * 
     * @return Http status 200 with all non-null metrics information.
     * @return Http status 500 with its error and message when the application failed to process the
     *         request.
     */
    @GetMapping("/challenge")
    public Metrics retrieveMetricsInformation()
    {
        return accountingMetricsService.retrieveMetricsInformation();
    }

    /**
     * Request mapping to get specific metrics information asked for in fieldName. These endpoints
     * can be more optimised using GraphQL, where, only return the asked information. But, in that
     * case, requested information will be passed in as a body. So, to pass that information as a
     * path parameter, will require more analysis to achieve that.
     * 
     * @param fieldName is requested metrics information.
     * @return metrics information asked for in fieldName.
     * @return Http status 404 with its error and message for Invalid field name.
     * @return Http status 500 with its error and message when the application failed to process the
     *         request.
     */
    @GetMapping("/challenge/{fieldName}")
    public Metrics retrieveFieldSpecificMetricsInformation(@PathVariable String fieldName)
    {
        return accountingMetricsService.retrieveSpecificMetricsInformation(fieldName);
    }
}
