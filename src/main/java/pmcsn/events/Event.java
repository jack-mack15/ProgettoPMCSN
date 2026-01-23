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
            /*if (newT == EventType.SAMPLING || oldT == EventType.SAMPLING) {
                //mi interessa poco
                return 0;
            } else if (this.getNode() == "P" || other.getNode() == "P") {
                //non mi interessa
                return 0;
            } else if (newT == EventType.ARRIVAL && other.getNode() == "B" && oldT == EventType.DEPARTURE && this.getNode() == "B") {
                //arrivo in b in contemporanea con departure b
                //deve essere in lista prima Departure di B e poi arrivo per B
                return 1;
            } else if (oldT == EventType.ARRIVAL && other.getNode() == "B" && newT == EventType.DEPARTURE && this.getNode() == "B") {
                //arriva una nuova departure per B e la sto confrontando con un arrivo per B già in lista
                //nuova departure deve essere prima
                return -1;
            } else if (newT == EventType.DESTROY || oldT == EventType.CREATE) {
                //nuovi eventi destroy o create, devono essere processati subito
                return 1;
            } else if (oldT == EventType.DESTROY || newT == EventType.CREATE) {
                //evento destroy e create già in lista hanno priorità
                return -1;
            }*/
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