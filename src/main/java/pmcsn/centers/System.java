package pmcsn.centers;

import pmcsn.controllers.NextEventScheduler;
import pmcsn.events.Event;

//classe che mantiene la struttura della rete e si occupa di mandare gli eventi ai server giusti
public class System {
    private AbstractNode serverA;
    private AbstractNode serverP;
    private BLoadBalancer bLoadBalancer;

    private NextEventScheduler scheduler;

    public System(NextEventScheduler scheduler, boolean scaling, boolean isF2A, int maxNumOfBCopies, int maxJobsForCopy, int simType) {
        this.scheduler = scheduler;
        this.serverA = new NodeA(scheduler,isF2A);
        this.serverP = new NodeP(scheduler,isF2A);
        this.bLoadBalancer = new BLoadBalancer(scheduler,scaling,maxNumOfBCopies,maxJobsForCopy,simType);
    }

    //se arriva un evento CREATION
    public void handleCopyCreation(Event e) {
        bLoadBalancer.createCopy(e.getNode());
    }

    //se arriva un evento DESTROY
    public void handleCopyDestroy(Event event) {
        bLoadBalancer.removeCopy(event.getNode());

    }

    //funzione di ausilio
    private AbstractNode getNode(String nodeName) {
        if (nodeName == "A") {
            return serverA;
        } else if (nodeName == "P") {
            return serverP;
        } else {
            return null;
        }
    }

    //se arriva un evento ARRIVAL sceglie il server giusto
    public void handleArrival(Event e, String node) {
        AbstractNode target = getNode(node);
        if (target == null) {
            bLoadBalancer.handleArrival(e);
        } else {
            target.handleArrival(e);
        }
    }

    //come handleArrival ma per eventi departure
    public void handleDeparture(Event e, String node) {
        AbstractNode target = getNode(node);
        if (target == null) {
            bLoadBalancer.handleDeparture(e);
        } else {
            target.handleDeparture(e);
        }
    }

    //funzione che ottiene e mi ritorna i job scartati da B
    public long getBDescardedJobs() {
        return bLoadBalancer.getDescardedJobs();
    }

    public int getCopiesNum() {
        return bLoadBalancer.getCurrNumOfCopy();
    }
}
