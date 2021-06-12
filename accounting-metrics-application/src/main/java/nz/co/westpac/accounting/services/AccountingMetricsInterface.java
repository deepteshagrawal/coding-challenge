package nz.co.westpac.accounting.services;

import nz.co.westpac.accounting.models.Metrics;

import org.springframework.web.server.ResponseStatusException;

/**
 * Interface for Accounting Metrics Service which is responsible to read metrics information from
 * the external file and then calculate metrics: Revenue, Expenses, Gross Profit Margin, Net Profit
 * Margin, workingCapitalRatio. This service also does currency and percent formatting on the
 * metrics information based on locale defined in properties.
 */
public interface AccountingMetricsInterface
{

    /**
     * Read Metrics Information from external file, calculate all metrics information and then do
     * formatting.
     * 
     * @return all metrics information.
     * @throws ResponseStatusException in case problem in reading external file, or file structure
     *         is not correct, or mappig is corrupt.
     */
    public Metrics retrieveMetricsInformation();

    /**
     * Read Metrics Information from external file, calculate fieldName specific metrics information
     * and then do formatting.
     * 
     * @param fieldName to calculate metrics information.
     * @return only metrics which is asked for in fieldName.
     * @throws ResponseStatusException in case invalid field is passed, or problem in reading
     *         external file, or file structure is not correct, or mappig is corrupt.
     */
    public Metrics retrieveSpecificMetricsInformation(String fieldName);
}
