package com.github.zukarusan.choreco.component.spectrum.chroma;

import com.github.zukarusan.choreco.component.LogFrequency;
import com.github.zukarusan.choreco.component.chroma.CRP;
import com.github.zukarusan.choreco.component.spectrum.LogFrequencySpectrum;
import com.github.zukarusan.choreco.system.CommonProcessor;
import com.github.zukarusan.choreco.system.DCT_1D;
import com.github.zukarusan.choreco.util.PlotManager;
import com.github.zukarusan.choreco.util.VectorUtils;
import lombok.Getter;

import java.util.Arrays;

public class CRPSpectrum extends ChromaSpectrum {
    static float[] _BUFFER_DCT_ = new float[LogFrequency.PITCH_LENGTH];
    static DCT_1D dct = new DCT_1D(LogFrequency.PITCH_LENGTH);
    @Getter
    private final double logConstant;

    public CRPSpectrum(LogFrequencySpectrum logSpectrum, double logConstant) {
        super(logSpectrum);
        this.logConstant = logConstant;
        float[][] pitches = Arrays.copyOf(logSpectrum.getDataBuffer(), frameTotal);
        float[][] t_pitches = new float[frameTotal][LogFrequency.PITCH_LENGTH];

        CommonProcessor.logCompress(pitches, logConstant);

        for (int i = 0; i < frameTotal; i++) {
            dct.transform(pitches[i], _BUFFER_DCT_);
            for (int j = 0; j < CRP.p_reduction; j++) {
                _BUFFER_DCT_[j] = 0f;
            }
            dct.inverse(_BUFFER_DCT_, t_pitches[i]);
        }

        mapPitch(t_pitches);
        for (int i = 0; i < frameTotal; i++) {
            VectorUtils.normalizeVector(dataBuffer[i]);
        }
    }

    @Override
    public CRP getVectorAt(float second) {
        int idx = (int) (second * frequencyResolution);
        if (idx >= frameTotal)
            throw new IllegalArgumentException("Out of range index, maximum length: "+
                    String.format("%.2f", dataBuffer.length/frequencyResolution)+" seconds");
        return new CRP(this.dataBuffer[idx], logConstant);
    }


    @Override
    public void plot() {
        PlotManager plotManager = PlotManager.getInstance();
//        SignalProcessor.powerToDb(copy);
        float[][] copy = setPlot();
        CommonProcessor.normalizeZeroOne(copy, -1f, 1f);
        plotManager.createSpectrogram(this+name, copy);
    }
}
