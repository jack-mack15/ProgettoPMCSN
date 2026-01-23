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
        boolean isF2A = true;           // indica quali tempi medi di servizio P e A devono usare
        int maxBcopies = Integer.MAX_VALUE; //2 per fase di verifica  //indica quante copie massime di b ci possono essere (sempre max)
        int maxJobsStart = 7;    //parametro C del report, numero massimo di job per copia di B

        //PARAMETRI DELLE RUN
        int numRuns = 15;       //numero perfetto per lambda da 0.5 a 1.20
        int batchSize = 4096;    //dimensione dei batch
        int numBatches = 200;       //numero dei barch
        int numJobs = 819200;       //numero di job massimi per la simulazione
        double maxTime = 0.0;    //172800 per avere 48 h  //tempo massimo di simulazione (se 0.0 uso solo job massimi)
        long initialSeed = 123456789L;      //seed da cui si calcolano tutti i seed
        boolean writeCopy = true;       //indica se è necessario scrivere il file delle copie

        //seed 0 per transitorio (9*6 utilizzati)
        //seed 60 per ploss (solo 6 utilizzati)
        //seed 72 per esperimenti (14*6 utilizzati)
        int firstIndexSeed = 72;        //indica l'indice nell'array degli stream.
        double lambda = 0.5;           //tasso di arrivi da esterno
        //0 per transitorio o per grafici temporali
        //1 per batch means
        //0 per acs ma varname == "" ma occorre avere il file pronto
        //2 per le simulations di scaling
        int simulationType = 1;
        double samplingPeriod = 0.0;        //indica ogni quanto fare il sampling temporale (se 0.0 mai)
        double samplingStart = 250.0;       //indica primo evento SAMPLING
        boolean resetSeed = false;          //indica se è necessario spostarsi di indice stream o no a fine run
                                            //se true, si resettano gli stream
        boolean discard = false;             //indica se scartare la prima run se problemi file


        //se serve il generatore del file batch finale
        BatchMeansCalculator bmControl = new BatchMeansCalculator();

        //scheduler e alcuni init
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
        OutputFileGenerator csvFile = OutputFileGenerator.getInstance();
         if (simulationType == 0) {
            csvFile.setFile("transitorio.csv",simulationType);
        }
        //csvFile.setFile("Batch.csv",simulationType);
        //csvFile.setFile("ACS.txt",simulationType);


        //for sul numero di run
        for (int i = 0; i < numRuns; i++) {

            if(simulationType == 1 || simulationType == 2) {
                csvFile.setFile("Batch.csv", simulationType);
            }
            if (writeCopy) {
                OutputFileGenerator.getInstance().setFileCopy();
            }

            //init dello scheduler
            scheduler.setMaxNumOfJobs(numJobs);
            scheduler.setMaxTime(maxTime);

            if(simulationType == 1) {
                //per f2a e per base con lambda variabile
                BatchMeansEstimator.getInstance().setBatches(batchSize,numBatches);
                lambda += (i * 0.05);
                scheduler.initArrival(1.0/lambda);
                scheduler.initSystem(isScaling,isF2A,maxBcopies,0,simulationType);
            } else if (simulationType == 2) {
                //per scaling
                BatchMeansEstimator.getInstance().setBatches(batchSize,numBatches);
                scheduler.initArrival(1.0/lambda);
                scheduler.initSystem(isScaling,isF2A,maxBcopies,maxJobsStart+i,simulationType);
                out.println("AVVIO RUN SCALING CON c="+(maxJobsStart+i));
            }else {
                //base o transitorio
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

            //imposto stoptime
            Statistics.getInstance().outputStatistics(stopTime);

            //reset per la prossima run
            scheduler.resetScheduler();
            Statistics.getInstance().resetStatistics();
            CopyEstimator.getInstance().hardReset();

            scheduler.initRngs(initialSeed,resetSeed);

            OutputFileGenerator.getInstance().flushFiles();

            //in base al tipo di simulazione mi servono dati differenti
            if(simulationType == 1) {
                bmControl.calculate(lambda);
            } else if (simulationType == 2 && i == 0 && discard) {
                //se necessario, si scarta la prima run per problemi di file
                //todo remove
                i--;
                discard = false;
            }else if (simulationType == 2) {
                //lambda viene usato come numero di massimi jobs per copia di b
                bmControl.calculate(maxJobsStart+i);
                if (writeCopy) {
                    //se necessario scrivo il file delle copie
                    bmControl.calculateCopy(maxJobsStart+i);
                }
            }


            //chiusura e rimozione file se necessito più run
            if (simulationType != 0) {
                csvFile.closeFile();
                csvFile.deleteBatch();
                if (writeCopy) {
                    csvFile.deleteCopy();
                }
            }
        }

        BatchResultsFileGenerator.getInstance().closeFile();
    }


}
