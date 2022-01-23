package com.github.zukarusan.choreco.component;

import com.github.zukarusan.choreco.util.PlotManager;

import java.util.Arrays;

public final class LogFrequency {
    private static volatile LogFrequency instance;
    private final float[] frequencies;
    public static final float REFERENCE_FREQUENCY = 440f;
    public static final int PITCH_LENGTH = 128;

    public static LogFrequency getInstance() {
        LogFrequency result = instance;
        if (result != null) {
            return result;
        }
        synchronized (PlotManager.class) {
            if (instance == null) {
                instance = new LogFrequency(REFERENCE_FREQUENCY);
            }
            return instance;
        }
    }

    private LogFrequency(float reference) {
        frequencies = new float[PITCH_LENGTH];
        int p = -69;
        for(int i = 0; i < PITCH_LENGTH; i++, p++) {
            frequencies[i] = (float) Math.pow(2, ((double)(p) - 0.5)/12f) * reference;
        }
    }

    public float[] getPitchFrequencyRange(int pitch) {
        if (pitch < 0 || pitch > 127) {
            throw new IllegalArgumentException("Pitch is ranged between 0 to 127 (128 MIDI pitches)");
        }
        return new float[]{frequencies[pitch], frequencies[pitch + 1]};
    }

    public boolean checkFreq(float freq_coefficient, int pitch) {
        if (pitch < 0 || pitch > 127) {
            throw new IllegalArgumentException("Pitch is ranged between 0 to 127 (128 MIDI pitches)");
        }
        return freq_coefficient >= frequencies[pitch] && freq_coefficient < frequencies[pitch+1];
    }

    public int searchPitch(float freq_coefficient) {
        return searchPitch(freq_coefficient, 0, PITCH_LENGTH -1);
    }

    public int searchPitch(float freq_coefficient, int fromPitch) {
        return searchPitch(freq_coefficient, fromPitch, PITCH_LENGTH -1);
    }

    public int searchPitch(float freq_coefficient, int fromPitch, int toPitch) {
        if (freq_coefficient <= frequencies[0]) { return 0; }
        if (freq_coefficient >= frequencies[PITCH_LENGTH - 1]) { return PITCH_LENGTH -1; }

        int result = Arrays.binarySearch(frequencies, fromPitch, toPitch, freq_coefficient);
        if (result >= 0) { return result; }

        int insertionPoint = -result - 1;
        return insertionPoint - 1; // Doesn't concern first index since the case has been handled
//        if (insertionPoint % 2 == 1) { return insertionPoint / 2; }
//        else { return (insertionPoint-1)/2; }
                // the closest value between edge of the 2 range (+.5 lower & -.5 higher)
//                (Math.abs(freq_coefficient - frequencies[insertionPoint-1])
//                        < Math.abs(freq_coefficient - frequencies[insertionPoint]))
//                        ? (insertionPoint-1)/2 : insertionPoint/2; }
    }

    public static int[] createFrequencyMap(int fft_size, float sample_rate) {
        int length = fft_size/2+1;
        int[] maps = new int[length];
        final float midi_0 = (float) (REFERENCE_FREQUENCY * Math.pow(2, -69/12f));
        float freq_res = sample_rate / fft_size;

        float f = 0;
        int i = 0;
        for ( ; f < midi_0; ++i, f+=freq_res) {
            if (i > length) throw new IllegalStateException("fft size too small");
            maps[i] = 0;
        }

        for ( ; i < length; ++i, f+=freq_res) {
            maps[i] = (int) Math.round((Math.log(f/REFERENCE_FREQUENCY) / Math.log(2) * 12f + 69));
            if (maps[i] > 127)
                maps[i] = 127;
        }

        return maps;
    }

//    public LogFrequency reset(float reference_pitch) {
//        synchronized (PlotManager.class) {
//            instance = null;
//            instance = new LogFrequency(reference_pitch);
//        }
//        referenceFrequency = reference_pitch;
//        return instance;
//    }


}
