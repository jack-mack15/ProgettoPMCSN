package pmcsn.centers;

import pmcsn.controllers.NextEventScheduler;
import pmcsn.entities.Job;
import pmcsn.estimators.CopyEstimator;
import pmcsn.events.Event;
import pmcsn.events.EventType;

import java.util.ArrayList;
import java.util.Objects;

import static java.lang.System.out;

public class BLoadBalancer {
    private ArrayList<NodeB> bNodes;
    private int maxBcopies;
    private NextEventScheduler scheduler;
    private int numOfDestroy;
    private int numOfCreate;
    private int currentIndex;
    private long descardedJobs;
    private long acceptedJobs;
    private long seenJobs;
    private int maxJobsForCopy;
    private ArrayList<Event> pendingEvents;
    private boolean isScaling;
    private int lastSelected;
    private boolean hasRecentDestroy;

    public BLoadBalancer(NextEventScheduler scheduler, boolean isScaling, int maxBcopies, int maxJobsForCopy) {
        this.scheduler = scheduler;
        this.currentIndex = 0;
        this.isScaling = isScaling;
        this.lastSelected = 0;
        this.hasRecentDestroy = false;
        this.maxJobsForCopy = maxJobsForCopy;
        descardedJobs = 0;
        acceptedJobs = 0;
        seenJobs = 0;
        bNodes = new ArrayList<>();
        pendingEvents = new ArrayList<>();
        this.maxBcopies = maxBcopies;

        bNodes.add(new NodeB(scheduler,"B_0"));

        numOfCreate = 0;
        numOfDestroy = 0;
    }

    public void removeCopy(String node) {
        if (!Objects.equals(node, "B_0")) {
            for (NodeB temp: bNodes) {
                if (temp.getName() == node) {
                    if (temp.getNumberOfJobsInServer() > 0) {
                        //si è ripopolato nel mentre
                        return;
                    }
                    bNodes.remove(temp);
                    numOfDestroy++;
                    hasRecentDestroy = true;
                    CopyEstimator.getInstance().onDestroy(scheduler.getClock());
                    return;
                }
            }
        }
    }

    public void createCopy(String node) {
        currentIndex++;
        String newName = "B_"+currentIndex;
        NodeB newCopy = new NodeB(scheduler,newName);
        bNodes.add(newCopy);
        numOfCreate++;

        //gestione pending events
        for (Event e: pendingEvents) {
            //in teoria ci sarà sempre e solo un job pending, ma se ci fossere due eventi con stesso preciso istante di arrivo
            //potrebbe essere più di uno
            newCopy.handleArrival(e);
        }
        pendingEvents.clear();
        CopyEstimator.getInstance().onCreation(scheduler.getClock());
    }

    public void handleArrival(Event e){
        //recupero la copia che deve ricevere l'arrivo
        NodeB node = selectNode(e);
        seenJobs++;
        if (node == null) {
            //in questo caso il job viene scartato
            descardedJobs++;
            //todo aggiornare pop sistema
            scheduler.incrementCurNumOfJobs();
            return;
        }
        acceptedJobs++;

        node.handleArrival(e);
    }

    public void handleDeparture(Event e){
        NodeB selected = null;
        int selectedIndex = -1;
        for (NodeB node: bNodes) {
            if (node.getName() == e.getNode()) {
                selected = node;
                selectedIndex = bNodes.indexOf(node);
            }
        }
        if (selected != null) {
            bNodes.get(selectedIndex).handleDeparture(e);
        }
    }

    //selectNode che riempie sempre il primo a disposizione
    private NodeB selectNode(Event event) {

        //caso no scaling
        if (!isScaling) {
            return bNodes.get(0);
        }
        //caso scaling, con più copie di B
        for (NodeB node: bNodes) {
            if(node.getNumberOfJobsInServer() < maxJobsForCopy) {
                return node;
            }
        }
        //se arrivo qua non ci sta nessuno libero
        if (bNodes.size() >= maxBcopies) {
            //drop del job appena arrivato
            return null;
        }
        pendingEvents.add(event);
        createCopy("B");
        //scheduleCreation(event);
        return null;
    }


    //selectNode meno pieno
    private NodeB selectNode3(Event event) {
        int limite = 3;

        //printSituation();

        //caso no scaling
        if (!isScaling) {
            return bNodes.get(0);
        }

        //caso scaling ma un solo nodo e libero
        if (bNodes.size() == 1 && bNodes.get(0).getNumberOfJobsInServer() < limite) {
            lastSelected = 0;
            return bNodes.get(0);
        }

        NodeB selectedNode = null;
        int min = limite;
        for (NodeB node: bNodes) {
            if(node.getNumberOfJobsInServer() < min) {
                min = node.getNumberOfJobsInServer();
                selectedNode = node;
            }
        }
        if(selectedNode != null) {
            return selectedNode;
        } else {
            //non ho trovato nodi liberi
            scheduleCreation(event);
            return null;
        }
    }

    //selectNode round robin
    private NodeB selectNode2(Event event) {
        int limite = 5;

        //printSituation();

        //caso no scaling
        if (!isScaling) {
            return bNodes.get(0);
        }

        //caso scaling ma un solo nodo e libero
        if (bNodes.size() == 1 && bNodes.get(0).getNumberOfJobsInServer() < limite) {
            lastSelected = 0;
            return bNodes.get(0);
        }

        //QUI CASO SCALING CON N NODI ATTIVI
        NodeB selectedNode = null;
        //dentro questo if se non ho eliminato copie dall'ultima selection
        if (!hasRecentDestroy) {
            //semplice round robin
            NodeB possibleNode = null;
            for (NodeB node: bNodes) {
                int temp = node.getNumberOfJobsInServer();
                //salvo un nodo se il round robin deve ricominciare da uno libero
                if(temp < limite && possibleNode == null) {
                    possibleNode = node;
                }
                int index = bNodes.indexOf(node);
                if (index > lastSelected && temp < limite) {
                    //il round robin ha trovato un match
                    selectedNode = node;
                    lastSelected = index;
                    break;
                }
            }
            if (selectedNode != null) {
                return selectedNode;
            } else {
                if (possibleNode != null) {
                    //entro qui dentro se il round robin non trova nodi liberi in avanti ma indietro
                    lastSelected = bNodes.indexOf(possibleNode);
                    return possibleNode;
                } else {
                    //arrivo in questo caso se non ho trovato alcun nodo libero, quindi ne creo uno nuovo
                    scheduleCreation(event);
                    lastSelected = 0;
                    return null;
                }
            }

        } else {
            //entro qui se tra adesso e l'ultima election, c'è stata una eliminazione
            //si resetta il round robin a causa di eliminazione
            lastSelected = 0;
            for (NodeB node: bNodes) {
                int temp = node.getNumberOfJobsInServer();
                if (temp < limite) {
                    //ritorno il primo libero e questo sarà l'indice del round robin
                    hasRecentDestroy = false;
                    lastSelected = bNodes.indexOf(node);
                    return node;
                }
            }
            //se arrivo qui, non ho trovato copie libere, quindi ne creo un'altra
            scheduleCreation(event);
            hasRecentDestroy = false;
            lastSelected = 0;
            return null;
        }
    }

    private void scheduleCreation(Event event) {
        Event createEvent = new Event(scheduler.getClock(), EventType.CREATE,"B",event.getClassId(), event.getIdRequest());
        scheduler.addEvent(createEvent);
        pendingEvents.add(event);
    }

    private void printSituation() {
        for (NodeB node: bNodes) {
            //node.debugPrint();
        }
        out.println("\n\n");
    }

    public long getDescardedJobs() {
        out.println("NODE B: accepted jobs are "+acceptedJobs);
        out.println("NODE B: seen jobs are "+seenJobs);
        return descardedJobs;
    }

    public int getNumbers() {
        out.println("\n\nLOAD BALANCER: numbers of creation: "+numOfCreate);
        out.println("LOAD BALANCER: number of destroy: "+numOfDestroy+"\n\n");
        return bNodes.size();
    }





}