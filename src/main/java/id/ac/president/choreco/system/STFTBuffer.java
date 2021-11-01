package id.ac.president.choreco.system;

import be.tarsos.dsp.util.fft.FFT;
import be.tarsos.dsp.util.fft.HannWindow;
import lombok.Getter;

import java.util.Arrays;

public class STFTBuffer {
    @Getter
    private final float[] audioBuffer;
    private final int frameSize;
    private final int hopSize;
    private FFT fft;
    private final static HannWindow hannWindow = new HannWindow();
    private float sampleRate;

    public STFTBuffer(float[] buffer, int frameSize, int hopSize, float sampleRate) {
        this.audioBuffer = buffer;
        this.frameSize = frameSize;
        this.hopSize = hopSize;
        this.sampleRate = sampleRate;
    }

    public float[][] process() {
        fft = new FFT(frameSize);
        int frameTotal = (audioBuffer.length - frameSize) / hopSize + 1;
        int binLength = frameSize / 2;
        float[][] bins = new float[frameTotal][binLength];
        float[] frames;
        for (int i = 0, hop = 0; i < frameTotal; i++, hop+= hopSize) {
            // copy frames to process
            frames = new float[frameSize * 2];
            System.arraycopy(
                audioBuffer, hop,
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

    public float[] fftPower() {
        fft = new FFT(audioBuffer.length);
        float[] tempBuffer = new float[audioBuffer.length * 2];

        System.arraycopy(
            audioBuffer, 0,
            tempBuffer, 0,
            audioBuffer.length
        );

        fft.complexForwardTransform(tempBuffer);
        int n = audioBuffer.length;

        float[] bins = new float[n];
        fft.modulus(tempBuffer, bins);

        return Arrays.copyOfRange(bins, 0, (n/2));
    }


}
