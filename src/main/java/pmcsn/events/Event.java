package pmcsn.events;

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