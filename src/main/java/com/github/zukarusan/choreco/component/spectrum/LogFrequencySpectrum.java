package com.github.zukarusan.choreco.component.spectrum;

import com.github.zukarusan.choreco.component.LogFrequency;
import com.github.zukarusan.choreco.component.LogFrequencyVector;
import com.github.zukarusan.choreco.component.Signal;
import com.github.zukarusan.choreco.system.SignalProcessor;
import com.github.zukarusan.choreco.util.PlotManager;
import lombok.Getter;

import java.util.Arrays;

public class LogFrequencySpectrum extends Spectrum{

    public static final float PEAK_THRESHOLD = 4f;

    protected final Signal.Domain domain = Signal.Domain.FREQUENCY_DOMAIN;


    public LogFrequencySpectrum(FrequencySpectrum spectrum) {
        super("Log-"+spectrum.name, spectrum.sampleRate, spectrum.frequencyResolution);

        float[][] fft_data = spectrum.getDataBuffer();
        float[][] trimmed = fft_data;
        float offset = spectrum.getOffset();
        if (offset < 25f){
            trimmed = SignalProcessor.trimOfRange(fft_data, 25f, 5000f, frequencyResolution);
            offset = ((int) (25f / frequencyResolution)) * frequencyResolution;
        }
        float[][] logBuffer = new float[fft_data.length][LogFrequency.PITCH_LENGTH];
        for (int i = 0; i < fft_data.length; i++) {
            int[] peaks = SignalProcessor.peakDetection(trimmed[i], PEAK_THRESHOLD);
            int pitch = 0, total = 0;
            float sum = 0;
            for (int peak : peaks) {
                float freq = peak * frequencyResolution + offset;
                LogFrequency logFrequency = LogFrequency.getInstance();
                if (logFrequency.checkFreq(freq, pitch)) {
                    sum += trimmed[i][peak];
                    total++;
                    continue;
                }

                if (total!=0) logBuffer[i][pitch] = sum / total;
                pitch = logFrequency.searchPitch(freq, pitch);
                total = 1;
                sum = trimmed[i][peak];
            }
        }

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
        SignalProcessor.normalizeZeroOne(copy);
        plotManager.createSpectrogram(name, copy);
    }

//    public void resetLogFrequency(float reference_pitch) {
//        logFrequency = logFrequency.reset(reference_pitch);
//    }

}
