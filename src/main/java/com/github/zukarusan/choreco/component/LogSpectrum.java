package com.github.zukarusan.choreco.component;

public class LogSpectrum {
    private LogFrequency logFrequency = LogFrequency.getInstance();



    public void resetLogFrequency(float reference_pitch) {
        logFrequency = logFrequency.reset(reference_pitch);
    }

}
