package com.github.zukarusan.choreco.component;

import com.github.zukarusan.choreco.util.PlotManager;
import lombok.Getter;
import lombok.Setter;

@Getter
public class Signal {

    public static final class Domain {
        private final int domain;

        private Domain(int domain) { this.domain = domain; }

        public static final Domain FREQUENCY_DOMAIN = new Domain(0);
        public static final Domain TIME_DOMAIN = new Domain(1);

        @Override
        public String toString() {
            return Integer.toString(domain);
        }
    }

    @Setter
    private String name;
    private final Domain domain;
    private final float[] data; // need to develop to be buffer stream
    private final float sampleRate;
    private final float totalSecond;
    protected float frequencyResolution;

    public Signal(String labelName, float[] data, float sampleRate, Domain domain)  {
        this.data = data;
        this.domain = domain;
        this.name = labelName;
        this.sampleRate = sampleRate;
        this.totalSecond = (float)(data.length) / sampleRate;
        this.frequencyResolution = sampleRate / (data.length * 2); // not accurate, use SignalFFT
    }

    public float getFrequencyResolution() {
        if (domain != Domain.FREQUENCY_DOMAIN)
            throw new IllegalCallerException("Not A frequency domain");
        return frequencyResolution;
    }

    public void plot() {
        PlotManager plotManager = PlotManager.getInstance();

        plotManager.createPlot(
                name,
                "Frequencies",
                data);
    }

}

