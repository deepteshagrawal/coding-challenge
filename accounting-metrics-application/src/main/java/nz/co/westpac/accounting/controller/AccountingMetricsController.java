package nz.co.westpac.accounting.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import nz.co.westpac.accounting.models.Metrics;
import nz.co.westpac.accounting.services.AccountingMetricsInterface;

@RestController
public class AccountingMetricsController {
	
	@Autowired
	private AccountingMetricsInterface accountingMetricsService;

	@GetMapping("/challenge")
	public Metrics challenge() throws Exception {
		return accountingMetricsService.retrieveMetricsInformation();
	}
}	