package pmcsn.controllers;

import pmcsn.centers.System;
import pmcsn.estimators.CopyEstimator;
import pmcsn.estimators.PopulationEstimator;
import pmcsn.events.Event;
import pmcsn.events.EventType;
import pmcsn.rngs.Rngs;

import java.util.PriorityQueue;

import static java.lang.System.out;

public class NextEventScheduler {

    //clock globale del sistema
    private double clock;

    //tempo massimo di simulazione
    private double stopTime;

    //coda globale che contiene tutti gli eventi
    //PriorityQueue in automatico ordina gli eventi sulla base del loro istante di arrivo
    private PriorityQueue<Event> eventList = new PriorityQueue<>();

    //istanza per rng
    private Rngs rng;

    //questo valore epsilon è necessario per evitare problemi di approssimazione, ad esempio
    //t1 + t2 = t3 con t1 = t3 poichè t2 troppo piccolo
    private final double epsilon = 1e-12;

    //istanza per ArrivalController
    private ArrivalController arrivalController;

    //istanza del sistema, contiene tutti i centri
    private System system;

    public void resetScheduler() {
        eventList = new PriorityQueue<>();
        this.clock = 0.0;
        stopTime = 0.0;
    }


    //METODI DI INIZIALIZZAZIONE
    //init della classe rngs
    public void initRngs(long seed) {
        this.rng = new Rngs();
        this.rng.plantSeeds(seed);
    }
    //init della classe ArrivalController
    public void initArrival(double arrivalRate) {
        this.arrivalController = new ArrivalController(arrivalRate,this);
    }
    //init dell'istanza del sistema, mantiene traccia dei nodi del sistema
    public void initSystem(boolean isScaling) {
        this.system = new System(this,isScaling);
    }

    //metodo che aggiunge un avento alla coda globale
    public void addEvent(Event e) {
        eventList.add(e);
    }

    //metodo per verificare se la coda è vuota
    //ritorna true se ci sono ancora eventi
    public boolean checkQueue() {
        return !this.eventList.isEmpty();
    }

    //metodo per ottenere il prossimo elemento della coda
    //ritorna il primo evento valido della coda
    public Event getNext() {

        if (checkQueue()) {
            Event e = this.eventList.poll();
            return e;
        }
        return null;
    }

    public int runSimulation() {
        this.clock = 0;
        Event e;


        //fase di simulazione
        while (!eventList.isEmpty() || clock <= stopTime) {
            e = getNext();
            if (e == null) {
                //la coda di eventi è vuota, errore
                out.println("errori con la coda degli eventi\n");
                return -1;
            }

            //evento valido
            //verifico che evento sia
            if (e.getType() == EventType.ARRIVAL) {

                //out.println("SCHED: arrival ad istante: "+e.getTime()+", per nodo: "+e.getNode()+" con classe: "+e.getClassId());
                setClock(e.getTime());

                if (e.getClassId() == -1) {
                    //il caso -1 riguarda arrivi dall'esterno e quindi richiede una generazione successiva di
                    //arrivo da esterno
                    if (clock <= stopTime) {
                        arrivalController.generateExtArrival();
                    }
                    system.handleArrival(e,"A");
                } else {
                    system.handleArrival(e,e.getNode());
                }

            } else if (e.getType() == EventType.DEPARTURE) {
                //out.println("SCHED: NODO "+e.getNode()+" departure at "+e.getTime());
                setClock(e.getTime());
                system.handleDeparture(e,e.getNode());
            } else if (e.getType() == EventType.CREATE) {
                //richiesta creazione copia di un server
                system.handleCopyCreation(e);
                setClock(e.getTime());
            } else if (e.getType() == EventType.DESTROY) {
                //richiesta distruzione copia di un server
                system.handleCopyDestroy(e);
                setClock(e.getTime());
            }


        }

        PopulationEstimator.getInstance().setFinishTime(clock);
        CopyEstimator.getInstance().setFinishTime(clock);
        out.println("\n\nin tutto ci sono stati questi arrivi: "+arrivalController.getNumArrivals()+"\n\n");
        out.println("in tutto ci sono state copie di B:"+system.getCopiesNum()+"\n\n");
        system.getCopiesNum();

        return 0;
    }

    //metodo che va a prendere un evento departure e lo rimuove se fosse presente
    //nella lista degli eventi c'è un solo departure event per nodo del sistema (ovvero il primo che termina del nodo)
    public Event getAndRemoveDepartureEvent(String nodeName) {
        PriorityQueue<Event> temp = new PriorityQueue<>(eventList);
        while (!temp.isEmpty()) {
            Event e = temp.poll();
            if(e.getType() == EventType.DEPARTURE && e.getNode() == nodeName) {
                eventList.remove(e);
                return e;
            }
        }
        return null;
    }

    public void setStopTime(double stopTime) {
        this.stopTime = stopTime;
    }
    public double getClock() {
        return this.clock;
    }
    public void setClock(double time) {
        this.clock = time;
        //out.
        // ("CLOCK: clock is now "+time+"\n");
    }
    public Rngs getRng() {
        return rng;
    }
}