package com.github.zukarusan.jchoreco.component.spectrum.chroma;

import com.github.zukarusan.jchoreco.component.chroma.Chroma;
import com.github.zukarusan.jchoreco.component.LogFrequency;
import com.github.zukarusan.jchoreco.component.chroma.ChromaVector;
import com.github.zukarusan.jchoreco.component.spectrum.LogFrequencySpectrum;
import com.github.zukarusan.jchoreco.component.spectrum.Spectrum;
import com.github.zukarusan.jchoreco.system.CommonProcessor;
import com.github.zukarusan.jchoreco.util.PlotManager;

import java.util.Arrays;

public abstract class ChromaSpectrum extends Spectrum {

    public ChromaSpectrum(LogFrequencySpectrum logSpectrum) {
        super(logSpectrum.getName(), logSpectrum.getSampleRate(), logSpectrum.getFrequencyResolution());
        this.frameTotal = logSpectrum.getFrameTotal();
        this.frameLength = Chroma.CHROMATIC_LENGTH;
        dataBuffer = new float[frameTotal][frameLength];
    }

    protected void mapPitch(float[][] pitchFrames) {
        for (int i = 0; i < frameTotal; i++) {
            for (int j = 0; j < LogFrequency.PITCH_LENGTH; j++) {
                dataBuffer[i][j % Chroma.CHROMATIC_LENGTH] += pitchFrames[i][j];
            }
        }
    }

    public abstract ChromaVector getVectorAt(float second);

    protected float[][] setPlot() {
        int size = (int) ((PlotManager.HEIGHT / 2 - PlotManager.HEIGHT / 2 * 0.1) / (frameLength));
        float[][] copy = new float[frameTotal][size * frameLength];
        for (int i = 0; i < frameTotal; i++) {
            for (int j = 0, k = 0; j < frameLength; j++, k+=size) {
                Arrays.fill(copy[i], k, k+size, dataBuffer[i][j]);
            }
        }
        return copy;
    }

    public void plot() {
        PlotManager plotManager = PlotManager.getInstance();
        float[][] copy = setPlot();
//        SignalProcessor.powerToDb(copy);
        CommonProcessor.normalizeZeroOne(copy, 0f, 1f);
        plotManager.createSpectrogram(this+name, copy);
    }
}
