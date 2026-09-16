package it.univr.riskmanagement;

import java.time.LocalDate;
import java.util.Arrays;
import org.apache.commons.math3.distribution.NormalDistribution;
 
/*
 * This class computes Monte Carlo estimators of VaR and ES.
 *
 * Definition (from lecture notes):
 *   VaR_alpha^MC(z^1,...,z^m) := VaR_alpha^hist(x_1^sim,...,x_s^sim)
 *   ES_alpha^MC(z^1,...,z^m)  := ES_alpha^hist(x_1^sim,...,x_s^sim)
 *
 * where:
 *   - z^1,...,z^m are the past observations of the underlying risk factors
 *     (the rolling window of portfolio returns)
 *   - x_1^sim,...,x_s^sim are s simulated portfolio returns drawn from
 *     N(mu, sigma^2), with plug-in estimators
 *   - the last step applies the historical VaR/ES to the simulated sample
 *     (semi-parametric approach: all the simulations were produced using parametric distributions,
 *     but in the last step we compute historical VaR/ES which are non-parametric estimators but applied to simulated data)
 *
 * This is consistent with the law of large numbers: as s goes to infinity,
 * the Monte Carlo estimator converges to the true VaR/ES under N(mu, sigma^2).
 * 
 * @author Stranger Risks Group
 */

public class MonteCarloSimulation {
	
	/*
     * We compute Monte Carlo VaR for a single time window.
     *
     * @param data:           rolling window of portfolio returns
     * @param alpha:          confidence level in (0,1)
     * @param numSimulations: number of Monte Carlo draws s
     * @return Monte Carlo VaR estimate
     */
	public static double computeMonteCarloVaR(double[] logReturnsWindow, double alpha, int numSimulations) {
	    int windowLength = logReturnsWindow.length;

	    // Step 1: plug-in estimates of mu and sigma
	    double mu = 0.0;
	    for (int i = 0; i < windowLength; i++) mu += logReturnsWindow[i];
	    mu /= windowLength;

	    double sigma = 0.0;
	    for (int i = 0; i < windowLength; i++) sigma += (logReturnsWindow[i] - mu) * (logReturnsWindow[i] - mu);
	    sigma = Math.sqrt(sigma / (windowLength - 1));
	    
	    // Step 2: simulate s = numSimulations draws from N(mu, sigma^2)
        NormalDistribution normal = new NormalDistribution(mu, sigma);
        double[] simulatedLogReturns = normal.sample(numSimulations);
       
        // Step 3: shift from logarithmic to absolute (simple) returns: r_abs = exp(r_log) - 1
        double[] simulatedAbsReturns = new double[numSimulations];
        for (int i = 0; i < numSimulations; i++) {
            simulatedAbsReturns[i] = Math.exp(simulatedLogReturns[i]) - 1;
        }
 
        // Step 4: apply historical VaR to the simulated sample x_1^sim,...,x_s^sim
        return RiskMeasures.computeHistoricalVaR(simulatedAbsReturns, alpha);
	}
	
	/*
     * We compute Monte Carlo ES for a single time window.
     *
     * @param data:           rolling window of portfolio returns
     * @param alpha:          confidence level in (0,1)
     * @param numSimulations: number of Monte Carlo draws s
     * @return Monte Carlo ES estimate
     */
    public static double computeMonteCarloES(double[] logReturnsWindow, double alpha, int numSimulations) {
        int windowLength = logReturnsWindow.length;
 
        // Step 1: plug-in estimates of mu and sigma
        double mu = 0.0;
        for (int i = 0; i < windowLength; i++) mu += logReturnsWindow[i];
        mu /= windowLength;
 
        double sigma = 0.0;
        for (int i = 0; i < windowLength; i++) sigma += (logReturnsWindow[i] - mu) * (logReturnsWindow[i] - mu);
        sigma = Math.sqrt(sigma / (windowLength - 1));
 
        // Step 2: simulate s draws from N(mu, sigma^2)
        NormalDistribution normal = new NormalDistribution(mu, sigma);
        double[] simulatedLogReturns = normal.sample(numSimulations);
 
        // Step 3: shift from logarithmic to absolute (simple) returns: r_abs = exp(r_log) - 1
        double[] simulatedAbsReturns = new double[numSimulations];
        for (int i = 0; i < numSimulations; i++) {
            simulatedAbsReturns[i] = Math.exp(simulatedLogReturns[i]) - 1;
        }
        
        // Step 4: apply historical ES to the simulated sample
        return RiskMeasures.computeHistoricalES(simulatedAbsReturns, alpha);
    }
    
    /*
     * We iterate Monte Carlo VaR along a rolling time window.
     *
     * @param data:           full time series of portfolio returns
     * @param alpha:          confidence level in (0,1)
     * @param windowLength:   estimation sample size n
     * @param numSimulations: number of Monte Carlo draws s per window
     * @return array of Monte Carlo VaR estimates 
     */
    public static double[] iterateMonteCarloVaR(double[] data, double alpha, int windowLength, int numSimulations) throws IllegalArgumentException {
        if (windowLength >= data.length)
            throw new IllegalArgumentException("Window length must be smaller than data length.");
 
        int numEstimates = data.length - windowLength;
        double[] VaRSeries = new double[numEstimates];
 
        for (int t = 0; t < numEstimates; t++) {
            double[] window = Arrays.copyOfRange(data, t, t + windowLength);
            VaRSeries[t] = computeMonteCarloVaR(window, alpha, numSimulations);
        }
        return VaRSeries;
    }
 
    /*
     * We iterate Monte Carlo ES along a rolling time window.
     *
     * @param data:           full time series of portfolio returns
     * @param alpha:          confidence level in (0,1)
     * @param windowLength:   estimation sample size n
     * @param numSimulations: number of Monte Carlo draws s per window
     * @return array of Monte Carlo ES estimates 
     */
    public static double[] iterateMonteCarloES(double[] data, double alpha, int windowLength, int numSimulations) throws IllegalArgumentException {
        if (windowLength >= data.length)
            throw new IllegalArgumentException("Window length must be smaller than data length.");
 
        int numEstimates = data.length - windowLength;
        double[] ESSeries = new double[numEstimates];
 
        for (int t = 0; t < numEstimates; t++) {
            double[] window = Arrays.copyOfRange(data, t, t + windowLength);
            ESSeries[t] = computeMonteCarloES(window, alpha, numSimulations);
        }
        return ESSeries;
    }
 
    /*
     * We produce plot of Monte Carlo VaR time series.
     *
     * @param dates:          full date array (same length as data)
     * @param data:           full time series of portfolio returns
     * @param alpha:          confidence level in (0,1)
     * @param windowLength:   estimation sample size n
     * @param numSimulations: number of Monte Carlo draws s per window
     */
    public static void plotIterateMonteCarloVaR(LocalDate[] dates, double[] data, double alpha, int windowLength, int numSimulations)
            throws IllegalArgumentException {
        double[] VaRSeries = iterateMonteCarloVaR(data, alpha, windowLength, numSimulations);
        // Dates start from day n+1 (drop the first windowLength dates)
        LocalDate[] rollingDates = Arrays.copyOfRange(dates, windowLength, dates.length);
        DataCollectionAndPlotting.plotData(rollingDates, VaRSeries, "Monte Carlo VaR");
    }
 
    /*
     * We produce plot of Monte Carlo ES time series.
     *
     * @param dates:          full date array (same length as data)
     * @param data:           full time series of portfolio returns
     * @param alpha:          confidence level in (0,1)
     * @param windowLength:   estimation sample size n
     * @param numSimulations: number of Monte Carlo draws s per window
     */
    public static void plotIterateMonteCarloES(LocalDate[] dates, double[] data, double alpha,
                                                int windowLength, int numSimulations)
            throws IllegalArgumentException {
        double[] ESSeries = iterateMonteCarloES(data, alpha, windowLength, numSimulations);
        // Dates start from day n+1 (drop the first windowLength dates)
        LocalDate[] rollingDates = Arrays.copyOfRange(dates, windowLength, dates.length);
        DataCollectionAndPlotting.plotData(rollingDates, ESSeries, "Monte Carlo ES");
    }
}

