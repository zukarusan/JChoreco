package com.github.zukarusan.jchoreco.component.spectrum.chroma;

import com.github.zukarusan.jchoreco.component.chroma.CLP;
import com.github.zukarusan.jchoreco.component.spectrum.LogFrequencySpectrum;
import com.github.zukarusan.jchoreco.system.CommonProcessor;
import com.github.zukarusan.jchoreco.util.VectorUtils;
import lombok.Getter;

import java.util.Arrays;

public class CLPSpectrum extends ChromaSpectrum{

    @Getter
    private final double logConstant;

    public CLPSpectrum(LogFrequencySpectrum logSpectrum, double logConstant) {
        super(logSpectrum);
        this.logConstant = logConstant;
        float[][] pitches = Arrays.copyOf(logSpectrum.getDataBuffer(), frameTotal);

        CommonProcessor.logCompress(pitches, logConstant);

        mapPitch(pitches);
        for (int i = 0; i < frameTotal; i++) {
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
