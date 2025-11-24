package pmcsn.centers;

import pmcsn.controllers.NextEventScheduler;
import pmcsn.events.Event;

import java.util.ArrayList;

import static java.lang.System.out;

public class BLoadBalancer {
    private ArrayList<NodeB> bNodes;
    private ArrayList<NodeA> aNodes;
    private NextEventScheduler scheduler;
    private int numOfDestroy;
    private int numOfCreate;

    public BLoadBalancer(NextEventScheduler scheduler, String type) {
        this.scheduler = scheduler;
        bNodes = new ArrayList<>();
        if (type == "A") {
            aNodes.add(new NodeA(4, scheduler));
        } else if (type == "B") {
            bNodes.add(new NodeB(4, scheduler,"B_0"));
        }
        numOfCreate = 0;
        numOfDestroy = 0;
    }

    public void removeCopy(String node) {
        if (node == "B_0") {
            return;
        } else {
            for (NodeB temp: bNodes) {
                if (temp.getName() == node) {
                    if (temp.getNumberOfJobsInServer() > 0) {
                        //si è ripopolato nel mentre
                        return;
                    }
                    out.println("LOAD BALANCER rimozione copia "+node+"-----------------------------------------------------------------------------------");
                    bNodes.remove(temp);
                    numOfDestroy++;
                    return;
                }
            }
        }
    }

    public NodeB createCopy(String node) {
        String newName = "B_"+bNodes.size();
        NodeB newCopy = new NodeB(4,scheduler,newName);
        out.println("LOAD BALANCER: creata copia: "+newName+"----------------------------------------------------------------------------------------------");
        bNodes.add(newCopy);
        numOfCreate++;
        return newCopy;
    }

    public void handleArrival(Event e){
        //recupero la copia che deve ricevere l'arrivo
        NodeB node = selectNode();
        node.handleArrival(e);
    }
    public void handleDeparture(Event e){
        int index = Integer.parseInt(e.getNode().split("_")[1]);
        bNodes.get(index).handleDeparture(e);
    }

    private NodeB selectNode() {
        int limite = 5;
        int min = limite;
        int index = -1;

        printSituation();

        //funzione che prende la copia di b con meno job in servizio
        for (NodeB node: bNodes) {
            int temp = node.getNumberOfJobsInServer();
            if (temp < limite) {
                min = node.getNumberOfJobsInServer();
                index = Integer.parseInt(node.getName().split("_")[1]);
                break;
            }
        }

        if (min == limite) {
            //tutti i server hanno già il massimo numero di job, quindi creo una nuova copia
            //return createCopy("B");
            return bNodes.get(0);
        }

        return bNodes.get(index);
    }

    private void printSituation() {
        out.println("LOAD BALANCER: stampa situazioe[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[");
        for (NodeB node: bNodes) {
            node.debugPrint();
        }
        out.println("\n\n");
    }


    public int getNumbers() {
        out.println("\n\nLOAD BALANCER: numbers of creation: "+numOfCreate);
        out.println("LOAD BALANCER: number of destroy: "+numOfDestroy+"\n\n");
        return bNodes.size();
    }





}