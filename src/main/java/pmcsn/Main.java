package pmcsn;

import static java.lang.System.out;

public class Main {
    public static void main(String[] args) {

        out.println(1.0/0.5);
        //run();
        //inizio run
    }

    //calcolo speriamo giusto per il calcolo dei pi grechi
    public static void run() {
        int limite = 8;
        int c = 12;
        double pi0 = 1.0;
        double l = 1.2;
        double div = 1;
        double m = 1.25;

        for(int i = 1; i < c; i++) {
            double temp = 0.0;
            int parteInt = (i/limite) +1;
            int resto = i % limite;

            if (parteInt > 1 && i > 3) {
                if (resto == 0) {
                    div *= (parteInt-1);
                } else {
                    div *= parteInt;
                }
            }

            double lambdaSuMu = l / m;
            lambdaSuMu = Math.pow(lambdaSuMu,i);

            temp += lambdaSuMu;
            temp = temp /div;

            pi0 += temp;

        }

        pi0 = 1 / pi0;
        out.println("pi0 viene: "+pi0);
        div = 1.0;
        double pop = 0.0;

        for (int i = 1; i < c; i++){
            double pi_i = 0.0;
            int parteInt = (i/limite) +1;
            int resto = i % limite;

            if (parteInt > 1 && i > 3) {
                if (resto == 0) {
                    div *= (parteInt-1);
                } else {
                    div *= parteInt;
                }
            }

            double lambdaSuMu = l / m;
            pi_i = Math.pow(lambdaSuMu,i);
            pi_i = pi_i * pi0;
            pi_i = pi_i / div;
            pop += pi_i * i;
            out.println("pi_"+i+": "+pi_i);
        }

        out.println("pop is: "+pop);
    }

    public static void run2() {
        double l = 1.2;
        double m = 1.25;
        int c = 200;
        double result = 1.0;
        for (int i = 1; i < c; i++) {
            double temp;
            double tempResult;
            if (i <= 8) {
                temp = l/m;
            } else if (i <= 16) {
                temp = l / (2*m);
            } else {
                temp = l / (3*m);
            }
            tempResult = temp;
            for (int j = 1; j < i; j++) {
                tempResult *= temp;
            }
            result += tempResult;
        }
        out.println("divisore è: "+ result);
    }
    public static void run3() {
        double l = 1.2;
        double m = 1.25;
        int limite = 8;
        int max = 20;
        double result = 1.0;

        for (int i = 1; i < max; i++) {
            double tempM = m;
            if (i <= 16 && i > 8) {
                tempM = m * 2;
            } else if (i > 16 ) {
                tempM = m * 3;
            }
            double temp = 1.0;
            int fact = 1;
            //potenza i e fattoriale
            for (int j = 1; j <= i; j++) {
                temp *= l/tempM;
                fact *= j;
            }

            result += temp /fact;

        }

        double res = 1/result;
        out.println("pi greco 0 is: "+res);
    }

}