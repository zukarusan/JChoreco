package com.github.zukarusan.choreco.component.spectrum.chroma;

import com.github.zukarusan.choreco.component.chroma.CLP;
import com.github.zukarusan.choreco.component.chroma.CP;
import com.github.zukarusan.choreco.component.spectrum.LogFrequencySpectrum;
import com.github.zukarusan.choreco.util.VectorUtils;
import lombok.Getter;

import java.util.Arrays;

public class CLPSpectrum extends ChromaSpectrum{

    @Getter
    private final double logConstant;

    public CLPSpectrum(LogFrequencySpectrum logSpectrum, double logConstant) {
        super(logSpectrum);
        this.logConstant = logConstant;
        float[][] pitches = Arrays.copyOf(logSpectrum.getDataBuffer(), frameTotal);

        for (int i = 0; i < frameTotal; i++) {
            VectorUtils.mapFunc(pitches[i], (j) -> j * logConstant + 1);
            VectorUtils.mapFunc(pitches[i], Math::log);
            mapPitch(pitches);
            VectorUtils.normalizeVector(dataBuffer[i]);
        }
    }

    @Override
    public CLP getVectorAt(float second) {
        int idx = (int) (second * frequencyResolution);
        if (idx >= frameTotal)
            throw new IllegalArgumentException("Out of range index, maximum length: "+
                    String.format("%.2f", dataBuffer.length/frequencyResolution)+" seconds");
        return new CLP(this.dataBuffer[idx], logConstant);
    }
}
