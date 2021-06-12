package nz.co.westpac.accounting;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry class for the accounting metrics application micro-service.
 */
@SpringBootApplication(scanBasePackageClasses = {AccountingMetricsApplication.class})
public class AccountingMetricsApplication
{

    /**
     * Entry point for the micro-service.
     * 
     * @param args is command line arguments.
     */
    public static void main(String[] args)
    {
        SpringApplication.run(AccountingMetricsApplication.class, args);
    }
}
