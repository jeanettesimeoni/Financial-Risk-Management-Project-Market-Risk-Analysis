package it.univr.riskmanagement;

import java.io.IOException;
import java.util.Arrays;
import java.time.LocalDate;

/*
This class takes the output of DataCollectionAndPlotting to compute portfolio returns
and produce the corresponding time series

@author Stranger Risks Group
*/

public class DataManagement {
	
	/*
	We use the methods in DataCollectionAndPlotting to inizialize the vectors containing prices and dates.
	We chose Eni as asset 1 and Amplifon as asset 2 
	because these two assets show a low correlation, 
	i.e. 0,187 (computed using historical data)
	
	@array pricesAsset1: prices of asset 1
	@array pricesAsset2: prices of asset 2
	@array dates: corresponding dates
	*/
	
	private double[] pricesAsset1; //vector of daily closing prices for Asset 1
	private double[] pricesAsset2; //vector of daily closing prices for Asset 2
	private LocalDate[] dates; // vector of corresponding trading dates
	
	/*
    Constructor: loads prices and dates from Excel files via DataCollectionAndPlotting.
    */
	
	public DataManagement() throws IOException {
		// Loads closing prices using the static methods of DataCollectionAndPlotting
		pricesAsset1 = DataCollectionAndPlotting.getHistoricalPricesStock1();
		pricesAsset2 = DataCollectionAndPlotting.getHistoricalPricesStock2();
		// Loads dates (shared by both assets, read from Asset1.xlsx)
		dates = DataCollectionAndPlotting.getDates();
	}
	
	/*
	We compute portfolio returns as:
	portfolioReturn(t) = budget1 * absReturn1(t) + budget2 * absReturn2(t)
	
	where absReturn_i(t) = (price_i(t) / price_i(t-1))/price_i(t-1)
	
	@param budget1: capital to invest in asset 1
	@param budget2: capital to invest in asset 2
	@return array of daily portfolio returns
	*/
	
	public double[] getPortfolioReturns(double budget1, double budget2) {
		int n = pricesAsset1.length;
		
		double units1 = budget1 / pricesAsset1[0];
		double units2 = budget2 / pricesAsset2[0];

        // Portfolio returns array has length n-1 
        double[] portfolioReturns = new double[n - 1];

        for (int t = 1; t < n; t++) {
        	// Here we compute daily absolute returns for each asset: (P_t - P_{t-1}) / P_{t-1}
        	double absReturn1 = pricesAsset1[t] - pricesAsset1[t-1];
        	double absReturn2 = pricesAsset2[t] - pricesAsset2[t-1];
        	// Here we compute portfolio returns
        	portfolioReturns[t-1] = units1 * absReturn1 + units2 * absReturn2;
        }    
            
		return portfolioReturns;
	}
	
	/* We compute the log-returns of the assets: log(P_t / P_{t-1})
	 * Used as input for the Monte Carlo simulation, which assumes
	 * that the assets' logarithmic returns are normally distributed and independent.
	 */
	public double[] getLogReturnsAsset1() {
	    int n = pricesAsset1.length;
	    double[] logReturns = new double[n - 1];
	    for (int t = 1; t < n; t++) {
	        logReturns[t - 1] = Math.log(pricesAsset1[t] / pricesAsset1[t - 1]);
	    }
	    return logReturns;
	}

	public double[] getLogReturnsAsset2() {
	    int n = pricesAsset2.length;
	    double[] logReturns = new double[n - 1];
	    for (int t = 1; t < n; t++) {
	        logReturns[t - 1] = Math.log(pricesAsset2[t] / pricesAsset2[t - 1]);
	    }
	    return logReturns;
	}

	/* We compute the portfolio log-returns as a weighted sum of the log-returns of the two assets:
	 * portfolioLogReturn(t) = w1 * logReturn1[t] + w2 * logReturn2[t]
	 * 
	 * These are passed to MonteCarloSimulation, which estimates mu and sigma from this data
	 */
	public double[] getSimulatedPortfolioReturns(double budget1, double budget2) {
		double totalBudget = budget1 + budget2;
	    double w1 = budget1 / totalBudget;
	    double w2 = budget2 / totalBudget;
		
		double[] logReturn1 = getLogReturnsAsset1();
	    double[] logReturn2 = getLogReturnsAsset2();
	    int n = logReturn1.length;
	    double[] portfolioLogReturns = new double[n];
	    for (int t = 0; t < n; t++) {
	        portfolioLogReturns[t] = w1 * logReturn1[t] + w2 * logReturn2[t];
	    }
	    return portfolioLogReturns;
	}
	
	/*
	Here we produce plots.
	*/
	
	//Plots the price time series of asset 1.
	public void plotPricesAsset1() throws IOException {
		DataCollectionAndPlotting.plotData(dates, pricesAsset1, "Prices Asset 1");
	}
	
	//Plots the price time series of asset 2.
	public void plotPricesAsset2() throws IOException {
		DataCollectionAndPlotting.plotData(dates, pricesAsset2, "Prices Asset 2");
	}
	
	/*
     Computes portfolio returns, plots them, and returns the array.
     Dates start from index 1 because the first return requires two prices.
     */
	public double[] plotPortfolioReturns(double budget1, double budget2) throws IOException {
		double[] returns = getPortfolioReturns(budget1, budget2);
        LocalDate[] datesToPlot = Arrays.copyOfRange(dates, 1, dates.length);
		DataCollectionAndPlotting.plotData(datesToPlot, returns, "Portfolio Returns");
		return returns;
	}
	
	// Getters (useful for RiskMeasures and Tests)
	
	// Returns the full array of dates.
    public LocalDate[] getDates() {
        return dates;
    }

    // Returns closing prices of asset 1.
    public double[] getPricesAsset1() {
        return pricesAsset1;
    }

    // Returns closing prices of asset 2.
    public double[] getPricesAsset2() {
        return pricesAsset2;
    }

}
