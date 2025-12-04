package pmcsn.controllers;

import pmcsn.estimators.CopyEstimator;
import pmcsn.estimators.Statistics;
import pmcsn.estimators.Welford;

import static java.lang.System.*;

public class SimulationController {

    public static void main(String[] args) {

        out.println("Inizio RUNS!\n");

        int numRuns = 1000;
        int numBatch = 10;
        boolean isScaling = true;

        NextEventScheduler scheduler = new NextEventScheduler();
        double rt = 0.0;
        double pop = 0.0;
        double uA = 0.0;
        double uB = 0.0;
        double uP = 0.0;
        double rtA = 0.0;
        double rtB = 0.0;
        double rtP = 0.0;
        double copyNum = 0.0;
        double intervalEstimate = 0.0;
        Welford rtSystem = new Welford();
        Welford popSystem = new Welford();
        for (int i = 0; i < numRuns; i++) {

            //init dello scheduler
            //scheduler.setStopTime(86400.0);
            scheduler.setStopTime(10000.0);
            scheduler.initRngs(3333L+i);
            scheduler.initArrival(1.0/1.2);
            scheduler.initSystem(isScaling);

            //inizio run
            scheduler.runSimulation();

            //gestione statistiche
            //out.println("STAMPA DELLE STATISTICHE RUN NUMERO: "+i+"\n");
            //Statistics.getInstance().printStatistics();
            out.println("numero medio di copie medie che ci sono state: "+CopyEstimator.getInstance().getNumCopyMean());

            double temp = Statistics.getInstance().getResponseTimeMean("SYSTEM");
            double temp2 = Statistics.getInstance().getPopMean("SYSTEM");
            rt += temp;
            pop += temp2;
            rtSystem.addData(temp);
            popSystem.addData(temp2);

            out.println("***********************deviazione standard del tempo risposta sistema: "+Statistics.getInstance().getResponseTimeStandDev("SYSTEM"));
            uA += Statistics.getInstance().getUtilizationMean("A");
            uB += Statistics.getInstance().getUtilizationMean("B");
            uP += Statistics.getInstance().getUtilizationMean("P");
            rtA += Statistics.getInstance().getResponseTimeMean("A");
            rtB += Statistics.getInstance().getResponseTimeMean("B");
            rtP += Statistics.getInstance().getResponseTimeMean("P");
            copyNum += CopyEstimator.getInstance().getNumCopyMean();




            //reset per la prossima run
            scheduler.resetScheduler();
            Statistics.getInstance().resetStatistics();
            CopyEstimator.getInstance().resetCopyEstimator();
        }

        out.println("++++++++++++++++++++++++++++++++++++++++++\nmedia tempo risposta "+rtSystem.getMean());
        out.println("standard dev tempo risposta "+rtSystem.getStandardDeviation());
        out.println("media popolazione "+popSystem.getMean());
        out.println("standard dev popolazione "+popSystem.getStandardDeviation());

        out.println("\n\nutilization A "+(uA/numRuns));
        out.println("utilization B "+(uB/numRuns));
        out.println("utilization P "+(uP/numRuns));
        out.println("response time A "+(rtA/numRuns));
        out.println("response time B "+(rtB/numRuns));
        out.println("response time P "+(rtP/numRuns));
        out.println("numero copie medio: "+(copyNum/numRuns));
    }


}
