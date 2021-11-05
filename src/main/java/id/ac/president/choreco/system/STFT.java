package id.ac.president.choreco.system;

import be.tarsos.dsp.util.fft.FFT;
import be.tarsos.dsp.util.fft.HannWindow;
import id.ac.president.choreco.component.Signal;
import id.ac.president.choreco.system.exception.STFTException;
import lombok.Getter;

import java.util.Arrays;

public class STFT {
    private final float[] signalBuffer;
    @Getter private final int frameSize;
    @Getter private final int hopSize;
    @Getter private final float sampleRate;
    @Getter private final float frequencyResolution;
    @Getter private final float frequencyResolutionFFT;

    private FFT fft;
    private final static HannWindow hannWindow = new HannWindow();


    public STFT(Signal buffer, int frameSize, int hopSize, float sampleRate) throws STFTException {
        if (buffer.getDomain() != Signal.Domain.TIME_DOMAIN) {
            throw new STFTException("CONSTRUCT_ERROR", "Time domain expected");
        }
        this.signalBuffer = buffer.getData();
        this.frameSize = frameSize;
        this.hopSize = hopSize;
        this.sampleRate = sampleRate;
        this.frequencyResolution = sampleRate / frameSize;
        this.frequencyResolutionFFT = sampleRate / buffer.getData().length;
    }

    public float[][] process() {
        fft = new FFT(frameSize);
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
        return bins;
    }

    private static void windowFunc(float[] data) {
        float[] windows = hannWindow.generateCurve(data.length);
        for (int i = 0; i < data.length; i++) {
            data[i] = data[i] * windows[i];
        }
    }

    public Signal fftPower() {
        fft = new FFT(signalBuffer.length);
        float[] tempBuffer = new float[signalBuffer.length * 2];

        System.arraycopy(
                signalBuffer, 0,
            tempBuffer, 0,
            signalBuffer.length
        );

        fft.complexForwardTransform(tempBuffer);
        int n = signalBuffer.length;

        float[] bins = new float[n];
        fft.modulus(tempBuffer, bins);

        return new Signal(
                "fftPower",
                Arrays.copyOfRange(bins, 0, (n/2)),
                sampleRate,
                Signal.Domain.FREQUENCY_DOMAIN
                );
    }


}
