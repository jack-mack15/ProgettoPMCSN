package pmcsn.estimators;

import static java.lang.System.out;

public class Population {
    private int numJobs;
    private double startTime;
    private double lastUpdate;

    //mi serve per calcolare l'integrale di N(t) e poi per ottenere la media
    private double area;

    //mi serve per calcolare l'integrale di N(t)^2, poi insieme alla media ci ottengo la varianza, se servisse
    private double areaSquared;
    private double busyTime;
    private double idleTime;
    private int nodeDeparture;

    public Population() {
        numJobs = 0;
        startTime = 0.0;
        area = 0.0;
        areaSquared = 0.0;
        busyTime = 0.0;
        idleTime = 0.0;
        nodeDeparture = 0;
    }

    private void updateBusyTimeOnArrival(double now) {
        if (numJobs == 0) {
            double idle = now - lastUpdate;
            this.idleTime += idle;
        } else {
            double busy = now -lastUpdate;
            this.busyTime += busy;
        }
    }

    private void updateBusyTimeOnDeparture(double now) {
        if (numJobs >= 0) {
            //la condizione non servirebbe, ma per sicurezza l'ho messa
            double busy = now - lastUpdate;
            this.busyTime += busy;
        }
    }

    private void checkBusyTimeCorrect(double now) {
        double elaps = now - startTime;
        out.println("check sul busy time");
        out.println("tempo totale: "+elaps);
        out.println("verifica: "+(busyTime+idleTime)+"\n\n");
    }

    public void jobArrival(double now) {
        updateBusyTimeOnArrival(now);
        updateAreas(now);
        numJobs++;
    }

    public void jobDeparture(double now) {
        nodeDeparture++;
        updateBusyTimeOnDeparture(now);
        updateAreas(now);
        numJobs--;
    }

    private void updateAreas(double now) {
        double delta = now - lastUpdate;
        if (delta > 0.0) {
            area += numJobs * delta;
            areaSquared += numJobs * numJobs * delta;
        }
        lastUpdate = now;
    }

    //media temporale della popolazione (sistema o per nodo a seconda di come si usa)
    public double getPopulationMean(double currTime) {
        double observationTime = currTime - startTime;
        return area / observationTime;
    }

    public void debug(double time) {
        out.println(numJobs);
        out.println(nodeDeparture);
        out.println(getUtilization(time));
    }

    //per la varianza sfrutto la formula E[X^2] - E[X]^2
    public double getPopulationVariance(double currTime) {
        double mean = getPopulationMean(currTime);
        double secondMoment = areaSquared / (currTime - startTime);
        double variance = secondMoment - mean * mean;
        return variance;
    }

    //per la deviazione standard si usa il metodo della varianza con radice quadrata
    public double getPopulationStandardDeviation(double finish) {
        return Math.sqrt(getPopulationVariance(finish));
    }

    public double getBusyTime() {
        return busyTime;
    }
    public double getUtilization(double finish) {
        double elapsed = finish - startTime;
        return getBusyTime() / elapsed;
    }

    public int getNodeDeparture() {
        return nodeDeparture;
    }

    public double getThroughput(double finish) {
        double elaps = finish - startTime;
        return getNodeDeparture() / elaps;
    }

    public void resetPopulation(double newStart) {
        busyTime = 0.0;
        idleTime = 0.0;
        area = 0.0;
        areaSquared = 0.0;
        startTime = newStart;
        nodeDeparture = 0;
        lastUpdate = newStart;
    }

    public void hardReset() {
        busyTime = 0.0;
        idleTime = 0.0;
        area = 0.0;
        areaSquared = 0.0;
        startTime = 0.0;
        nodeDeparture = 0;
        lastUpdate = 0.0;
        numJobs = 0;
    }
}
