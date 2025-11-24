package pmcsn.estimators;

import static java.lang.System.out;

public class Population {
    private int numJobs;
    private double startTime;
    private double lastUpdate;

    //mi serve per calcolare l'integrale di N(t) e poi per ottenere la media
    private double area;

    //mi serve per calcolare l'integrale di N(t)^2, poi insieme alla media ci ottengo la varianza
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
    public double getPopulationMean() {
        double observationTime = lastUpdate - startTime;
        return area / observationTime;
    }

    //per la varianza sfrutto la formula E[X^2] - E[X]^2
    public double getPopulationVariance() {
        double mean = getPopulationMean();
        double secondMoment = areaSquared / (lastUpdate - startTime);
        double variance = secondMoment - mean * mean;
        return variance;
    }

    //per la deviazione standard si usa il metodo della varianza con radice quadrata
    public double getPopulationStandardDeviation() {
        return Math.sqrt(getPopulationVariance());
    }

    public double getBusyTime() {
        return busyTime;
    }
    public double getUtilization() {
        double elapsed = lastUpdate - startTime;
        //checkBusyTimeCorrect(lastUpdate);
        return getBusyTime() / elapsed;
    }

    public int getNodeDeparture() {
        return nodeDeparture;
    }

    public double getThroughput() {
        double elaps = lastUpdate - startTime;
        return (double) getNodeDeparture() / elaps;
    }
}
