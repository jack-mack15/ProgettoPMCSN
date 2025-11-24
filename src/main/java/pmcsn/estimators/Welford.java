package pmcsn.estimators;

public class Welford {
    private long n;
    private double mean;
    private double sumSquaredDev;
    private double min;
    private double max;

    public Welford() {
        this.n = 0L;
        this.mean = 0.0;
        this.sumSquaredDev = 0.0;
        this.min = Double.POSITIVE_INFINITY;
        this.max = Double.NEGATIVE_INFINITY;
    }

    //metodo fulcro di welford. Calcolo i valori Xi (X sbarrato) e Vi (V sbarrata) delle slide
    //poi per le metriche uso i metodi sotto
    public void addData(double x) {
        n += 1;
        double delta = x - mean;
        //robba di v
        sumSquaredDev = sumSquaredDev + (delta*delta)*(n-1) /n;
        mean = mean + delta / n;
        if (x < min) {
            min = x;
        }
        if (x > max) {
            max = x;
        }
    }

    //metodo che ritorna la media
    public double getMean() {
        return mean;
    }

    //metodo che ritorna la varianza
    public double getVariance() {
        return sumSquaredDev / n;
    }

    //metodo che ritorna la deviazione standard
    public double getStandardDeviation() {
        return Math.sqrt(getVariance());
    }

    //metodo che ritorna il minimo
    public double getMin() {
        return min;
    }

    //metodo che ritorna il massimo
    public double getMax() {
        return max;
    }
}
