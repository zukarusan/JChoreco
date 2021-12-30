package com.github.zukarusan.choreco.component.spectrum;

import com.github.zukarusan.choreco.component.SignalFFT;
import com.github.zukarusan.choreco.system.CommonProcessor;
import com.github.zukarusan.choreco.util.PlotManager;
import lombok.Getter;

import java.util.Arrays;

public class FrequencySpectrum extends Spectrum {

    @Getter
    private final float offset;

    public FrequencySpectrum(String name, float[][] fft_data, float sampleRate, float freq_res, float offsetFrequency) {
        super(name, sampleRate, freq_res);
        this.dataBuffer = fft_data;
        this.frameTotal = fft_data.length;
        this.frameLength = fft_data[0].length;
        if (offsetFrequency < 0) throw new IllegalArgumentException("Offset should be positive value");
        else this.offset = offsetFrequency;
    }

    public float[] getFrequenciesLabel() {
        float[] frequencies = new float[frameLength];
        for(int i = 0; i < frameLength; i++) frequencies[i] = i * frequencyResolution + offset;
        return frequencies;
    }

    // get fft-analyzed frequencies at given time
    public SignalFFT getSignalAt(float second){
        int idx = (int) (second * frequencyResolution);
        if (idx >= dataBuffer.length)
            throw new IllegalArgumentException("Out of range index, maximum length: "+
                    String.format("%.2f", dataBuffer.length/frequencyResolution)+" seconds");
        return new SignalFFT(
                "SignalAt"+String.format("%.1f",second)+"_"+name,
                dataBuffer[idx],
                sampleRate,
                frequencyResolution,
                true,
                offset);
    }

    public void plot() {
        PlotManager plotManager = PlotManager.getInstance();
        float[][] copy = Arrays.copyOf(dataBuffer, dataBuffer.length);
        CommonProcessor.normalizeZeroOne(copy);
        plotManager.createSpectrogram(name, copy);
    }

}
