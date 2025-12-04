package pmcsn.controllers;

import pmcsn.events.Event;
import pmcsn.events.EventType;
import pmcsn.rngs.Exponential;

public class ArrivalController {

    //se isActive == true, continuo a generare arrivi esterni
    private boolean isActive;
    private double arrivalRate;
    private long numArrivals;
    //questo sarà l'id unico che verrà assegnato ad eventi e job relativi alla stessa richiesta al sistema (job inteso come nel corso)
    private long idArrival;
    private NextEventScheduler scheduler;

    //costruttore classe
    public ArrivalController(double arrivalRate, NextEventScheduler sched){
        //init delle sue variabili
        this.isActive = true;
        this.arrivalRate = arrivalRate;
        this.scheduler = sched;
        this.numArrivals = 0;
        this.idArrival = 0L;
        //schedulazione primo job
        generateExtArrival();
    }


    //metodo che genera un arrivo da esterno. Viene invocata ogni qual volta che si gestisce un arrivo da esterno
    public void generateExtArrival(){
        double arrivalTime = scheduler.getClock();
        if (numArrivals == 0) {
            //caso di inizio simulazione e coda vuota
            Event e = new Event(0.0, EventType.ARRIVAL, "A",-1,idArrival);
            scheduler.addEvent(e);
            idArrival++;
            numArrivals++;
        } else {
            scheduler.getRng().selectStream(0);
            arrivalTime += Exponential.exponential(arrivalRate, scheduler.getRng());

            Event e = new Event(arrivalTime, EventType.ARRIVAL, "A", -1,idArrival);

            scheduler.addEvent(e);
            idArrival++;
            numArrivals++;
        }
    }

    public long getNumArrivals() {
        return numArrivals;
    }
}
