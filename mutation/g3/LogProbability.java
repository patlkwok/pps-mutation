package mutation.g3;

/**
 *
 * @author group3
 */
public class LogProbability {

    public static final double LOG_ZERO_PROB = Double.NEGATIVE_INFINITY;
    public static final double LOG_ONE_PROB = 0;
    public static final double LOG_ZERO_THRESHOLD = -150;

    public static double p2log(double p) {
        return Math.log(p);
    }

    public static double logPAdd(double p1, double p2) {
        p1 = logPTrunc(p1);
        p2 = logPTrunc(p2);
        double r;
        // if p1 or p2 are 0, return 0, probability cannot go beyond 1 (0 in log space)
        if (p1 == 0.0 || p2 == 0.0) {
            r = 0.0;
        } else if (p1 == LOG_ZERO_PROB) {
            r = p2;
        } else if (p2 == LOG_ZERO_PROB) {
            r = p1;
        } else {
            r = p1 + Math.log1p(Math.exp(p2 - p1));
        }
        if (r == Double.POSITIVE_INFINITY) {
            System.out.println("Here");
        }
        r = logPTrunc(r);
        return r;
    }

    public static double logPMult(double p1, double p2) {
        p1 = logPTrunc(p1);
        p2 = logPTrunc(p2);
        if (p1 == LOG_ZERO_PROB || p2 == LOG_ZERO_PROB) {
            return LOG_ZERO_PROB;
        }
        if (p1 + p2 == Double.POSITIVE_INFINITY || p1 + p2 > 0) {
            //System.out.println("Here");
        }
        return logPTrunc(p1 + p2);
    }

    public static double logPTrunc(double p) {
        if (p < LOG_ZERO_THRESHOLD) {
            return LOG_ZERO_PROB;
        } else if (p > LOG_ONE_PROB) {
            return LOG_ONE_PROB;
        } else {
            return p;
        }
    }
}
