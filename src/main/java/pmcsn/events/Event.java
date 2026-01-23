package pmcsn.events;

import static java.lang.System.out;

public class Event implements Comparable<Event> {

    private double time;
    private EventType type;
    private String node;
    private int classId;

    private long idRequest;

    public Event(double time, EventType type, String nodeName, int classId, long idRequest){
        this.time = time;
        this.type = type;
        this.classId = classId;
        this.node = nodeName;
        this.idRequest = idRequest;
    }

    //metodo che implementa l'ordinamento automatico della priorityqueue
    //normalmente il grosso if non servirebbe ma per essere sicuri che non ci siano problemi con eventi
    //con stesso istante di arrivo, è stato implementata ulteriore logica per ordinamento (il grosso if)
    //other è l'elemento già presente nella priority queue
    //this è il nuovo elemento da inserire nella priority queue
    // se il valore di ritorno è -1, inserisco this prima di other
    // con valore maggiore di 0, inserisco this dopo other
    // con 0, l'inserimento può essere casuale
    @Override
    public int compareTo(Event other){
        //other è l'elemento già nella lista degli eventi
        if(Double.compare(this.time,other.time) == 0) {
            //significa che il nuovo elemento this, ha stesso tempo di arrivo di other, già presente in lista
            EventType newT = this.getType();
            EventType oldT = other.getType();
            if (newT == EventType.DEPARTURE && this.getNode() == "B") {
                return -1;
            } else if (oldT == EventType.DEPARTURE && other.getNode() == "B") {
                return 1;
            } else if (oldT == EventType.DESTROY) {
                return -1;
            } else if (newT == EventType.DESTROY) {
                return 1;
            } else if (newT == EventType.CREATE){
                return -1;
            } else if (oldT == EventType.CREATE) {
                return 1;
            } else {
                return 1;
            }
        }
        return Double.compare(this.time, other.time);
    }

    @Override
    public String toString(){
        return "to change";
    }

    //metodi di ausilio
    public double getTime() {
        return time;
    }
    public void setTime(long time) {
        this.time = time;
    }
    public EventType getType() {
        return type;
    }
    public void setType(EventType type) {
        this.type = type;
    }
    public int getClassId() {
        return classId;
    }
    public void setClassId(int classId) {
        this.classId = classId;
    }
    public String getNode() {
        return node;
    }
    public void setNode(String node) {
        this.node = node;
    }
    public void setTime(double time) {
        this.time = time;
    }
    public long getIdRequest() {
        return idRequest;
    }
}