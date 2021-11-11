package com.github.zukarusan.choreco.component;

import com.github.zukarusan.choreco.util.PlotManager;
import lombok.Getter;

import java.util.Arrays;

public  class LogFrequency {
    private static volatile LogFrequency instance;
    private float[] frequencies;
    @Getter private final int LOG_LENGTH = 128;
    @Getter private float REFERENCE_PITCH = 440f;

    public static LogFrequency getInstance() {
        LogFrequency result = instance;
        if (result != null) {
            return result;
        }
        synchronized (PlotManager.class) {
            if (instance == null) {
                instance = new LogFrequency(440f);
            }
            return instance;
        }
    }

    private LogFrequency(float reference) {
        frequencies = new float[LOG_LENGTH * 2];
        int p = -69;
        for(int i = 0, j = 0; i < LOG_LENGTH; i++, j+=2, p++) {
            frequencies[j] = (float) Math.pow(2, ((double)(p) - 0.5)/12f) * reference;
            frequencies[j+1] = (float) Math.pow(2, ((double)(p) + 0.5)/12f) * reference;
        }
    }

    public float[] getPitchFrequencyRange(int pitch) {
        if (pitch < 0 || pitch > 127) {
            throw new IllegalArgumentException("Pitch is ranged between 0 to 127 (128 MIDI pitches)");
        }
        return new float[]{frequencies[pitch * 2], frequencies[pitch * 2 + 1]};
    }

    public boolean checkFreq(float freq_coefficient, int pitch) {
        if (pitch < 0 || pitch > 127) {
            throw new IllegalArgumentException("Pitch is ranged between 0 to 127 (128 MIDI pitches)");
        }
        int idx = pitch * 2;
        return freq_coefficient >= frequencies[idx] && freq_coefficient <= frequencies[idx+1];
    }

    public int searchPitch(float freq_coefficient) {
        return searchPitch(freq_coefficient, 0, LOG_LENGTH-1);
    }

    public int searchPitch(float freq_coefficient, int fromPitch) {
        return searchPitch(freq_coefficient, fromPitch, LOG_LENGTH-1);
    }

    public int searchPitch(float freq_coefficient, int fromPitch, int toPitch) {
        if (freq_coefficient <= frequencies[0]) { return 0; }
        if (freq_coefficient >= frequencies[LOG_LENGTH * 2 - 1]) { return LOG_LENGTH-1; }

        int result = Arrays.binarySearch(frequencies, fromPitch*2, toPitch*2+1, freq_coefficient);
        if (result >= 0) { return result/2; }

        int insertionPoint = -result - 1;
        if (insertionPoint % 2 == 0) { return insertionPoint / 2; }
        else { return
                // the closest value between edge of the 2 range (+.5 lower & -.5 higher)
                (Math.abs(freq_coefficient - frequencies[insertionPoint-1])
                        < Math.abs(freq_coefficient - frequencies[insertionPoint]))
                        ? (insertionPoint-1)/2 : insertionPoint/2; }
    }

    public LogFrequency reset(float reference_pitch) {
        synchronized (PlotManager.class) {
            instance = null;
            instance = new LogFrequency(reference_pitch);
        }
        REFERENCE_PITCH = reference_pitch;
        return instance;
    }


}
