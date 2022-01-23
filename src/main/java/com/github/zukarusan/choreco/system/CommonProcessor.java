package com.github.zukarusan.choreco.system;

import com.github.zukarusan.choreco.component.spectrum.FrequencySpectrum;
import com.github.zukarusan.choreco.component.SignalFFT;
import com.github.zukarusan.choreco.component.Signal;
import com.github.zukarusan.choreco.component.spectrum.Spectrum;
import com.github.zukarusan.choreco.system.exception.STFTException;
import com.github.zukarusan.choreco.util.VectorUtils;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
import be.tarsos.dsp.beatroot.*;

import java.util.ArrayList;
import java.util.List;

import static java.lang.Float.NaN;


public final class CommonProcessor {

    public static void harmonicPeakSubtract(float[][] bins, float freqRes, int harmonic_size) {
        int len = bins.length;
        int len_frame = bins[0].length;
        for (int i = 0; i < len; i++) {
            int H = harmonic_size;
            for (int j = 1; j < len_frame; j++) { // discard 0Hz
                if (j * H >= len_frame) H = len_frame/j - 1;
                float sum = 0;
                for (int h = 0, hf = j; h < H; h++, hf+=j) {
                    sum += bins[i][hf] -
                            Math.max(a(bins[i], j, H), Math.max(b(bins[i], j), g(bins[i], j)));
                }
                bins[i][j] = sum;
            }
        }
    }

    /*** Penalize even harmonic ***/
    public static float a(final float[] bin, int freq_idx, int harmonic_size) {
        float sum = 0;
        float idx = (0.5f) * freq_idx;
        for (int h = 0; h < harmonic_size; h++, idx += freq_idx) {
            sum += bin[Math.round(idx)];
        }
        return sum;
    }
    /*** Penalize third harmonic ***/
    final static float[] h3rd = {1/3f, 2/3f, 4/3f, 5/3f};
    public static float b(final float[] bin, int freq_idx) {
        float min = bin[Math.round(h3rd[0] * freq_idx)];
        for (int h = 1; h < h3rd.length; h++) {
            int i = Math.round(h3rd[h] * freq_idx);
            if (i >= bin.length) break;
            min = Math.min(min, bin[i]);
        }
        return min;
    }
    /*** Penalize fifth harmonic ***/
    final static float[] h5th = {1/5f, 2/5f, 3/5f, 4/5f};
    public static float g(final float[] bin, int freq_idx) {
        float min = bin[Math.round(h5th[0] * freq_idx)];
        for (int h = 1; h < h5th.length; h++) {
            min = Math.min(min, bin[Math.round(h5th[h] * freq_idx)]);
        }
        return min;
    }

    public static int[] findPeaksByAverage(float[] data, float threshold) {

        // init stats instance
        SummaryStatistics stats = new SummaryStatistics();

        // the results (peaks, 1 or -1) of our algorithm
        List<Integer> peaks = new ArrayList<>();

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
            if (Math.abs(data[i] - avg) > threshold * std)
                peaks.add(i);
        }
        return peaks.stream().mapToInt(i -> i).toArray();
    }

//            if (Math.abs(data[i] - avg) > threshold * std) {
//
//                peaks.add(i);
//                peaks[i] = 1;
//                 this is a signal (i.e. peak), determine if it is a positive or negative signal
//                if (data[i] > avg) {
//                    peaks[i]    = 1;
//                } else {
//                    peaks[i] = -1;
//                }
//
//            }
//            else {
//                // ensure this signal remains a zero
//                peaks[i] = 0;
//            }

/*

    public static int[] tarsosPeakFinder(float[] data, float threshold, int minWidth) {
        double[] d_data = new double[data.length];
        SummaryStatistics stats = new SummaryStatistics();

        for (int i = 0; i < d_data.length; i++) {
            d_data[i] = data[i];
            stats.addValue(data[i]);
        }

        float avg = (float) stats.getMean();
        stats.clear();
        return Peaks.findPeaks(d_data, minWidth, avg * threshold).stream().mapToInt(i -> i).toArray();
    }
*/

    public static int findPeaksByExtremePoints(float[] data, int[] peaks, int width) {
        int peakCount = 0;
        int maxp;
        int mid = 0;
        int end = data.length;
        while (mid < end) {
            int i = mid - width;
            if (i < 0)
                i = 0;
            int stop = mid + width + 1;
            if (stop > data.length)
                stop = data.length;
            maxp = i;
            for (i++; i < stop; i++)
                if (data[i] > data[maxp])
                    maxp = i;
            if (maxp == mid) {
                int j;
                for (j = peakCount; j > 0; j--) {
                    if (data[maxp] <= data[peaks[j-1]])
                        break;
                    else if (j < peaks.length)
                        peaks[j] = peaks[j-1];
                }
                if (j != peaks.length)
                    peaks[j] = maxp;
                if (peakCount != peaks.length)
                    peakCount++;
            }
            mid++;
        }
        return peakCount;
    }

    public static int freqToIdx(float frequency, float freq_res) {
        return (int) (frequency / freq_res);
    }

    public static int[] freqToIdx(float[] frequencies, float freq_res) {
        int[] indexes = new int[frequencies.length];

        for (int i = 0; i < frequencies.length; i++) {
            indexes[i] = (int) (frequencies[i] / freq_res);
        }
        return indexes;
    }

    public static float idxToFreq(int index, float freq_res) {
        return freq_res * index;
    }

    public static float[] idxToFreq(int[] indexes, float freq_res) {
        float[] frequencies = new float[indexes.length];

        for (int i = 0; i < indexes.length; i++) {
            frequencies[i] = freq_res * indexes[i];
        }
        return frequencies;
    }


    // data input in frequency-domain with amplitudes (not in complex number representation)
    public static FrequencySpectrum trimOfRange(FrequencySpectrum spectrum, float from_freq, float to_freq) {
        float freq_res = spectrum.getFrequencyResolution();
        float[][] trimmed = trimOfRange(spectrum.getDataBuffer(), from_freq, to_freq, freq_res);
        return new FrequencySpectrum(
                "trimmed"+from_freq+"-"+to_freq+"_"+spectrum.getName(),
                trimmed,
                spectrum.getSampleRate(),
                spectrum.getFrequencyResolution(),
                ((int) (from_freq / freq_res)) * freq_res);
    }

    public static float[][] trimOfRange(float[][] fft_amp_data, float from_freq, float to_freq, float freq_res) {
        int idx_from = freqToIdx(from_freq, freq_res);
        int idx_to = freqToIdx(to_freq, freq_res);
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

    public static Signal trimOfRange(Signal signal, float from_freq, float to_freq) throws STFTException {
        if (signal.getDomain() != Signal.Domain.FREQUENCY_DOMAIN) {
            throw new STFTException("SIGNAL_DOMAIN_MISMATCH", "Signal must be in Frequency-domain", new IllegalArgumentException("fft_amp_data not match"));
        }
        float freq_res = signal.getFrequencyResolution();
        float[] trimmed = trimOfRange(signal.getData(), from_freq, to_freq, freq_res);
        if (signal instanceof SignalFFT)
            return new SignalFFT(
                    "trimmed"+from_freq+"-"+to_freq+"_"+signal.getName(),
                    trimmed,
                    signal.getSampleRate(),
                    signal.getFrequencyResolution(),
                    ((SignalFFT) signal).isNyquist(),
                    ((int) (from_freq / freq_res)) * freq_res
            );
        else return new Signal(
                "trimmed"+from_freq+"-"+to_freq+"_"+signal.getName(),
                trimmed,
                signal.getSampleRate(),
                Signal.Domain.FREQUENCY_DOMAIN
        );
    }

    public static float[] trimOfRange(float[] fft_amp_data, float from_freq, float to_freq, float freq_res){
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


    public static void normalizeEuclid(float[][] data) {
//        float[][]
    }

    public static void normalizeZeroOne(Spectrum spectrum) {
        normalizeZeroOne(spectrum.getDataBuffer());
    }
    public static void normalizeZeroOne(final float[][] data) {
        normalizeZeroOne(data, null, null);
    }

    public static void normalizeZeroOne(Spectrum spectrum, Float min, Float max) {
        normalizeZeroOne(spectrum.getDataBuffer(), min, max);
    }

    public static void normalizeZeroOne(final float[][] data, Float min, Float max) {
        int frameTotal = data.length;
        int n = data[0].length;
        float maxAmp = data[0][0], minAmp = maxAmp;
        if (min != null && max != null) {
            minAmp = min;
            maxAmp = max;
        }
        else {
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
        }
        float diff = maxAmp - minAmp;
        for (int i = 0; i < frameTotal; i++){
            for (int j = 0; j < n; j++){
                data[i][j] = (data[i][j]-minAmp)/diff;
            }
        }
    }

    public static void normalizeZeroOne(Signal signal) {
        normalizeZeroOne(signal.getData());
    }

    public static void normalizeZeroOne(float[] data) {
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

    public static void powerToDb(Spectrum spectrum) {
        powerToDb(spectrum.getDataBuffer());
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

    public static float[] avgSubtract(Signal signal) {
        return avgSubtract(signal.getData());
    }


    public static float[] avgSubtract(float[] signal) {
        SummaryStatistics stat = new SummaryStatistics();
        for (float s : signal) {
            stat.addValue(s);
        }
        float avg = (float) stat.getMean();
        float[] out = new float[signal.length];
        for (int i = 0; i < signal.length; i++) {
            out[i] = signal[i] - avg;
        }
        return out;
    }

    public static void powerToDb(Signal signal) {
        powerToDb(signal.getData());
    }

    public static void powerToDb(float[] data) {
        for (int i = 0; i < data.length; i++) {
            data[i] = (float) (10 * Math.log10(data[i]));
        }
    }

    public static void logCompress(Signal signal, double constant) {
        logCompress(signal.getData(), constant);
    }

    public static void logCompress(float[] data, double constant) {
        VectorUtils.mapFunc(data, (j) -> j * constant + 1);
        VectorUtils.mapFunc(data, Math::log);
    }

    public static void logCompress(Spectrum spectrum, double constant) { logCompress(spectrum.getDataBuffer(), constant); }

    public static void logCompress(final float[][] data, double constant) {
        int len_i = data.length, len_j = data[0].length;
        for (int i = 0; i < len_i; i++) {
            for (int j = 0; j < len_j; j++) {
                data[i][j] = (float) Math.log(data[i][j] * constant + 1);
            }
        }
    }


}
