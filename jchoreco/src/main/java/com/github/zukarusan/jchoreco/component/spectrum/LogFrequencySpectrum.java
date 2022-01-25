package com.github.zukarusan.jchoreco.component.spectrum;

import com.github.zukarusan.jchoreco.component.LogFrequency;
import com.github.zukarusan.jchoreco.component.LogFrequencyVector;
import com.github.zukarusan.jchoreco.component.Signal;
import com.github.zukarusan.jchoreco.system.CommonProcessor;
import com.github.zukarusan.jchoreco.util.PlotManager;

import java.util.Arrays;

public class LogFrequencySpectrum extends Spectrum{

    public static final float PEAK_THRESHOLD =2.5f;
    public static final int HARMONIC_SIZE = 12;

    protected final Signal.Domain domain = Signal.Domain.FREQUENCY_DOMAIN;


    public LogFrequencySpectrum(FrequencySpectrum spectrum) {
        super("Log-"+spectrum.name, spectrum.sampleRate, spectrum.frequencyResolution);

        float[][] fft_data = spectrum.getDataBuffer();
        float[][] trimmed = fft_data;
        float offset = spectrum.getOffset();
        if (offset < 25f){
            trimmed = CommonProcessor.trimOfRange(fft_data, 25f, 5000f, frequencyResolution);
            offset = ((int) (25f / frequencyResolution)) * frequencyResolution;
        }

        float[][] logBuffer = new float[fft_data.length][LogFrequency.PITCH_LENGTH];

        int[] fMaps = LogFrequency.createFrequencyMap(fft_data[0].length * 2, spectrum.sampleRate);
        int off_idx = CommonProcessor.freqToIdx(offset, frequencyResolution);
        for (int i = 0; i < fft_data.length; i++) {
            int[] peaks = new int[8]; // pick 8 highest frequency peaks
            int found = CommonProcessor.findPeaksByExtremePoints(trimmed[i], peaks, 4);
//            int[] peaks = CommonProcessor.findPeaksByAverage(trimmed[i], PEAK_THRESHOLD);
            int total = 1;
            for (int j = 0; j < found; j++) {
                int f_idx = off_idx + peaks[j];
                int l_idx = fMaps[f_idx];
                logBuffer[i][l_idx] += fft_data[i][f_idx];
                if (j+1 < found) {
                    if (peaks[j] != peaks[j+1]) {
                        logBuffer[i][l_idx] /= total;
                        total = 1;
                    } else total++;
                } else logBuffer[i][l_idx] /= total;
            }
        }

        /*for (int i = 0; i < fft_data.length; i++) {
//            int[] peaks = SignalProcessor.findPeaksByAverage(trimmed[i], PEAK_THRESHOLD);
            int[] peaks = new int[8]; // pick 6 highest frequency peaks
            int found = SignalProcessor.findPeaksByExtremePoints(trimmed[i], peaks, 4);
            int pitch = 0, total = 0;
            float sum = 0;
            for (int j = 0; j < found; j++) {
                float freq = peaks[j] * frequencyResolution + offset;
                LogFrequency logFrequency = LogFrequency.getInstance();
                if (logFrequency.checkFreq(freq, pitch)) {
                    sum += trimmed[i][peaks[j]];
                    total++;
                    continue;
                }

                if (total != 0) logBuffer[i][pitch] = sum / total;
                pitch = logFrequency.searchPitch(freq, pitch);
                total = 1;
                sum = trimmed[i][peaks[j]];
            }
        }*/

        this.dataBuffer = logBuffer;
        this.frameTotal = fft_data.length;
        this.frameLength = LogFrequency.PITCH_LENGTH;
    }

    public LogFrequencyVector getVectorAt(float second) {
        int idx = (int) (second * frequencyResolution);
        if (idx >= dataBuffer.length)
            throw new IllegalArgumentException("Out of range index, maximum length: "+
                    String.format("%.2f", dataBuffer.length/frequencyResolution)+" seconds");
        return new LogFrequencyVector(this.dataBuffer[idx]);
    }


    public void plot() {
        PlotManager plotManager = PlotManager.getInstance();
        int size = (int) ((PlotManager.HEIGHT /*- PlotManager.HEIGHT * 0.1*/) / (frameLength-21)); // begin from pitch 21
        float[][] copy = new float[frameTotal][size * (frameLength-21)];
        for (int i = 0; i < frameTotal; i++) {
            for (int j = 21, k = 0; j < frameLength; j++, k+=size) { // from pitch 21, A0
                Arrays.fill(copy[i], k, k+size, dataBuffer[i][j]);
            }
        }
//        SignalProcessor.powerToDb(copy);
        CommonProcessor.normalizeZeroOne(copy);
        plotManager.createSpectrogram(name, copy);
    }

//    public void resetLogFrequency(float reference_pitch) {
//        logFrequency = logFrequency.reset(reference_pitch);
//    }

}
