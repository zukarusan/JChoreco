package com.github.zukarusan.choreco.component.spectrum.chroma;

import com.github.zukarusan.choreco.component.LogFrequencyVector;
import com.github.zukarusan.choreco.component.chroma.Chroma;
import com.github.zukarusan.choreco.component.LogFrequency;
import com.github.zukarusan.choreco.component.chroma.ChromaVector;
import com.github.zukarusan.choreco.component.spectrum.LogFrequencySpectrum;
import com.github.zukarusan.choreco.component.spectrum.Spectrum;
import com.github.zukarusan.choreco.system.SignalProcessor;
import com.github.zukarusan.choreco.util.PlotManager;
import com.github.zukarusan.choreco.util.VectorUtils;

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

    public void plot() {
        PlotManager plotManager = PlotManager.getInstance();
        int size = (int) ((PlotManager.HEIGHT - PlotManager.HEIGHT * 0.1) / (frameLength));
        float[][] copy = new float[frameTotal][size * frameLength];
        for (int i = 0; i < frameTotal; i++) {
            for (int j = 0, k = 0; j < frameLength; j++, k+=size) {
                Arrays.fill(copy[i], k, k+size, dataBuffer[i][j]);
            }
        }
//        SignalProcessor.powerToDb(copy);
        SignalProcessor.normalizeZeroOne(copy);
        plotManager.createSpectrogram(name, copy);
    }
}
