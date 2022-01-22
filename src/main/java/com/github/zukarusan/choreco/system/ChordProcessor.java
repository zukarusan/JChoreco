package com.github.zukarusan.choreco.system;

import be.tarsos.dsp.AudioEvent;
import be.tarsos.dsp.AudioProcessor;
import com.github.zukarusan.choreco.component.Chord;
import com.github.zukarusan.choreco.component.LogFrequency;
import com.github.zukarusan.choreco.component.LogFrequencyVector;
import com.github.zukarusan.choreco.component.chroma.CRP;
import com.github.zukarusan.choreco.component.chroma.Chroma;
import lombok.Getter;
import lombok.SneakyThrows;
import org.tensorflow.*;
import org.tensorflow.ndarray.Shape;
import org.tensorflow.ndarray.buffer.DataBuffers;
import org.tensorflow.ndarray.buffer.FloatDataBuffer;
import org.tensorflow.types.TFloat32;

import java.io.File;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.URL;
import java.util.Iterator;

public class ChordProcessor implements AudioProcessor {
    static boolean _IS_ANDROID_ = (System.getProperty("java.specification.vendor").contains("Android"));
    static public boolean isDebug = false;

    @Getter
    private final int[] freqMaps;
    private final float[] _FFT_BUFFER_;
    private final float[] _PITCH_BUFFER_;
    private final float[] _CRP_BUFFER_;
    private final FloatDataBuffer tensorFeeder;
    private final int bufferSize;
    private final float sampleRate;
    private final TFChordModel chordModel;
    private final PrintStream predicted;


    public interface TFChordModel {
        int predict(FloatDataBuffer chroma);
        void close();
    }

    final public static class TFChordLite implements TFChordModel { // TODO: develop for android .tflite model

        @Override
        public int predict(FloatDataBuffer chroma) {
            return 0;
        }

        @Override
        public void close() {

        }
    }
    final public static class TFChordSTD implements TFChordModel {
        private final Session.Runner runner;
        private final TFloat32 input;
        private final float[] output = new float[Chord.Total];
        private final FloatDataBuffer oBuffer = DataBuffers.of(output);

        SavedModelBundle smb;
        private boolean isClose = false;

        public TFChordSTD() {
            /* URL url = getClass().getResource("model_std"); */
            URL url = this.getClass().getClassLoader().getResource("raw_2nd_good");
            FloatDataBuffer iBuffer = DataBuffers.of(new float[Chroma.CHROMATIC_LENGTH]);
            input = TFloat32.tensorOf(Shape.of(1, Chroma.CHROMATIC_LENGTH), iBuffer);
            assert url != null;
            smb = SavedModelBundle.load(new File(url.getPath()).getAbsolutePath(), "serve");
            try {
                if (isDebug) {
                    Graph graph = smb.graph();
                    System.out.println("Operations: ");
                    for (Iterator<Operation> it = graph.operations(); it.hasNext(); ) {
                        GraphOperation go = (GraphOperation) it.next();
                        System.out.println("\""+ go.name() + "\" => " + go.type());
                    }
                }
                runner = smb.session().runner().feed("serving_default_input_1", input).fetch("StatefulPartitionedCall");
            } catch (Exception e) {
                input.close();
                smb.close();
                throw new IllegalStateException("Failed to instantiate session of model", e);
            }
        }

        @Override
        public int predict(FloatDataBuffer chroma) {
            assert !isClose;
            int max = 0;
            input.write(chroma);
                try(TFloat32 outTensor = (TFloat32) runner.run().get(0)) {
                    outTensor.read(oBuffer);
                    for (int i = 1; i < Chord.Total; ++i)
                        max = (output[i] > max) ? i : (i-1);
                } catch (Exception e) {
                    input.close();
                    smb.close();
                    throw new IllegalStateException("Cannot fetch model: ", e);
                }
            return max;
        }

        @Override
        public void close() {
            smb.close();
            input.close();
            isClose = true;
        }
    }

    public ChordProcessor(float sampleRate, int bufferSize, PrintStream predicted) {
        this.bufferSize = bufferSize;
        this.sampleRate = sampleRate;
        _FFT_BUFFER_ = new float[bufferSize/2];
        _PITCH_BUFFER_ = new float[LogFrequency.PITCH_LENGTH];
        _CRP_BUFFER_ = new float[Chroma.CHROMATIC_LENGTH];
        tensorFeeder = DataBuffers.of(_CRP_BUFFER_);
        freqMaps = LogFrequencyVector.createFreqMaps(_FFT_BUFFER_, sampleRate);

        if (!_IS_ANDROID_)
            this.chordModel = new TFChordSTD();
        else
            this.chordModel = new TFChordLite();

        this.predicted = predicted;
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
            System.arraycopy(array, 0, array, i, ((len - i) < i) ? (len - i) : i);
        }
    }

    @SneakyThrows
    @Override
    public boolean process(AudioEvent audioEvent) {
        float[] buffer = audioEvent.getFloatBuffer();

        floatFill(_FFT_BUFFER_, 0);
        floatFill(_PITCH_BUFFER_, 0);
        floatFill(_CRP_BUFFER_, 0);

        STFT.fftPower(buffer, _FFT_BUFFER_);
        double LOG_CONSTANT = 100.0;
        CommonProcessor.logCompress(_FFT_BUFFER_, LOG_CONSTANT);
        LogFrequencyVector.process(_FFT_BUFFER_, freqMaps, sampleRate, bufferSize, 0, _PITCH_BUFFER_);
        CRP.process(_PITCH_BUFFER_, LOG_CONSTANT, _CRP_BUFFER_);

        predicted.println(
                Chord.get(chordModel.predict(tensorFeeder)));
        return true;
    }

    @SneakyThrows
    @Override
    public void processingFinished() {
        close();
    }

    public void close() {
        chordModel.close();
        predicted.close();
    }
}