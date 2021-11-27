package com.github.zukarusan.choreco.component;

import com.github.zukarusan.choreco.system.SignalProcessor;
import com.github.zukarusan.choreco.util.PlotManager;
import lombok.Getter;

public class LogFrequencyVector {
    @Getter private float[] power;
    public static final float PEAK_THRESHOLD = 5f;

    public LogFrequencyVector(float[] pitches_power) {
        if (pitches_power.length != LogFrequency.PITCH_LENGTH) {
            throw new IllegalArgumentException("This constructor accepts power values of 128 MIDI pitches");
        }
        this.power = pitches_power;
    }

    public LogFrequencyVector(SignalFFT signal) {
        float[] trimmed = signal.getData();
        float offset = signal.getOffset();
        float frequencyResolution = signal.frequencyResolution;

        if (signal.getOffset() < 25f){
            trimmed = SignalProcessor.trimOfRange(signal.getData(), 25f, 5000f, frequencyResolution);
            offset = ((int) (25f / frequencyResolution)) * frequencyResolution;
        }
        power = new float[LogFrequency.PITCH_LENGTH];
        int[] peaks = SignalProcessor.peakDetection(trimmed, PEAK_THRESHOLD);
        int pitch = 0, total = 0;
        float sum = 0;
        for (int peak : peaks) {
            float freq = peak * frequencyResolution + offset;
            LogFrequency logFrequency = LogFrequency.getInstance();
            if (logFrequency.checkFreq(freq, pitch)) {
                sum += trimmed[peak];
                total++;
                continue;
            }

            if (total!=0) this.power[pitch] = sum / total;
            pitch = logFrequency.searchPitch(freq, pitch);
            total = 1;
            sum = trimmed[peak];
        }
    }

    public void plot() {
        PlotManager plotManager = PlotManager.getInstance();
        plotManager.createPlot("LogFrequencyVector", "Pitch", power);
    }
}
