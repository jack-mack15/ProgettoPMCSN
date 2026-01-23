package pmcsn.centers;

import pmcsn.controllers.NextEventScheduler;
import pmcsn.entities.Job;
import pmcsn.estimators.BatchMeansEstimator;
import pmcsn.estimators.CopyEstimator;
import pmcsn.estimators.Statistics;
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
    private long descardedForBatch;
    private long acceptedJobs;
    private long seenForBatch;
    private long seenJobs;
    private int maxJobsForCopy;
    private ArrayList<Event> pendingEvents;
    private boolean isScaling;
    private int lastSelected;
    private boolean hasRecentDestroy;
    private boolean uponCreate;

    public BLoadBalancer(NextEventScheduler scheduler, boolean isScaling, int maxBcopies, int maxJobsForCopy, int simType) {
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
        if (isScaling && simType == 2) {
            BatchMeansEstimator.getInstance().setBatchForLoss(this);
        }
        numOfCreate = 0;
        numOfDestroy = 0;
    }

    //metodo che elimina una copia di B. Se la copia è B_0 non viene eliminata
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

    //metodo che si occupa di creare una copia del server B
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

    //metodo che si occupa di gestire l'evento arrivo. Seleziona la copia di B che deve riceverlo.
    //se necessario crea una nuova copia
    public void handleArrival(Event e){
        //recupero la copia che deve ricevere l'arrivo
        NodeB node = selectNode(e);
        seenJobs++;
        seenForBatch++;
        if (node == null && !uponCreate) {
            //in questo caso il job viene scartato. solo in fase di verifica
            descardedJobs++;
            descardedForBatch++;
            scheduler.incrementCurNumOfJobs();
            Statistics.getInstance().finalizeDroppedJob(e);
            return;
        } else if (uponCreate) {
            uponCreate = false;
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
        scheduleCreation(event);
        uponCreate = true;
        return null;
    }

    public long getAndResetDiscardedJobs() {
        long temp = descardedForBatch;
        descardedForBatch = 0;
        return temp;
    }

    public long getAndResetSeenJobs() {
        long temp = seenForBatch;
        seenForBatch = 0;
        return temp;
    }


    //selectNode meno pieno, non usato
    //todo remove
    private NodeB selectBestNode(Event event) {
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

    //selectNode round robin, non usato
    //todo remove
    private NodeB selectRRNode(Event event) {
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


    //metodo che si occupa di schedulare l'evento CREATION
    private void scheduleCreation(Event event) {
        Event createEvent = new Event(scheduler.getClock(), EventType.CREATE,"B",event.getClassId(), event.getIdRequest());
        scheduler.addEvent(createEvent);
        pendingEvents.add(event);
    }


    //metodo che ritorna il numero di job scartati
    public long getDescardedJobs() {
        return descardedJobs;
    }

    //metodo che ritorna il numero di copie attuali
    public int getCurrNumOfCopy() {
        return bNodes.size();
    }





}