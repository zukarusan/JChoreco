package com.github.zukarusan.choreco.component.spectrum.chroma;

import com.github.zukarusan.choreco.component.chroma.CP;
import com.github.zukarusan.choreco.component.spectrum.LogFrequencySpectrum;
import com.github.zukarusan.choreco.util.VectorUtils;

public class CPSpectrum extends ChromaSpectrum{
    public CPSpectrum(LogFrequencySpectrum logSpectrum) {
        super(logSpectrum);
        mapPitch(logSpectrum.getDataBuffer());

        for (int i = 0; i < frameTotal; i++) {
            VectorUtils.normalizeVector(dataBuffer[i]);
        }
    }

    @Override
    public CP getVectorAt(float second) {
        int idx = (int) (second * frequencyResolution);
        if (idx >= frameTotal)
            throw new IllegalArgumentException("Out of range index, maximum length: "+
                    String.format("%.2f", dataBuffer.length/frequencyResolution)+" seconds");
        return new CP(this.dataBuffer[idx]);
    }
}
