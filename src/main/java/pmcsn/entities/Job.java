package pmcsn.entities;

import static java.lang.System.out;

public class Job implements Comparable<Job>{

    private final long id;
    private int jobClass;
    private final double arrivalTime;
    private final double serviceTime;
    private double remainServiceTime;
    private String node;
    private double completeTime;

    //questo valore epsilon è necessario per evitare problemi di approssimazione, ad esempio
    //t1 + t2 = t3 con t1 = t3 poichè t2 troppo piccolo
    private final double epsilon = 1e-10;

    public Job(String node, long id, double arrivalTime, int jobClass, double serviceTime) {
        this.node = node;
        this.jobClass = jobClass;
        this.id = id;
        this.arrivalTime = arrivalTime;
        this.serviceTime = serviceTime;
        this.remainServiceTime = serviceTime;
        this.completeTime = 0.0;
    }

    @Override
    public int compareTo(Job other) {
        return Double.compare(this.remainServiceTime, other.getRemainingServiceTime());
    }

    @Override
    public String toString(){
        return "to change";
    }

    //funzioni per set e get degli attributi
    public double getRemainServiceTime() {
        return remainServiceTime;
    }
    public int getJobClass() {
        return jobClass;
    }
    public long getId() {
        return id;
    }

    public double setRemainingServiceTime(double share) {
        double temp = remainServiceTime - share;
        if ((remainServiceTime - temp) < epsilon && share > 0.0) {
            //questo caso si dovrebbe verificare solo quando due eventi (uno departure e uno arrival)
            //hanno lo stesso tempo di arrivo o in caso di problemi di rappresentazione double
            //out.println("JOB: service remain time: "+(remainServiceTime-share));
            this.remainServiceTime = 0.0;
            return 0.0;
        }
        this.remainServiceTime = temp;
        return remainServiceTime;
    }

    public void setCompleteTime(double now) {
        this.completeTime = now;
    }
    public double getRemainingServiceTime() {
        return this.remainServiceTime;
    }
    public double getArrivalTime() {
        return arrivalTime;
    }
    public void setJobClass(int jobClass) {
        this.jobClass = jobClass;
    }
    public void setRemainServiceTime(double remainServiceTime) {
        this.remainServiceTime = remainServiceTime;
    }
    public String getNode() {
        return node;
    }
    public double getCompleteTime() {
        return completeTime;
    }
    public double getEpsilon() {
        return epsilon;
    }
    public double getServiceTime() {
        return serviceTime;
    }
}
