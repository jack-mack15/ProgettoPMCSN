package pmcsn.controllers;

import pmcsn.centers.System;
import pmcsn.estimators.CopyEstimator;
import pmcsn.estimators.PopulationEstimator;
import pmcsn.estimators.Statistics;
import pmcsn.events.Event;
import pmcsn.events.EventType;
import pmcsn.rngs.Rngs;
import java.util.PriorityQueue;

import static java.lang.System.out;

public class NextEventScheduler {

    //clock globale del sistema
    private double clock;

    //numero di job massimi delle run
    private long maxNumOfJobs;

    //numero di job attualmente arrivati e usciti dal sistema
    private long curNumOfJobs = 0;

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
    private double maxTime;
    private double samplingPeriod;
    private double samplingStart;

    public void resetScheduler() {
        eventList = new PriorityQueue<>();
        this.clock = 0.0;
        maxNumOfJobs = 0;
        curNumOfJobs = 0;
    }


    //METODI DI INIZIALIZZAZIONE
    //init della classe rngs e seeds di output
    public void initRngs(long seed, boolean reset) {
        if (reset) {
            this.rng = new Rngs(seed);
            Statistics.getInstance().setSeedsForOutput(rng.getSeeds());
        } else {
            this.rng.setRunCursor();
            Statistics.getInstance().setSeedsForOutput(rng.getSeeds());
        }
    }

    public void setCursor(int cursorIndex){
        rng.setRunCursorByIndex(cursorIndex);
        Statistics.getInstance().setSeedsForOutput(rng.getSeeds());
    }
    //init della classe ArrivalController
    public void initArrival(double arrivalRate) {
        this.arrivalController = new ArrivalController(arrivalRate,this);
    }
    //init dell'istanza del sistema, mantiene traccia dei nodi del sistema
    public void initSystem(boolean isScaling, boolean isF2A, int maxNumOfBCopies,int maxJobsForCopy, int simType) {
        this.system = new System(this,isScaling,isF2A,maxNumOfBCopies,maxJobsForCopy,simType);
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
            return this.eventList.poll();
        }
        return null;
    }

    //metodo che implementa la simulazione. ritorna l'ultimo valore di clock al termine della run.
    public double runSimulation() {
        this.clock = 0;
        Event e;

        //creazione primo evento di sampling
        if(samplingPeriod > 0.0) {
            Event firstSampl = new Event(samplingStart, EventType.SAMPLING, "", -1, -1);
            addEvent(firstSampl);
        }


        //fase di simulazione
        while (!eventList.isEmpty() && curNumOfJobs < maxNumOfJobs && clock <= maxTime) {
            e = getNext();
            if (e == null) {
                //la coda di eventi è vuota, errore
                out.println("errori con la coda degli eventi\n");
                return -1;
            }

            //out.println("sto viaggiando "+clock+"  "+e.getIdRequest()+e.getNode()+e.getType()+e.getTime());

            //evento valido
            //verifico che evento sia
            if (e.getType() == EventType.ARRIVAL) {
                setClock(e.getTime());
                if (e.getClassId() == -1) {
                    //il caso -1 riguarda arrivi dall'esterno e quindi richiede una generazione successiva di
                    //arrivo da esterno
                    arrivalController.generateExtArrival();
                    system.handleArrival(e,"A");
                } else {
                    system.handleArrival(e,e.getNode());
                }

            } else if (e.getType() == EventType.DEPARTURE) {
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
            } else if (e.getType() == EventType.SAMPLING) {
                Statistics.getInstance().onSampling(e.getTime());
                //creazione futuro sampling
                Event sampl = new Event(e.getTime()+samplingPeriod,EventType.SAMPLING,"",-1,-1);
                addEvent(sampl);
                setClock(e.getTime());
            }


        }

        PopulationEstimator.getInstance().setFinishTime(clock);
        CopyEstimator.getInstance().setFinishTime(clock);
        system.getCopiesNum();
        //stampe interessanti per debug
        out.println("media copie di B:"+CopyEstimator.getInstance().getNumCopyMean());
        out.println("ci sono stati "+arrivalController.getNumArrivals()+" jobs");
        out.println("final clock is "+clock);
        out.println("sono stati scartati "+system.getBDescardedJobs()+" jobs da B\n\n");

        return clock;
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

    //stessa cosa di prima ma rimuove eventi DEStroy senza ritornarlo
    public void removeDestroyEvent(String nodeName) {
        //il check viene già fatto dal nodo interessato
        PriorityQueue<Event> temp = new PriorityQueue<>(eventList);
        while (!temp.isEmpty()) {
            Event e = temp.poll();
            if(e.getType() == EventType.DESTROY && e.getNode() == nodeName) {
                eventList.remove(e);
                return;
            }
        }
    }

    //metodi di set e get utili
    public void setSamplingPeriodAndStart(double period,double start) {
        this.samplingStart = start;
        this.samplingPeriod = period;
    }

    public void setMaxNumOfJobs(long maxNumOfJobs) {
        this.maxNumOfJobs = maxNumOfJobs;
    }
    public void setMaxTime(double maxTime) {
        if (maxTime <= 0.0) {
            this.maxTime = Double.MAX_VALUE;
            return;
        }
        this.maxTime = maxTime;
    }

    public void incrementCurNumOfJobs() {
        this.curNumOfJobs += 1;
    }

    public double getClock() {
        return this.clock;
    }
    public void setClock(double time) {
        this.clock = time;
    }
    public Rngs getRng() {
        return rng;
    }
}