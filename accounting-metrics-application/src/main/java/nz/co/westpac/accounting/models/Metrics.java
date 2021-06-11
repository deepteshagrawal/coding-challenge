package nz.co.westpac.accounting.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_NULL)
public class Metrics {

	private String revenue;
	private String expenses;
	private String grossProfitMargin;
	private String netProfitMargin;
	private String workingCapitalRatio;
	
	public String getRevenue() {
		return revenue;
	}
	public void setRevenue(String revenue) {
		this.revenue = revenue;
	}
	public String getExpenses() {
		return expenses;
	}
	public void setExpenses(String expenses) {
		this.expenses = expenses;
	}
	public String getGrossProfitMargin() {
		return grossProfitMargin;
	}
	public void setGrossProfitMargin(String grossProfitMargin) {
		this.grossProfitMargin = grossProfitMargin;
	}
	public String getNetProfitMargin() {
		return netProfitMargin;
	}
	public void setNetProfitMargin(String netProfitMargin) {
		this.netProfitMargin = netProfitMargin;
	}
	public String getWorkingCapitalRatio() {
		return workingCapitalRatio;
	}
	public void setWorkingCapitalRatio(String workingCapitalRatio) {
		this.workingCapitalRatio = workingCapitalRatio;
	}
}
