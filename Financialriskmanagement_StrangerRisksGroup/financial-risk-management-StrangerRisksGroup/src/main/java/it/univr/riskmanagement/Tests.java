package it.univr.riskmanagement;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Arrays;

/*
The test class outputs all plots taking the model parameters as inputs. 

@author Stranger Risks Group
*/

public class Tests {

	public static void main(String[] args) throws IOException {
		DataManagement tester = new DataManagement();
		
		//plot prices of asset 1
		tester.plotPricesAsset1();
		//plot prices of asset 2
		tester.plotPricesAsset2();
		
		// We set a total budget of 20000, equally divided between the two assets 
		double budget1 = 10000; //50% of the budget in Asset 1
		double budget2 = 10000; //50% of the budget in Asset 2
		
		// We compute portfolio returns
		
		double[] returns = tester.getPortfolioReturns(budget1, budget2);
		// Monte Carlo uses log-returns for simulation
		double[] logReturns = tester.getSimulatedPortfolioReturns(budget1, budget2);

		int windowLength = 250;
		double alphaVaR = 0.01;
		double alphaES = 0.025;
		int numSimulations = 100000; // number of Monte Carlo simulations 
		
		// Gets dates from tester
        LocalDate[] dates = Arrays.copyOfRange(tester.getDates(), 1, tester.getDates().length);
        
        // Step 1: plot historical VaR and ES
        RiskMeasures.plotIterateHistoricalVaR(dates, returns, alphaVaR, windowLength);
        RiskMeasures.plotIterateHistoricalES(dates, returns, alphaES, windowLength);
        
        // Step 2: plot normal VaR and ES
        RiskMeasures.plotIterateNormalVaR(dates, returns, alphaVaR, windowLength);
        RiskMeasures.plotIterateNormalES(dates, returns, alphaES, windowLength);
        
        // Step 3: plot Monte Carlo VaR and ES
     	MonteCarloSimulation.plotIterateMonteCarloVaR(dates, logReturns, alphaVaR, windowLength, numSimulations);
     	MonteCarloSimulation.plotIterateMonteCarloES(dates, logReturns, alphaES, windowLength, numSimulations);
     	
	}
}