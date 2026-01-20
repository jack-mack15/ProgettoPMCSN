package pmcsn.centers;

import pmcsn.controllers.NextEventScheduler;
import pmcsn.events.Event;

public class System {
    private AbstractNode serverA;
    private AbstractNode serverP;
    private BLoadBalancer bLoadBalancer;

    private NextEventScheduler scheduler;

    public System(NextEventScheduler scheduler, boolean scaling, boolean isF2A, int maxNumOfBCopies, int maxJobsForCopy) {
        this.scheduler = scheduler;
        this.serverA = new NodeA(scheduler,isF2A);
        this.serverP = new NodeP(scheduler,isF2A);
        this.bLoadBalancer = new BLoadBalancer(scheduler,scaling,maxNumOfBCopies,maxJobsForCopy);
    }

    public void handleCopyCreation(Event e) {
        bLoadBalancer.createCopy(e.getNode());
    }

    public void handleCopyDestroy(Event event) {
        bLoadBalancer.removeCopy(event.getNode());

    }

    private AbstractNode getNode(String nodeName) {
        if (nodeName == "A") {
            return serverA;
        } else if (nodeName == "P") {
            return serverP;
        } else {
            return null;
        }
    }

    public void handleArrival(Event e, String node) {
        AbstractNode target = getNode(node);
        if (target == null) {
            bLoadBalancer.handleArrival(e);
        } else {
            target.handleArrival(e);
        }
    }

    public void handleDeparture(Event e, String node) {
        AbstractNode target = getNode(node);
        if (target == null) {
            bLoadBalancer.handleDeparture(e);
        } else {
            target.handleDeparture(e);
        }
    }

    public long getBDescardedJobs() {
        return bLoadBalancer.getDescardedJobs();
    }

    public int getCopiesNum() {
        return bLoadBalancer.getNumbers();
    }
}
