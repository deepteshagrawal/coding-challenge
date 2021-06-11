package nz.co.westpac.accounting.controller;

import nz.co.westpac.accounting.models.Metrics;
import nz.co.westpac.accounting.services.AccountingMetricsInterface;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AccountingMetricsController {
	
	@Autowired
	private AccountingMetricsInterface accountingMetricsService;

	@GetMapping("/challenge")
    public Metrics retrieveMetricsInformation() throws Exception
    {
        return accountingMetricsService.retrieveMetricsInformation();
	}

    @GetMapping("/challenge/{fieldName}")
    public Metrics retrieveFieldSpecificMetricsInformation(@PathVariable String fieldName) throws Exception
    {
        return accountingMetricsService.retrieveSpecificMetricsInformation(fieldName);
    }
}	