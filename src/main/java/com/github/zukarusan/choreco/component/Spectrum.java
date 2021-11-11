package com.github.zukarusan.choreco.component;

import lombok.Getter;
import lombok.Setter;

@Getter
public class Spectrum {
    private final float[][] dataBuffer; // need to develop to be buffer stream
    private final Signal.Domain domain = Signal.Domain.FREQUENCY_DOMAIN;
    @Setter private String name;
    private final float sampleRate;
    private final float frequencyResolution;
    private final int frameLength;
    private final int frameTotal;

    public Spectrum(String name, float[][] data, float sampleRate, float freq_res) {
        this.name = name;
        this.dataBuffer = data;
        this.sampleRate = sampleRate;
        this.frequencyResolution = freq_res;
        this.frameTotal = data.length;
        this.frameLength = data[0].length;
    }


    // get fft-analyzed frequencies at given time
    public Signal getSignalAt(float second){
        int idx = (int) (second * frequencyResolution);
        if (idx >= dataBuffer.length)
            throw new IllegalArgumentException("Out of range index, maximum length: "+
                    String.format("%.2f", dataBuffer.length/frequencyResolution)+" seconds");
        return new SignalFFT(
                "SignalAt"+String.format("%.1f",second)+"_"+name,
                dataBuffer[idx],
                sampleRate,
                frequencyResolution,
                true);
    }

}
