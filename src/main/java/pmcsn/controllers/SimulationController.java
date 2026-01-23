package pmcsn.controllers;

import pmcsn.estimators.BatchMeansEstimator;
import pmcsn.estimators.CopyEstimator;
import pmcsn.estimators.Statistics;
import pmcsn.files.BatchResultsFileGenerator;
import pmcsn.files.OutputFileGenerator;

import static java.lang.System.out;

public class SimulationController {

    public static void main(String[] args) {

        //PARAMETRI DEL SISTEMA
        boolean isScaling = false;       //indica se il nodo B esegue scaling oppure no
        boolean isF2A = true;
        int maxBcopies = Integer.MAX_VALUE;
        int maxJobsStart = 7;

        //PARAMETRI DELLE RUN
        int numRuns = 15;//numero perfetto da 0.45 a 1.20
        int batchSize = 4096;
        int numBatches = 200;
        int numJobs = 819200; //819200;//409600; //1000000 per scaling con perdita
        double maxTime = 0.0; //172800.0; //172800.0; //172800 per avere 48 h
        long initialSeed = 123456789L;
        boolean writeCopy = true;

        //seed 0 per transitorio (9*6 utilizzati)
        //seed 60 per ploss (solo 6 utilizzati)
        //seed 72 per esperimenti (14*6 utilizzati)
        int firstIndexSeed = 72;//88;//72;
        double lambda = 0.45;
        //0 per transitorio
        //1 per batch means
        //0 per acs ma varname == ""
        //2 per le simulations di scaling
        int simulationType = 1;
        double samplingPeriod = 0.0;
        double samplingStart = 250.0;
        boolean resetSeed = false;
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
        if (writeCopy) {
            bmControl.setFileCopy(maxJobsStart);
        }


        //csv generator
        OutputFileGenerator csvTransitorio = OutputFileGenerator.getInstance();
         if (simulationType == 0) {
            csvTransitorio.setFile("transitorio.csv",simulationType);
        }
        //csvTransitorio.setFile("Batch.csv",simulationType);
        //csvTransitorio.setFile("ACS.txt",simulationType);


        for (int i = 0; i < numRuns; i++) {

            if(simulationType == 1 || simulationType == 2) {
                csvTransitorio.setFile("Batch.csv", simulationType);
            }
            if (writeCopy) {
                OutputFileGenerator.getInstance().setFileCopy();
            }

            //init dello scheduler
            scheduler.setMaxNumOfJobs(numJobs);
            scheduler.setMaxTime(maxTime);

            if(simulationType == 1) {
                //per f2a e per base
                BatchMeansEstimator.getInstance().setBatches(batchSize,numBatches);
                lambda += 0.05;
                scheduler.initArrival(1.0/lambda);
                scheduler.initSystem(isScaling,isF2A,maxBcopies,0,simulationType);
            } else if (simulationType == 2) {
                //per scaling
                BatchMeansEstimator.getInstance().setBatches(batchSize,numBatches);
                scheduler.initArrival(1.0/lambda);
                scheduler.initSystem(isScaling,isF2A,maxBcopies,maxJobsStart+i,simulationType);
                out.println("AVVIO RUN SCALING CON c="+(maxJobsStart+i));
            }else {
                //base
                scheduler.initArrival(1.0 / lambda);
                scheduler.initSystem(isScaling,isF2A,maxBcopies,maxJobsStart,simulationType);
            }

            if (simulationType == 2) {
                Statistics.getInstance().setType(1);
            } else {
                Statistics.getInstance().setType(simulationType);
            }

            //AVVIO RUN
            double stopTime = scheduler.runSimulation();
            Statistics.getInstance().outputStatistics(stopTime);

            //reset per la prossima run
            scheduler.resetScheduler();
            Statistics.getInstance().resetStatistics();
            CopyEstimator.getInstance().hardReset();

            scheduler.initRngs(initialSeed,resetSeed);

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
                if (writeCopy) {
                    bmControl.calculateCopy(maxJobsStart+i);
                }
            }


            if (simulationType != 0) {
                csvTransitorio.closeFile();
                csvTransitorio.deleteBatch();
                if (writeCopy) {
                    csvTransitorio.deleteCopy();
                }
            }
        }

        BatchResultsFileGenerator.getInstance().closeFile();
    }


}
