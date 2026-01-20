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

import static java.lang.System.*;

public class NodeA extends AbstractNode{

    private double serviceRate[];
    private ArrayList<Job> jobsInService;
    private double lastUpdate;

    public NodeA(NextEventScheduler scheduler,boolean isF2A) {
        super("A", scheduler);
        if (isF2A) {
            serviceRate = new double[] {0.2,0.4,0.15};
        } else {
            serviceRate = new double[]{0.2, 0.4, 0.1};
        }
        jobsInService = new ArrayList<>();
        lastUpdate = 0.0;
    }

    @Override
    public void handleArrival(Event e) {
        int newClassId = e.getClassId();

        //lista di variabili che dipendono dalla classe dell'evento
        String streamSelection = "";
        int selectedServiceRate = 0;
        switch (newClassId) {
            case -1:
                streamSelection = "A_1";
                break;

            case 1:
                streamSelection = "A_2";
                selectedServiceRate = 1;
                break;

            case 2:
                streamSelection = "A_3";
                selectedServiceRate = 2;
                break;

            default:
                out.println("NODO A: errore ricezione di un job con classe "+newClassId+"\n");
                exit(-1);
        }

        //ottenere il tempo di esecuzione del job appena arrivato
        scheduler.getRng().selectStream(streamSelection);
        double serviceTime = Exponential.exponential(serviceRate[selectedServiceRate],scheduler.getRng());

        //creazione job e inserimento nella giusta lista
        Job newJob = new Job("A",e.getIdRequest(), scheduler.getClock(),e.getClassId(), serviceTime);

        //aggiornamento popolatione
        PopulationEstimator.getInstance().updatePopulationOnArrival(newJob);

        //prima di aggiungere il job ai job in esecuzione, eseguo l'update del remaining service time di tutti i job in servizio
        updateRemainingServiceTime(scheduler.getClock());

        //aggiunta del nodo ai job in esecuzione
        jobsInService.add(newJob);

        scheduleNextDeparture();

        //TODO rimuovere questa stampa di debug
        //debugPrint();
        //out.println("\n\n");
    }

    @Override
    public void handleDeparture(Event e) {
        //out.println("NODO A: handling departure ad istante: "+e.getTime());

        //aggiorno i tempi di servizio rimanenti, in base al clock attuale
        updateRemainingServiceTime(scheduler.getClock());

        //verifico se ci sono job completati (internamente verranno gestiti gli invii ad altri nodi)
        checkTerminateJobAndDeparture();

        //qui devo aggiungere il prossimo evento departure
        scheduleNextDeparture();

        //TODO rimuovere questa stampa di debug
        //debugPrint();
        //out.println("\n\n");

    }

    //metodo che si occupa di creare e aggiungere il prossimo evento di departure per questo nodo.
    //effettivamente la departure non richiede una classe, perchè poi è la classe del job che completa a influenzare
    //il path del job
    private void scheduleNextDeparture() {
        //ordino i job in servizio in base al tempo di esecuzioe rimanente
        sortJobsInService();

        //non servirebbe, ma comunque verifico situazioni problematiche
        scheduler.getAndRemoveDepartureEvent("A");

        //prendo il tempo di primo completamento tra i job in servizio
        Job firstJob = getFirstToComplete();
        if (firstJob == null) {
            //non ci sono più job in esecuzione
            return;
        }
        double firstDeparture = firstJob.getRemainingServiceTime() * jobsInService.size();
        firstDeparture += scheduler.getClock();

        //generare evento departure per il job
        Event departureForThis = new Event(firstDeparture, EventType.DEPARTURE,"A",firstJob.getJobClass(),firstJob.getId());
        //aggiunta evento departure alla coda dello scheduler
        scheduler.addEvent(departureForThis);

        //out.println("NODO A: creato evento departure ad istante:"+departureForThis.getTime());
    }


    //questa funzione va ad aggiornare i remaining time dei job in servizio.
    //ci potrebbero essere casi estremi in cui questo comporta avere dei job che terminano, questi
    //verranno eliminati dalla lista
    private void updateRemainingServiceTime(double now) {
        double elapsed = Math.max(0.0, now - lastUpdate);
        double share = elapsed / jobsInService.size();
        for (Job j: jobsInService) {
            j.setRemainingServiceTime(share);
        }
        lastUpdate = now;
    }

    //questo metodo serve a verificare se per qualche motivo dei job sono stati completati senza un evento departure
    //la situazione si può verificare o all'inizo delle run o se eventi departure e arrival sono nello stesso istante
    private void checkTerminateJobAndDeparture() {
        //controllo se ci sono job da rimuovere
        ArrayList<Job> toRemove = new ArrayList<>();
        for (Job j: jobsInService) {
            double temp = j.getRemainingServiceTime();
            if (temp <= j.getEpsilon()) {
                toRemove.add(j);
                j.setCompleteTime(scheduler.getClock());

                //aggiornamento per le statistiche
                Statistics.getInstance().updateEstimators("A",j);
                PopulationEstimator.getInstance().updatePopulationOnDeparture(j);

                //aumento il numro di job passati per il sistema
                if (j.getJobClass() == 2) {
                    scheduler.incrementCurNumOfJobs();
                }

                sendJobToServer(j);
            }
        }
        if (!toRemove.isEmpty()) {
            for (Job j: toRemove) {
                jobsInService.remove(j);
            }
        }
    }

    //METODO PER DEBUG
    private void debugPrint() {
        for (Job j: jobsInService) {
            out.println("DEBUG NODO A: job id: "+j.getId()+", rst: "+j.getRemainingServiceTime());
        }
    }

    //Metodo che in base al job terminato, aggiunge un nuovo evento alla coda di eventi di tipo Arrival ma per il nodo successivo
    //se il nodo deve uscire dal sistema non si aggiunge nessun evento
    private void sendJobToServer(Job j) {
        String nextNode = "";
        int currentClass = j.getJobClass();
        if (currentClass == -1) {
            currentClass = 1;
            nextNode = "B";
        } else if (currentClass == 1) {
            currentClass = 2;
            nextNode = "P";
        } else {
            nextNode = "EXIT";
            return;
        }
        Event e = new Event(scheduler.getClock(), EventType.ARRIVAL,nextNode,currentClass,j.getId());
        //out.println("NODO A: sending job to "+e.getNode());
        scheduler.addEvent(e);
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

}
