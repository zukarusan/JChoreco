package com.github.zukarusan.choreco.system;

import com.github.zukarusan.choreco.component.LogFrequencyVector;
import com.github.zukarusan.choreco.component.Signal;
import com.github.zukarusan.choreco.component.SignalFFT;
import com.github.zukarusan.choreco.component.chroma.CRP;
import com.github.zukarusan.choreco.component.chroma.Chroma;

public final class CRPVectorFactory {
    static final double LOG_CONSTANT = 100;
    public static CRP from_signal(Signal signal) {
        assert signal != null;
        SignalFFT fft = STFT.fftPower(signal, signal.getSampleRate());
        CommonProcessor.logCompress(fft, LOG_CONSTANT);
        return new CRP(new LogFrequencyVector(fft), LOG_CONSTANT);
    }

    public static float[] from_floatSamples(float[] audioSamples, float sampleRate) {
        int[] freqMaps = LogFrequencyVector.createFreqMaps(new float[audioSamples.length/2], sampleRate);
        return from_floatSamples(audioSamples, sampleRate, freqMaps);
    }

    public static float[] from_floatSamples(float[] audioSamples, float sampleRate, int[] freqMaps) {
        assert audioSamples != null;
        float[] fft = new float[audioSamples.length/2];
        STFT.fftPower(audioSamples, fft);
        CommonProcessor.logCompress(fft, LOG_CONSTANT);
        float[] crp = new float[Chroma.CHROMATIC_LENGTH];
        LogFrequencyVector.process(fft, freqMaps, sampleRate, audioSamples.length, 0, crp);
        return crp;
    }

}
