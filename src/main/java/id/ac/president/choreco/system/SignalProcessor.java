package id.ac.president.choreco.system;

import org.apache.commons.math3.stat.descriptive.SummaryStatistics;


public class SignalProcessor {

    public static int[] peakDetection(float[] data, float threshold) {

        // init stats instance
        SummaryStatistics stats = new SummaryStatistics();

        // the results (peaks, 1 or -1) of our algorithm
        int[] signals = new int[data.length];

        // init average and standard deviation
        for (float datum : data) {
            stats.addValue(datum);
        }
        float avg = (float) stats.getMean();
        float std = (float) Math.sqrt(stats.getPopulationVariance()); // getStandardDeviation() uses sample variance
        stats.clear();

        // loop input
        for (int i = 0; i < data.length; i++) {

            // if the distance between the current value and average is enough standard deviations (threshold) away
            if (Math.abs(data[i] - avg) > threshold * std) {

                // this is a signal (i.e. peak), determine if it is a positive or negative signal
                if (data[i] > avg) {
                    signals[i] = 1;
                } else {
                    signals[i] = -1;
                }
            } else {
                // ensure this signal remains a zero
                signals[i] = 0;
            }
        }

        return signals;

    }

    // data input in frequency-domain with amplitudes (not in complex number representation)
    public static float[][] trimOfRange(float[][] fft_amp_data, float from_freq, float to_freq, float sampleRate) {
        float freq_res = sampleRate / (fft_amp_data[0].length * 2);

        int idx_from = (int) (from_freq / freq_res);
        int idx_to = (int) (to_freq / freq_res);
        int length = idx_to-idx_from+1;
        float[][] trimmed = new float[fft_amp_data.length][length];
        for (int i = 0; i < trimmed.length; i ++) {
            System.arraycopy(
                    fft_amp_data[i], idx_from,
                    trimmed[i], 0,
                    length
            );
        }
        return trimmed;
    }

    public static float[] trimOfRange(float[] fft_amp_data, float from_freq, float to_freq, float sampleRate) {
        float freq_res = sampleRate / (fft_amp_data.length * 2);

        int idx_from = (int) (from_freq / freq_res);
        int idx_to = (int) (to_freq / freq_res);
        int length = idx_to-idx_from+1;
        float[] trimmed = new float[length];
        System.arraycopy(
                fft_amp_data, idx_from,
                trimmed, 0,
                length
        );
        return trimmed;
    }

    public static void normalizePower(float[][] data) {
        int frameTotal = data.length;
        int n = data[0].length;
        float maxAmp = data[0][0], minAmp = maxAmp;
        for (float[] datum : data) {
            if (datum[0] > maxAmp) {
                maxAmp = datum[0];
            } else if (datum[0] < minAmp) {
                minAmp = datum[0];
            }
            for (int j = 0; j + 1 < n; j++) {
                if (datum[j + 1] > maxAmp) {
                    maxAmp = datum[j + 1];
                } else if (datum[j + 1] < minAmp) {
                    minAmp = datum[j + 1];
                }
            }
        }
        float diff = maxAmp - minAmp;
        for (int i = 0; i < frameTotal; i++){
            for (int j = 0; j < n; j++){
                data[i][j] = (data[i][j]-minAmp)/diff;
            }
        }
    }

    public static void normalizePower(float[] data) {
        float maxAmp = data[0], minAmp = maxAmp;

        for (int i = 0; i + 1 < data.length; i++) {
            if (data[i + 1] > maxAmp) {
                maxAmp = data[i + 1];
            } else if (data[i + 1] < minAmp) {
                minAmp = data[i + 1];
            }
        }
        float diff = maxAmp - minAmp;
        for (int i = 0; i < data.length; i++){
            data[i] = (data[i]-minAmp)/diff;
        }
    }

    public static void powerToDb(float[][] data) {
        int frameTotal = data.length;
        int n = data[0].length;
        for (int i = 0; i < frameTotal; i++) {
            for (int j = 0; j < n; j++) {
                if (data[i][j] != 0) {
                    data[i][j] = (float) (10 * Math.log10(data[i][j]));
                }
            }
        }
    }

    public static void powerToDb(float[] data) {
        for (int i = 0; i < data.length; i++) {
            data[i] = (float) (10 * Math.log10(data[i]));
        }
    }

}
