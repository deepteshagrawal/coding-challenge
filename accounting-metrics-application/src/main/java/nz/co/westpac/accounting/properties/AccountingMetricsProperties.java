package nz.co.westpac.accounting.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

/**
 * Configuration properties for the accounting metrics.
 */
@ConfigurationProperties("accounting.metrics")
@Component
public class AccountingMetricsProperties
{
    private Locale locale = new Locale();

    private CurrencyFormatter currencyFormatter = new CurrencyFormatter();

    private PercentFormatter percentFormatter = new PercentFormatter();

    public CurrencyFormatter getCurrencyFormatter()
    {
        return currencyFormatter;
    }

    public PercentFormatter getPercentFormatter()
    {
        return percentFormatter;
    }

    public Locale getLocale()
    {
        return locale;
    }

    /**
     * Configuration properties for the accounting metrics locale.
     */
    @Valid
    public class Locale
    {
        @NotNull
        private String language;

        @NotNull
        private String country;

        public String getLanguage()
        {
            return language;
        }

        public void setLanguage(String language)
        {
            this.language = language;
        }

        public String getCountry()
        {
            return country;
        }

        public void setCountry(String country)
        {
            this.country = country;
        }
    }

    /**
     * Configuration properties for the accounting metrics currency formatter.
     */
    @Valid
    public class CurrencyFormatter
    {
        @NotNull
        private Integer maximumFractionDigits;

        public Integer getMaximumFractionDigits()
        {
            return maximumFractionDigits;
        }

        public void setMaximumFractionDigits(Integer maximumFractionDigits)
        {
            this.maximumFractionDigits = maximumFractionDigits;
        }

    }

    /**
     * Configuration properties for the accounting metrics percent formatter.
     */
    @Valid
    public class PercentFormatter
    {
        @NotNull
        private Integer maximumFractionDigits;

        public Integer getMaximumFractionDigits()
        {
            return maximumFractionDigits;
        }

        public void setMaximumFractionDigits(Integer maximumFractionDigits)
        {
            this.maximumFractionDigits = maximumFractionDigits;
        }

    }
}
