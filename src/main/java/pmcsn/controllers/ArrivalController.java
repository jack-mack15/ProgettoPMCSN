package pmcsn.controllers;

import pmcsn.events.Event;
import pmcsn.events.EventType;
import pmcsn.rngs.Exponential;

public class ArrivalController {

    //se isActive == true, continuo a generare arrivi esterni
    private boolean isActive;
    private double arrivalRate;
    private long numArrivals;

    private NextEventScheduler scheduler;

    //costruttore classe
    public ArrivalController(double arrivalRate, NextEventScheduler sched){
        //init delle sue variabili
        this.isActive = true;
        this.arrivalRate = arrivalRate;
        this.scheduler = sched;
        this.numArrivals = 0;
        //schedulazione primo job
        generateExtArrival();
    }

    //metodo che genera un arrivo da esterno. Viene invocata ogni qual volta che si gestisce un arrivo da esterno
    public void generateExtArrival(){
        double arrivalTime = scheduler.getClock();
        if (numArrivals == 0) {
            //caso di inizio simulazione e coda vuota
            Event e = new Event(0.0, EventType.ARRIVAL, "A",-1);
            scheduler.addEvent(e);
            numArrivals++;
        } else {
            scheduler.getRng().selectStream(0);
            arrivalTime += Exponential.exponential(arrivalRate, scheduler.getRng());

            Event e = new Event(arrivalTime, EventType.ARRIVAL, "A", -1);

            scheduler.addEvent(e);
            numArrivals++;
        }
    }

    public long getNumArrivals() {
        return numArrivals;
    }
}
