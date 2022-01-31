package com.github.zukarusan.jchoreco.system;

import be.tarsos.dsp.AudioEvent;
import be.tarsos.dsp.AudioProcessor;
import com.github.zukarusan.jchoreco.component.Chord;
import com.github.zukarusan.jchoreco.component.LogFrequency;
import com.github.zukarusan.jchoreco.component.LogFrequencyVector;
import com.github.zukarusan.jchoreco.component.chroma.CRP;
import com.github.zukarusan.jchoreco.component.chroma.Chroma;
import com.github.zukarusan.jchoreco.component.tfmodel.TFChordLite;
import com.github.zukarusan.jchoreco.component.tfmodel.TFChordModel;
import com.github.zukarusan.jchoreco.component.tfmodel.TFChordSTD;
import lombok.Getter;
import lombok.SneakyThrows;

import java.io.OutputStream;
import java.io.PrintStream;

public class ChordProcessor implements AudioProcessor, AutoCloseable{
    protected static boolean _IS_ANDROID_ = (System.getProperty("java.specification.vendor").contains("Android"));
    public static boolean isDebug = false;

    @Getter
    protected final int[] freqMaps;
    protected final float[] _FFT_BUFFER_;
    protected final float[] _PITCH_BUFFER_;
    protected final float[] _CRP_BUFFER_;
    protected final int bufferSize;
    protected final float sampleRate;
    protected final STFT fftWindower;
    protected final TFChordModel chordModel;
    protected final PrintStream predicted;

    public ChordProcessor(float sampleRate, int bufferSize, PrintStream predicted) {
        this.bufferSize = bufferSize;
        this.sampleRate = sampleRate;
        this.predicted = predicted;
        _FFT_BUFFER_ = new float[bufferSize/2];
        _PITCH_BUFFER_ = new float[LogFrequency.PITCH_LENGTH];
        _CRP_BUFFER_ = new float[Chroma.CHROMATIC_LENGTH];
        this.freqMaps = LogFrequencyVector.createFreqMaps(_FFT_BUFFER_, sampleRate);
        this.fftWindower = new STFT(bufferSize, bufferSize/2);

        if (!_IS_ANDROID_)
            this.chordModel = new TFChordSTD(_CRP_BUFFER_);
        else
            this.chordModel = new TFChordLite();

    }

    public ChordProcessor(float sampleRate, int bufferSize, OutputStream predicted) {
        this(sampleRate, bufferSize, new PrintStream(predicted));
    }

    public static void floatFill(float[] array, float value) {
        int len = array.length;
        if (len > 0){
            array[0] = value;
        }
        for (int i = 1; i < len; i += i) {
            System.arraycopy(array, 0, array, i, Math.min((len - i), i));
        }
    }

    @Override
    public boolean process(AudioEvent audioEvent) {
        float[] buffer = audioEvent.getFloatBuffer();

        floatFill(_FFT_BUFFER_, 0);
        floatFill(_PITCH_BUFFER_, 0);
        floatFill(_CRP_BUFFER_, 0);

        fftWindower.windowFunc(buffer);
        STFT.fftPower(buffer, _FFT_BUFFER_);
        double LOG_CONSTANT = 100.0;
        CommonProcessor.logCompress(_FFT_BUFFER_, LOG_CONSTANT);
        LogFrequencyVector.process(_FFT_BUFFER_, freqMaps, sampleRate, bufferSize, 0, _PITCH_BUFFER_);
        CRP.process(_PITCH_BUFFER_, LOG_CONSTANT, _CRP_BUFFER_);

        predicted.println(
                Chord.get(chordModel.predict())
        );
        return true;
    }

    @SneakyThrows
    @Override
    public void processingFinished() {
        close();
    }

    @Override
    public void close() { // TODO find running still program after close
        chordModel.close();
        predicted.close();
    }
}
