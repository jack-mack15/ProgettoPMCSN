package pmcsn.controllers;

import pmcsn.estimators.BatchMeansEstimator;
import pmcsn.estimators.CopyEstimator;
import pmcsn.estimators.Statistics;
import pmcsn.files.OutputFileGenerator;

import static java.lang.System.out;

public class SimulationController {

    public static void main(String[] args) {

        //PARAMETRI DEL SISTEMA
        boolean isScaling = false;       //indica se il nodo B esegue scaling oppure no

        //PARAMETRI DELLE RUN
        int numRuns = 7;
        int batchSize = 4096;
        int numBatches = 100; //valore usato era 244
        int numJobs = 409600;
        long initialSeed = 123456789L;
        //long initialSeed = 3333L;
        double lambda = 1.2;
        //0 per transitorio
        //1 per batch means
        //0 per acs ma varname == ""
        int simulationType = 0;
        double samplingPeriod = 100.0;

        //scheduler
        NextEventScheduler scheduler = new NextEventScheduler();
        scheduler.setSamplingPeriod(samplingPeriod);

        //set del Batch means
        if (simulationType == 1) {
            BatchMeansEstimator.getInstance().setBatches(batchSize,numBatches);
        }


        //csv generator
        OutputFileGenerator csvTransitorio = OutputFileGenerator.getInstance();
        //csvTransitorio.setFile("Batch.csv",simulationType);
        //csvTransitorio.setFile("ACS.txt",simulationType);
        csvTransitorio.setFile("Transitorio.csv",simulationType);

        for (int i = 0; i < numRuns; i++) {

            //init dello scheduler
            scheduler.setMaxNumOfJobs(numJobs);
            scheduler.initRngs(initialSeed);

            scheduler.initArrival(1.0/lambda);
            scheduler.initSystem(isScaling);

            Statistics.getInstance().setType(simulationType);
            //inizio run
            double stopTime = scheduler.runSimulation();

            Statistics.getInstance().outputStatistics(stopTime);

            //reset per la prossima run
            scheduler.resetScheduler();
            Statistics.getInstance().resetStatistics();
            CopyEstimator.getInstance().resetCopyEstimator();

        }
        OutputFileGenerator.getInstance().closeFile();

    }


}
