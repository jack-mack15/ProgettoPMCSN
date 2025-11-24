package pmcsn.controllers;

import pmcsn.estimators.Estimator;
import pmcsn.estimators.Statistics;

import static java.lang.System.*;

public class SimulationController {

    //valore iniziale del clock di simulazione
    private long startTime = 0;

    //flag che indica se la simulazione è attiva (true) o no (false)
    private boolean simulationStatus = true;

    public static void main(String[] args) {

        out.println("Inizio RUN!\n");

        //inizio run
        NextEventScheduler scheduler = new NextEventScheduler();
        //una giornata
        //scheduler.setStopTime(86400.0);
        scheduler.setStopTime(1500.0);
        scheduler.initRngs(233L);
        scheduler.initArrival(1.0/1.2);
        scheduler.initSystem();
        scheduler.runSimulation();
        Statistics.getInstance().printStatistics();
    }


}
