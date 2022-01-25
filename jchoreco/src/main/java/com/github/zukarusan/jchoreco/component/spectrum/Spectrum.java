package com.github.zukarusan.jchoreco.component.spectrum;

import com.github.zukarusan.jchoreco.component.Signal;
import lombok.Getter;
import lombok.Setter;

@Getter
public abstract class Spectrum {
    protected float[][] dataBuffer; // need to develop to be buffer stream
    protected final Signal.Domain domain = Signal.Domain.FREQUENCY_DOMAIN;
    @Setter
    protected String name;
    protected final float sampleRate;
    protected final float frequencyResolution;
    protected int frameLength;
    protected int frameTotal;

    public Spectrum(String name, float sampleRate, float freq_res) {
        this.name = name;
        this. sampleRate = sampleRate;
        this.frequencyResolution = freq_res;
    }
}
