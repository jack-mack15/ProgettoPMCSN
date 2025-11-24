package pmcsn.centers;

import pmcsn.controllers.NextEventScheduler;
import pmcsn.entities.Job;
import pmcsn.estimators.PopulationEstimator;
import pmcsn.estimators.Statistics;
import pmcsn.events.Event;
import pmcsn.events.EventType;
import pmcsn.rngs.Exponential;

import java.util.ArrayList;
import java.util.Comparator;

import static java.lang.System.out;

public class NodeP extends AbstractNode{
    private double serviceRate;

    //lista dei job attualmente in servizio
    private ArrayList<Job> jobsInService;
    private double lastUpdate;

    public NodeP(int serverNumber, NextEventScheduler scheduler) {
        super("P", serverNumber, scheduler);
        serviceRate = 0.4;
        jobsInService = new ArrayList<>();
        lastUpdate = 0.0;
    }

    @Override
    public void handleArrival(Event e) {
        out.println("NODO P: arrival ad istante: "+e.getTime());

        //stream del generatore
        int streamSelection = 4;
        //la classe uscente sarà sempre 1
        int nextJobClass = 2;

        //ottenere il tempo di esecuzione del job appena arrivato
        scheduler.getRng().selectStream(streamSelection);
        double serviceTime = Exponential.exponential(serviceRate,scheduler.getRng());
        //creazione job e inserimento nella giusta lista
        lastJobId++;
        Job newJob = new Job("P", lastJobId, scheduler.getClock(),nextJobClass, serviceTime);
        PopulationEstimator.getInstance().updatePopulationOnArrival(newJob);

        out.println("NODO P: job creato ad istante: "+scheduler.getClock()+", con service time: "+serviceTime+ " e rst: "+newJob.getRemainingServiceTime());

        updateRemainingServiceTime(scheduler.getClock());

        jobsInService.add(newJob);

        boolean checkDep = checkTerminateJobAndDeparture();
        if (!checkDep) {
            //creo il nuovo evento departure
            scheduleNextDeparture();
        }

        //TODO rimuovere questa stampa di debug
        //debugPrint();
        out.println("\n\n");

    }

    @Override
    public void handleDeparture(Event e) {
        out.println("NODO P: departure ad istante: "+e.getTime());

        //aggiorno i tempi di servizio rimanenti, in base al clock attuale
        updateRemainingServiceTime(scheduler.getClock());

        //verifico se ci sono job completati
        boolean checkDep = checkTerminateJobAndDeparture();

        if (!checkDep) {
            //qui devo aggiungere il prossimo evento departure
            scheduleNextDeparture();
        }

        //TODO rimuovere questa stampa di debug
        //debugPrint();
        out.println("\n\n");
    }

    //il valore di ritorno indica se è stato schedulato la prossima departure
    private boolean checkTerminateJobAndDeparture() {

        //controllo se ci sono job da rimuovere
        ArrayList<Job> toRemove = new ArrayList<>();
        for (Job j: jobsInService) {
            double temp = j.getRemainingServiceTime();
            if (temp <= j.getEpsilon()) {
                toRemove.add(j);
                j.setCompleteTime(scheduler.getClock());
                Statistics.getInstance().updateEstimators(j);
                PopulationEstimator.getInstance().updatePopulationOnDeparture(j);
                sendJobToServer();
            }
        }
        if (!toRemove.isEmpty()) {
            for (Job j: toRemove) {
                //rimozione del job completato
                jobsInService.remove(j);
            }
            scheduleNextDeparture();
            return true;
        }
        return false;
    }

    private void scheduleNextDeparture() {
        //ordino i job in servizio in base al tempo di esecuzioe rimanente
        sortJobsInService();

        //elimino eventuali eventi departure già presenti
        scheduler.getAndRemoveDepartureEvent("P");

        //prendo il tempo di primo completamento tra i job in servizio
        double firstToComplete = getFirstToComplete();
        if (firstToComplete == -1.0) {
            // se non ci sono più job in esecuzione evito dei loop
            return;
        }
        double firstDeparture = firstToComplete * jobsInService.size();
        firstDeparture += scheduler.getClock();

        //generare evento departure per il job
        Event departureForThis = new Event(firstDeparture, EventType.DEPARTURE,"P",2);
        //aggiunta evento departure alla coda dello scheduler
        scheduler.addEvent(departureForThis);

        out.println("NODO P: creato evento departure ad istante:"+departureForThis.getTime());
    }

    private void sendJobToServer() {
        Event e = new Event(scheduler.getClock(), EventType.ARRIVAL,"A",2);
        scheduler.addEvent(e);
    }

    private void updateRemainingServiceTime(double now) {
        double elapsed = Math.max(0.0, now - lastUpdate);
        double share = elapsed / jobsInService.size();
        for (Job j: jobsInService) {
            j.setRemainingServiceTime(share);
        }
        lastUpdate = now;

    }

    private void sortJobsInService() {
        jobsInService.sort(Comparator.comparingDouble(Job::getRemainServiceTime));
    }

    private double getFirstToComplete() {
        if (jobsInService.isEmpty()) {
            return -1.0;
        }
        return jobsInService.get(0).getRemainServiceTime();
    }

    //METODO PER DEBUG
    private void debugPrint() {
        for (Job j: jobsInService) {
            out.println("DEBUG NODO P: job id: "+j.getId()+", rst: "+j.getRemainingServiceTime());
        }
    }
}
