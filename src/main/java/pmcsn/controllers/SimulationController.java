package pmcsn.controllers;

import pmcsn.estimators.BatchMeansEstimator;
import pmcsn.estimators.CopyEstimator;
import pmcsn.estimators.Statistics;
import pmcsn.files.BatchResultsFileGenerator;
import pmcsn.files.OutputFileGenerator;

import java.util.Scanner;

import static java.lang.System.exit;
import static java.lang.System.out;

public class SimulationController {

    public static void main(String[] args) {

        //PARAMETRI DEL SISTEMA
        boolean isScaling = true;       //indica se il nodo B esegue scaling oppure no
        boolean isF2A = true;
        int maxBcopies = 2;//Integer.MAX_VALUE;
        int maxJobsStart = 3;

        //PARAMETRI DELLE RUN
        int numRuns = 1;//numero perfetto da 0.45 a 1.20
        int batchSize = 4096;
        int numBatches = 200; //valore usato era 244
        int numJobs = 819200;//409600;
        double maxTime = 0.0;
        long initialSeed = 123456789L;
        int firstIndexSeed = 72;//88;//72;
        double lambda = 1.2;
        //0 per transitorio
        //1 per batch means
        //0 per acs ma varname == ""
        //2 per le simulations di scaling
        int simulationType = 2;
        double samplingPeriod = 0.0;
        double samplingStart = 2500.0;

        boolean discard = true;

        //se servisse
        BatchMeansCalculator bmControl = new BatchMeansCalculator();

        //scheduler
        NextEventScheduler scheduler = new NextEventScheduler();
        scheduler.setSamplingPeriodAndStart(samplingPeriod,samplingStart);
        scheduler.initRngs(initialSeed,true);
        scheduler.setCursor(firstIndexSeed);

        //set del Batch means
        if (simulationType == 1 || simulationType == 2) {
            bmControl.setFile();
        }


        //csv generator
        OutputFileGenerator csvTransitorio = OutputFileGenerator.getInstance();
        //csvTransitorio.setFile("Batch.csv",simulationType);
        //csvTransitorio.setFile("ACS.txt",simulationType);


        for (int i = 0; i < numRuns; i++) {

            if(simulationType == 1 || simulationType == 2) {
                csvTransitorio.setFile("Batch.csv", simulationType);
            } else {
                csvTransitorio.setFile("transitorio.csv",simulationType);
            }

            //init dello scheduler
            scheduler.setMaxNumOfJobs(numJobs);
            scheduler.setMaxTime(maxTime);

            if(simulationType == 1) {
                //per f2a e per base
                BatchMeansEstimator.getInstance().setBatches(batchSize,numBatches);
                lambda += 0.05;
                scheduler.initArrival(1.0/lambda);
                scheduler.initSystem(isScaling,isF2A,maxBcopies,0);
            } else if (simulationType == 2) {
                //per scaling
                out.println("lambda is "+lambda);
                BatchMeansEstimator.getInstance().setBatches(batchSize,numBatches);
                scheduler.initArrival(1.0/lambda);
                scheduler.initSystem(isScaling,isF2A,maxBcopies,maxJobsStart+i);
                out.println("AVVIO RUN SCALING CON c="+(maxJobsStart+i));
            }else {
                //base
                scheduler.initArrival(1.0 / lambda);
                scheduler.initSystem(isScaling,isF2A,maxBcopies,0);
            }

            if (simulationType == 2) {
                Statistics.getInstance().setType(1);
            } else {
                Statistics.getInstance().setType(simulationType);
            }

            //inizio run
            double stopTime = scheduler.runSimulation();
            Statistics.getInstance().outputStatistics(stopTime);

            //reset per la prossima run
            scheduler.resetScheduler();
            Statistics.getInstance().resetStatistics();
            CopyEstimator.getInstance().resetCopyEstimator();

            scheduler.initRngs(initialSeed,true);

            OutputFileGenerator.getInstance().flushFiles();
            //se serve
            if(simulationType == 1) {
                bmControl.calculate(lambda);
            } else if (simulationType == 2 && i == 0 && discard) {
                i--;
                discard = false;
            }else if (simulationType == 2) {
                //lambda viene usato come numero di massimi jobs per copia di b
                bmControl.calculate(maxJobsStart+i);
            }
            //if (i == 0) {
            //    exit(-1);
            //}

            csvTransitorio.closeFile();
            csvTransitorio.deleteBatch();
        }

        BatchResultsFileGenerator.getInstance().closeFile();
    }


}
