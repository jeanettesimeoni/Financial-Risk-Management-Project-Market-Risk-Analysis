package it.univr.riskmanagement;


import java.time.LocalDate;
import java.util.Arrays;
import org.apache.commons.math3.distribution.NormalDistribution;

/*
This class contains the risk measures of interest

NOTE: we work under the profit convention

@author Stranger Risks Group
*/

public class RiskMeasures {

	/*
	 * Here we define historical VaR for a single time window.
	 */
	public static double computeHistoricalVaR(double[] data, double alpha) {
		double[] sorted = Arrays.copyOf(data, data.length);
		
		/* Sorts the returns in increasing order: x_{1:n}, x_{2:n}, ..., x_{n:n}
		 * x_{1:n} is the smallest observation (worst realization), x_{n:n} is the largest (best realization)
		 */
	    Arrays.sort(sorted); 
	    int windowLength = sorted.length; // n = windowLength
	    int index = (int) Math.floor(windowLength * alpha);
	    
		return -sorted[index]; //formula of historical VaR: -x_{floor(n*alpha)+1:n}
	}

	/*
	 * Here we define historical ES.
	 * 
	 */
	public static double computeHistoricalES(double[] data, double alpha) {
		double[] sorted = Arrays.copyOf(data, data.length);
	    Arrays.sort(sorted); // order statistics: x_{1:n}, x_{2:n}, ..., x_{n:n}
	    int windowLength = sorted.length;
	    int index = (int) Math.floor(windowLength * alpha); // floor(n*alpha)
	    double sum = 0.0;
	    for (int i = 0; i < index; i++) {
	        sum += sorted[i];
	    }
	    
	    /* We split the formula of hist ES into first term and second term,
	     * in order to make the computation easier to understand
	     */
	    
	    // First term: -1/(n*alpha) * sum_{i=1}^{floor(n*alpha)} x_{i:n}
	    double firstTerm = -1.0 / (windowLength * alpha) * sum;
	    double correction = alpha - (double) index / windowLength; // alpha - floor(n*alpha)/n
	    // Second term: -1/alpha * (alpha - floor(n*alpha)/n) * x_{floor(n*alpha)+1:n}
	    double secondTerm = -1.0 / alpha * correction * sorted[index];

		return firstTerm + secondTerm; //formula of historical ES: -1/(n*alpha) * sum_{i=1}^{floor(n*alpha)} x_{i:n} - 1/alpha * (alpha - floor(n*alpha)/n) * x_{floor(n*alpha)+1:n}
	}

	/*
	 * We iterate the calculation of historical VaR and ES along a rolling time window.
	 * @param windowLength: length of the time window corresponding to the estimation sample
	 */
	public static double[] iterateHistoricalVaR(double[] data, double alpha, int windowLength) throws  IllegalArgumentException{
		if (windowLength >= data.length) {
	        throw new IllegalArgumentException("Window length must be smaller than data length.");
	    }
		
		// The number of estimates goes from day n+1 to day N
	    int numEstimates = data.length - windowLength;
	    double[] VaRSeries = new double[numEstimates];
	    
	    /* We apply a rolling window of length windowLength to estimate historical VaR daily from day n+1 to day N
	     * t=0: window = [0,   1,   2,   ... 249]  
	     * t=1: window = [1,   2,   3,   ... 250]  
	     * t=2: window = [2,   3,   4,   ... 251]
	     * ...
	     * 
	     * NOTE: the rolling window applies uniform weights: 
	     * each observation contributes equally with weight 1/n
	     */
	    		
	    for (int t = 0; t < numEstimates; t++) {
	        // Extracts the rolling window [t, t + windowLength - 1]
	        double[] window = Arrays.copyOfRange(data, t, t + windowLength);
	        // Computes historical VaR on the current time window and store the estimate
	        VaRSeries[t] = computeHistoricalVaR(window, alpha);
	    }
	    
	    // Returns the full time series of historical VaR estimates, from day n+1 to day N
        return VaRSeries;
	}		
	
	public static double[] iterateHistoricalES(double[] data, double alpha, int windowLength) throws  IllegalArgumentException{
		// Ensures that the window length is strictly smaller than the total number of observations
		if (windowLength >= data.length) {
	        throw new IllegalArgumentException("Window length must be smaller than data length.");
	    }
	    
	    int numEstimates = data.length - windowLength;
	    double[] ESSeries = new double[numEstimates];
	    
	    for (int t = 0; t < numEstimates; t++) {
	        double[] window = Arrays.copyOfRange(data, t, t + windowLength);
	        ESSeries[t] = computeHistoricalES(window, alpha);
	    }
		
	    // Returns the full time series of historical ES estimates, from day n+1 to day N
        return ESSeries;
	}
	
	/*
	 * Here we produce plots of historical VaR and ES.
	 */
	public static void plotIterateHistoricalVaR(LocalDate[] dates, double[] data, double alpha, int windowLength) throws  IllegalArgumentException{
		// Computes the time series of historical VaR using the rolling window
	    double[] VaRSeries = iterateHistoricalVaR(data, alpha, windowLength);
	    
	    // Dates start from day n+1 (drop the first windowLength dates)
	    LocalDate[] rollingDates = Arrays.copyOfRange(dates, windowLength, dates.length);
	    
	    // Plots the time series of historical VaR
	    DataCollectionAndPlotting.plotData(rollingDates, VaRSeries, "Historical VaR");
	}
	
	public static void plotIterateHistoricalES(LocalDate[] dates, double[] data, double alpha, int windowLength) throws  IllegalArgumentException{
		// Computes the time series of historical ES using the rolling window
	    double[] ESSeries = iterateHistoricalES(data, alpha, windowLength);
	    
	    // Dates start from day n+1 (drop the first windowLength dates)
	    LocalDate[] rollingDates = Arrays.copyOfRange(dates, windowLength, dates.length);
	    
	    // Plots the time series of historical ES
	    DataCollectionAndPlotting.plotData(rollingDates, ESSeries, "Historical ES");
	}
	
	/* Here we define normal VaR for a single time window.
	 * Using plug-in estimator: mu and sigma are estimated using historical data.
	 */
	public static double computeNormalVaR(double[] data, double alpha) {
	    int windowLength = data.length; // n = windowLength
	    // Estimates mu (sample mean) from historical data: plug-in estimator
	    double mu = 0.0;
	    for (int i = 0; i < windowLength; i++) {
	        mu += data[i]; // accumulate the sum: x_1 + x_2 + ... + x_n
	    }
	    mu /= windowLength; // divide by n to obtain the sample mean
	    
	    // Estimates sigma from historical data: plug-in estimator
	    double sigma = 0.0;
	    for (int i = 0; i < windowLength; i++) {
	        sigma += (data[i] - mu) * (data[i] - mu); // accumulate: Σ(x_i - μ)²
	    }
	    sigma = Math.sqrt(sigma / (windowLength - 1)); // divide by (n-1) and take square root to obtain sample standard deviation
	    
	    // Phi^{-1}(alpha): alpha-quantile of the standard normal
	    NormalDistribution standardNormal = new NormalDistribution(0, 1);
	    double quantile = standardNormal.inverseCumulativeProbability(alpha); // Phi^{-1}(alpha)

	    // Formula: VaR_alpha(X) = -mu - Phi^{-1}(alpha) * sigma
	    return -mu - quantile * sigma;
	}
	
	/*
	 * Here we define normal ES for a single time window.
	 * Using plug-in estimator: mu and sigma are estimated from historical data.
	 */
	public static double computeNormalES(double[] data, double alpha) {
	    int windowLength = data.length; // n = windowLength = 250

	    // Estimate mu (sample mean) from historical data: plug-in estimator
	    double mu = 0.0;
	    for (int i = 0; i < windowLength; i++) {
	        mu += data[i];
	    }
	    mu /= windowLength;
	    
	    // Estimates sigma (sample standard deviation) from historical data: plug-in estimator
	    double sigma = 0.0;
	    for (int i = 0; i < windowLength; i++) {
	        sigma += (data[i] - mu) * (data[i] - mu);
	    }
	    sigma = Math.sqrt(sigma / (windowLength - 1)); // sample standard deviation: 1/(n-1)
	    
	    // Computes Phi^{-1}(alpha): alpha-quantile of the standard normal
	    NormalDistribution standardNormal = new NormalDistribution(0, 1); // N(0,1)
	    double quantile = standardNormal.inverseCumulativeProbability(alpha); // Phi^{-1}(alpha)

	    // Phi'(Phi^{-1}(alpha)): density of the standard normal evaluated at the quantile
	    double phiDensity = standardNormal.density(quantile); // Phi'(Phi^{-1}(alpha))

	    // Formula: ES_alpha(X) = -mu + Phi'(Phi^{-1}(alpha)) / alpha * sigma
	    return -mu + (phiDensity / alpha) * sigma;
	}
	
	/*
	 * Here we iterate the calculation of normal VaR and ES along a rolling time window.
	 * @param windowLength: length of the time window corresponding to the estimation sample
	 */
	public static double[] iterateNormalVaR(double[] data, double alpha, int windowLength) throws IllegalArgumentException {
	    if (windowLength >= data.length) {
	        throw new IllegalArgumentException("Window length must be smaller than data length.");
	    }

	    // The number of estimates goes from day n+1 to day N
	    int numEstimates = data.length - windowLength;
	    double[] VaRSeries = new double[numEstimates];

	    // We apply a rolling window of length windowLength to estimate normal VaR daily from day n+1 to day N
	    for (int t = 0; t < numEstimates; t++) {
	        // Extracts the rolling window [t, t + windowLength - 1]
	        double[] window = Arrays.copyOfRange(data, t, t + windowLength);
	        VaRSeries[t] = computeNormalVaR(window, alpha);
	    }

	    return VaRSeries;
	}
	
	public static double[] iterateNormalES(double[] data, double alpha, int windowLength) throws IllegalArgumentException {
	    if (windowLength >= data.length) {
	        throw new IllegalArgumentException("Window length must be smaller than data length.");
	    }

	    // The number of estimates goes from day n+1 to day N
	    int numEstimates = data.length - windowLength;
	    double[] ESSeries = new double[numEstimates];

	    // Apply a rolling window of length windowLength to estimate normal ES daily from day n+1 to day N
	    for (int t = 0; t < numEstimates; t++) {
	        // Extracts the rolling window [t, t + windowLength - 1]
	        double[] window = Arrays.copyOfRange(data, t, t + windowLength);
	        ESSeries[t] = computeNormalES(window, alpha);
	    }

	    return ESSeries;
	}
	
	/*
	Here we produce plots of normal VaR and ES.
	*/

	public static void plotIterateNormalVaR(LocalDate[] dates, double[] data, double alpha, int windowLength) throws IllegalArgumentException {
	    // Computes the time series of normal VaR using the rolling window
	    double[] VaRSeries = iterateNormalVaR(data, alpha, windowLength);

	    // Dates start from day n+1 (drop the first windowLength dates)
	    LocalDate[] rollingDates = Arrays.copyOfRange(dates, windowLength, dates.length);

	    // Plots the time series of normal VaR
	    DataCollectionAndPlotting.plotData(rollingDates, VaRSeries, "Normal VaR");
	}

	public static void plotIterateNormalES(LocalDate[] dates, double[] data, double alpha, int windowLength) throws IllegalArgumentException {
	    // Computes the time series of normal ES using the rolling window
	    double[] ESSeries = iterateNormalES(data, alpha, windowLength);

	    // Dates start from day n+1 (drop the first windowLength dates)
	    LocalDate[] rollingDates = Arrays.copyOfRange(dates, windowLength, dates.length);

	    // Plots the time series of normal ES
	    DataCollectionAndPlotting.plotData(rollingDates, ESSeries, "Normal ES");
	}
}
