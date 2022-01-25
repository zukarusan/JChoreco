package com.github.zukarusan.jchoreco.component;

import lombok.Getter;

import java.util.Arrays;

public class SignalFFT extends Signal {
    @Getter private final boolean isNyquist; // is the data frequency half of the rate
    @Getter private final float offset;
    public SignalFFT(String labelName, float[] data, float sampleRate, float frequencyResolution, boolean isNyquist, float offsetFrequency) {
        super(labelName, data, sampleRate, Domain.FREQUENCY_DOMAIN);
        this.frequencyResolution = frequencyResolution;
        this.isNyquist = isNyquist;
        if (offsetFrequency < 0)
            throw new IllegalArgumentException("Offset should be positive value");
        this.offset = offsetFrequency;
    }

    public float[] getDataNyquist() {
        if (isNyquist) { return getData(); }

        float[] data = getData();
        return Arrays.copyOf(data, data.length/2);
    }

    @Override
    public float getFrequencyResolution() {
        return this.frequencyResolution;
    }
}
