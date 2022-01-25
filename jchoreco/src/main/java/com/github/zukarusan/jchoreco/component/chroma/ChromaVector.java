package com.github.zukarusan.jchoreco.component.chroma;

import com.github.zukarusan.jchoreco.component.LogFrequency;
import com.github.zukarusan.jchoreco.util.PlotManager;
import lombok.Getter;

public abstract class ChromaVector {

    @Getter
    protected final float[] power;

    public ChromaVector() {
        this.power = new float[Chroma.CHROMATIC_LENGTH];
    }

    public ChromaVector(float[] power) {
        if (power.length != Chroma.CHROMATIC_LENGTH)
            throw new IllegalCallerException("Must be chroma vector");
        this.power = power;
    }

    protected void mapPitch(float[] pitches) {
        mapPitch(pitches, this.power);
    }

    protected static void mapPitch(float[] pitches, final float[] out_mapped) {
        assert pitches.length == LogFrequency.PITCH_LENGTH;
        for (int i = 0; i < LogFrequency.PITCH_LENGTH; i++) {
            out_mapped[i % 12] += pitches[i];
        }
    }

    public void plot() {
        PlotManager plotManager = PlotManager.getInstance();
        plotManager.createPlot("ChromaVector", "Chroma", power);
    }

}
