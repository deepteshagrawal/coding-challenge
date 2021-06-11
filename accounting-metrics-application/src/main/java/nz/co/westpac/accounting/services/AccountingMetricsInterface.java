package nz.co.westpac.accounting.services;

import nz.co.westpac.accounting.models.Metrics;

public interface AccountingMetricsInterface {

	public Metrics retrieveMetricsInformation() throws Exception;
}
