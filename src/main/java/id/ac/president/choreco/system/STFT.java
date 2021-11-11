package id.ac.president.choreco.system;

import be.tarsos.dsp.util.fft.FFT;
import be.tarsos.dsp.util.fft.HannWindow;
import id.ac.president.choreco.component.Signal;
import id.ac.president.choreco.component.SignalFFT;
import id.ac.president.choreco.component.Spectrum;
import id.ac.president.choreco.system.exception.STFTException;
import lombok.Getter;

import java.util.Arrays;

public class STFT {
//    private final float[] signalBuffer; // need to develop to be buffer stream
    @Getter private final int frameSize;
    @Getter private final int hopSize;

    private FFT fft;
    private final static HannWindow hannWindow = new HannWindow();


    public STFT(int frameSize, int hopSize) throws STFTException {
        this.frameSize = frameSize;
        this.hopSize = hopSize;
    }

    public Spectrum process(Signal buffer, float sampleRate) throws STFTException { // Need to developed to be spectrum output buffer
        fft = new FFT(frameSize);
        float frequencyResolution = sampleRate / (frameSize * 2);
        float[] signalBuffer = buffer.getData();
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

            // Multiply by Hann window function
            windowFunc(frames);
            fft.complexForwardTransform(frames);
            fft.modulus(
                Arrays.copyOfRange(frames, 0, frameSize),
                bins[i]);

            for (int j = 0; j < binLength; j++) {
                bins[i][j] /= sampleRate;
            }

        }
        return new Spectrum("stftFrame"+frameSize, bins, sampleRate, frequencyResolution);
    }

    private static void windowFunc(float[] data) {
        float[] windows = hannWindow.generateCurve(data.length);
        for (int i = 0; i < data.length; i++) {
            data[i] = data[i] * windows[i];
        }
    }

    public Signal fftPower(Signal buffer, float sampleRate) {
        int length = buffer.getData().length;
        float frequencyResolution = sampleRate / (length * 2);
        fft = new FFT(length);
        float[] tempBuffer = new float[length * 2];

        System.arraycopy(
                buffer.getData(), 0,
            tempBuffer, 0,
            length
        );

        fft.complexForwardTransform(tempBuffer);
        int n = length;

        float[] bins = new float[n];
        fft.modulus(tempBuffer, bins);

        return new SignalFFT(
                "fftPower",
                Arrays.copyOfRange(bins, 0, (n/2)),
                sampleRate,
                frequencyResolution,
                true);
    }


}
