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

public class NodeB extends AbstractNode{

    private double serviceRate;

    //lista dei job attualmente in servizio
    private ArrayList<Job> jobsInService;
    private double lastUpdate;

    //indica se c'è un evento destroy in arrivo
    private boolean uponDestroy;

    public NodeB(NextEventScheduler scheduler, String newName) {
        super(newName, scheduler);
        serviceRate = 0.8;
        uponDestroy = false;
        jobsInService = new ArrayList<>();
        lastUpdate = 0.0;
    }

    @Override
    public void handleArrival(Event e) {

        //vedo se ho un evento destroy pending e in caso lo elimino
        if (uponDestroy) {
            //out.println(name+"sto tentando di rimuovere ad istante "+e.getTime());
            scheduler.removeDestroyEvent(name);
            uponDestroy = false;
        }

        //stream del generatore
        String streamSelection = "B";
        //la classe uscente sarà sempre 1
        int nextJobClass = 1;

        //ottenere il tempo di esecuzione del job appena arrivato
        scheduler.getRng().selectStream(streamSelection);
        double serviceTime = Exponential.exponential(serviceRate,scheduler.getRng());

        //creazione job e inserimento nella giusta lista
        Job newJob = new Job("B",e.getIdRequest(), scheduler.getClock(),nextJobClass, serviceTime);
        PopulationEstimator.getInstance().updatePopulationOnArrival(newJob);

        updateRemainingServiceTime(scheduler.getClock());

        jobsInService.add(newJob);

        boolean checkDep = checkTerminateJobAndDeparture();
        if (!checkDep) {
            //creo il nuovo evento departure
            scheduleNextDeparture();
        }

        //TODO rimuovere questa stampa di debug
        //debugPrint(e.getTime());
        //out.println("\n\n");

    }

    @Override
    public void handleDeparture(Event e) {
        //aggiorno i tempi di servizio rimanenti, in base al clock attuale
        updateRemainingServiceTime(scheduler.getClock());

        //verifico se ci sono job completati
        boolean checkDep = checkTerminateJobAndDeparture();

        if (!checkDep) {
            //qui devo aggiungere il prossimo evento departure
            //non dovrebbe mai succedere che entro qua dentro dopo le modifiche a checkTerminate...
            scheduleNextDeparture();
        }

        //TODO rimuovere questa stampa di debug
        //debugPrint(e.getTime());
        //out.println("\n\n");
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
                Statistics.getInstance().updateEstimators("B",j);
                PopulationEstimator.getInstance().updatePopulationOnDeparture(j);
                sendJobToServer(j);
            }
        }
        if (!toRemove.isEmpty()) {
            for (Job j : toRemove) {
                //rimozione del job completato
                jobsInService.remove(j);
            }
            scheduleNextDeparture();
            return true;
        }
        return false;
    }

    private void scheduleDestroy() {
        Event destroy = new Event(scheduler.getClock()+100.0,EventType.DESTROY,name,-1,-1);
        //out.println(scheduler.getClock()+"    "+destroy.getTime()+"  "+destroy.getType());
        scheduler.addEvent(destroy);
        //out.println(name+" ha schedulato ad itante "+ scheduler.getClock()+10000.0);
        uponDestroy = true;
    }

    private void scheduleNextDeparture() {
        //ordino i job in servizio in base al tempo di esecuzioe rimanente
        sortJobsInService();

        //elimino eventuali eventi departure già presenti
        scheduler.getAndRemoveDepartureEvent(name);

        //se non ho job da eseguire creo evento destroy
        if (jobsInService.isEmpty()) {
            scheduleDestroy();
            return;
        }

        //prendo il tempo di primo completamento tra i job in servizio
        Job firstToComplete = getFirstToComplete();
        if (firstToComplete == null) {
            return;
        }

        double firstDeparture = firstToComplete.getRemainingServiceTime() * jobsInService.size();
        firstDeparture += scheduler.getClock();

        //generare evento departure per il job
        Event departureForThis = new Event(firstDeparture, EventType.DEPARTURE,name,1, firstToComplete.getId());
        //aggiunta evento departure alla coda dello scheduler
        scheduler.addEvent(departureForThis);
    }

    private void sendJobToServer(Job j) {
        Event e = new Event(scheduler.getClock(), EventType.ARRIVAL,"A",1,j.getId());
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

    private Job getFirstToComplete() {
        if (jobsInService.isEmpty()) {
            return null;
        }
        return jobsInService.get(0);
    }

    //METODO PER DEBUG
    public void debugPrint(double time) {
        for (Job j: jobsInService) {
            out.println("DEBUG NODO "+name+": at time: "+time+"job id: "+j.getId()+", rst: "+j.getRemainingServiceTime());
        }
    }

    //metodo che server al load balancer per sapere quando il centro sia pieno
    public int getNumberOfJobsInServer() {
        return jobsInService.size();
    }
}
