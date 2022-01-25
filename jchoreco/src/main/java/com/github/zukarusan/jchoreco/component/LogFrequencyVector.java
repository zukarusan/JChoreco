package com.github.zukarusan.jchoreco.component;

import com.github.zukarusan.jchoreco.system.CommonProcessor;
import com.github.zukarusan.jchoreco.util.PlotManager;
import lombok.Getter;

public class LogFrequencyVector {

    @Getter private final float[] power;
    @Getter private final int[] freqMaps;
    public static final float PEAK_THRESHOLD = 3f;


    public LogFrequencyVector(float[] pitches_power) {
        if (pitches_power.length != LogFrequency.PITCH_LENGTH) {
            throw new IllegalArgumentException("This constructor accepts power values of 128 MIDI pitches");
        }
        this.power = pitches_power;
        this.freqMaps = null;
    }

    public LogFrequencyVector(SignalFFT signal) {
        this.power = new float[LogFrequency.PITCH_LENGTH];
        this.freqMaps = createFreqMaps(signal.getData(), signal.getSampleRate());
        process(signal.getData(), this.freqMaps, signal.getFrequencyResolution(), signal.getOffset(), this.power);
    }

    public static int[] createFreqMaps(final float[] fft_buffer, float sampleRate) {
        return LogFrequency.createFrequencyMap(fft_buffer.length * 2, sampleRate);
    }

    public static void process(final float[] fft_buffer, int[] freqMaps, float sampleRate, int rawBufferSize, float trim_offset_freq, final float[] out) {
        process(fft_buffer, freqMaps, (sampleRate/rawBufferSize), trim_offset_freq, out);
    }

    public static void process(final float[] fft_buffer, int[] freqMaps, float frequency_resolution, float trim_offset_freq, final float[] out) {
        assert out.length == LogFrequency.PITCH_LENGTH;
        float[] trimmed = fft_buffer;
        float offset = trim_offset_freq;

        if (offset < 25f){
            trimmed = CommonProcessor.trimOfRange(fft_buffer, 25f, 5000f, frequency_resolution);
            offset = ((int) (25f / frequency_resolution)) * frequency_resolution;
        }

//        int[] peaks = CommonProcessor.findPeaksByAverage(trimmed, PEAK_THRESHOLD);
//        int[] peaks = new int[6]; // pick 6 highest frequency peaks
//        int found = SignalProcessor.tarsosPeakFinder(trimmed, peaks);
//            int[] peaks = CommonProcessor.findPeaksByAverage(trimmed[i], PEAK_THRESHOLD);

        int[] peaks = new int[8]; // pick 8 highest frequency peaks
        int found = CommonProcessor.findPeaksByExtremePoints(trimmed, peaks, 4);
        int total = 1;
        int off_idx = (int) (offset / frequency_resolution);
        for (int j = 0; j < found; j++) {
            int f_idx = off_idx + peaks[j];
            int l_idx = freqMaps[f_idx];
            out[l_idx] += fft_buffer[f_idx];
            if (j+1 < found) {
                if (peaks[j] != peaks[j+1]) {
                    out[l_idx] /= total;
                    total = 1;
                } else total++;
            } else out[l_idx] /= total;
        }
    }

/* Legacy approach of mapping frequency
int pitch = 0, total = 0;
float sum = 0;
for (int peak : peaks) {
    float freq = peak * frequencyResolution + offset;
    LogFrequency logFrequency = LogFrequency.getInstance();
    if (logFrequency.checkFreq(freq, pitch)) {
        sum += trimmed[peak];
        total++;
        continue;
    }

    if (total != 0) this.power[pitch] = sum / total;
    pitch = logFrequency.searchPitch(freq, pitch);
    total = 1;
    sum = trimmed[peak];
}*/



    public void plot() {
        PlotManager plotManager = PlotManager.getInstance();
        plotManager.createPlot("LogFrequencyVector", "Pitch", power);
    }
}
