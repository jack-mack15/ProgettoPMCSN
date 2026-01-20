package pmcsn.centers;

import pmcsn.controllers.NextEventScheduler;
import pmcsn.events.Event;

public abstract class AbstractNode {

    //nome del server (A, B o P)
    protected final String name;

    //numero di serventi occupati
    protected int occupiedServesNum;

    //istanza dello scheduler per aggiungere eventi departure
    protected NextEventScheduler scheduler;

    //identificatore dell'ultimo job del nodo

    public AbstractNode(String name,NextEventScheduler scheduler) {
        this.name = name;
        this.occupiedServesNum = 0;
        this.scheduler  = scheduler;
    }

    //metodo astratto che si occupa di gestire un evento del nodo
    abstract public void handleArrival(Event e);
    abstract public void handleDeparture(Event e);
    public int getOccupiedServesNum() {
        return occupiedServesNum;
    }
    public String getName() {
        return name;
    }
}
