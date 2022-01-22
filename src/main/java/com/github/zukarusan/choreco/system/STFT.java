package com.github.zukarusan.choreco.system;

import be.tarsos.dsp.util.fft.*;
import com.github.zukarusan.choreco.component.Signal;
import com.github.zukarusan.choreco.component.SignalFFT;
import com.github.zukarusan.choreco.component.spectrum.FrequencySpectrum;
import com.github.zukarusan.choreco.system.exception.STFTException;
import lombok.Getter;
import org.jtransforms.fft.FloatFFT_1D;

import java.util.Arrays;



public class STFT {
//    private final float[] signalBuffer; // need to develop to be buffer stream
    @Getter private final int frameSize;
    @Getter private final int hopSize;

    public static enum WINDOW {
        Cosine,
        Gauss,
        Hamming,
        Hann,
        Rectangular,
        Triangular
    }

    private final FloatFFT_1D fft;
    private final float[] window_data;

    public STFT(int frameSize, int hopSize) {
        this.frameSize = frameSize;
        this.hopSize = hopSize;
        window_data = createWindowCurve(WINDOW.Hamming, frameSize);
        fft = new FloatFFT_1D(frameSize);
    }

    public FrequencySpectrum process(Signal buffer, float sampleRate) throws STFTException { // Need to developed to be spectrum output buffer
        float frequencyResolution = sampleRate / (frameSize);
        float[] signalBuffer = CommonProcessor.avgSubtract(buffer);

        if (buffer.getDomain() != Signal.Domain.TIME_DOMAIN) {
            throw new STFTException("CONSTRUCT_ERROR", "Time domain expected");
        }
        int frameTotal = (signalBuffer.length - frameSize) / hopSize + 1;
        int binLength = frameSize / 2;
        float[][] bins = new float[frameTotal][binLength];
        float[] frames;
        for (int i = 0, hop = 0; i < frameTotal; i++, hop+= hopSize) {
            // copy frames to process
            frames = new float[frameSize * 2];
            System.arraycopy(
                signalBuffer, hop,
                frames, 0,
                frameSize
            );

            // Multiply by Hamming window function
            windowFunc(frames);
            fft.realForward(frames);
            modulus(frames, bins[i]);

//            for (int j = 0; j < binLength; j++) {
//                bins[i][j] /= sampleRate;
//            }

        }
        return new FrequencySpectrum("stftFrame"+frameSize, bins, sampleRate, frequencyResolution, 0);
    }

    public static void modulus(float[] complex_data, float[] output_amplitudes) {
        if (output_amplitudes.length > complex_data.length/2)
            throw new IllegalArgumentException("Output length must not greater than complex_data length / 2");
        for(int i = 0, j = 0; i < output_amplitudes.length; ++i, ++j) {
            float real = complex_data[j];
            float imaginary = complex_data[++j];
            output_amplitudes[i] = (float) Math.sqrt(real*real + imaginary*imaginary);
        }
    }

    public static float[] createWindowCurve(WINDOW type, int size) {
        WindowFunction wFunc;
        switch (type) {
            case Cosine:
                wFunc = new CosineWindow();
                break;
            case Gauss:
                wFunc = new GaussWindow();
                break;
            case Hamming:
                wFunc = new HammingWindow();
                break;
            case Hann:
                wFunc = new HannWindow();
                break;
            case Rectangular:
                wFunc = new RectangularWindow();
                break;
            case Triangular:
                wFunc = new TriangularWindow();
                break;
            default:
                throw new IllegalStateException("Unexpected window type" + type);
        }
        return wFunc.generateCurve(size);
    }

    public void windowFunc(float[] data) {
        for (int i = 0; i < frameSize; i++) {
            data[i] = data[i] * window_data[i];
        }
    }

    public static void windowFunc(float[] data, WINDOW type) {
        float[] windows = createWindowCurve(type, data.length);
        for (int i = 0; i < data.length; i++) {
            data[i] = data[i] * windows[i];
        }
    }

    public static SignalFFT fftPower(Signal buffer, float sampleRate) {
        float[] bufferFloat = buffer.getData();
        float frequencyResolution = sampleRate / (bufferFloat.length);
        float[] fftBuffer = new float[bufferFloat.length/2];
        fftPower(bufferFloat, fftBuffer);
        return new SignalFFT(
                "fftPower",
                fftBuffer,
                sampleRate,
                frequencyResolution,
                true,
                0);
    }

    public static void fftPower(final float[] buffer, final float[] out) {
        assert out.length == buffer.length/2;
        int length = buffer.length;
        FloatFFT_1D fft = new FloatFFT_1D(length);
        float[] tempBuffer = new float[length * 2];

        System.arraycopy(
            buffer, 0,
            tempBuffer, 0,
            length
        );

        fft.realForward(tempBuffer);

        float[] bins = new float[length];
        modulus(tempBuffer, bins);

        System.arraycopy(
                bins, 0,
                out, 0,
                (length/2)
        );
    }


}
