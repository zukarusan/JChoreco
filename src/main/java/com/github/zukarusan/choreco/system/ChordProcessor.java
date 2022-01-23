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
    private final int bufferSize;
    private final float sampleRate;
    private final STFT fftWindower;
    private final TFChordModel chordModel;
    private final PrintStream predicted;


    public interface TFChordModel {
        int predict();
        void close();
    }

    final public static class TFChordLite implements TFChordModel { // TODO: develop for android .tflite model

        @Override
        public int predict() {
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
        private final FloatDataBuffer iBuffer;

        SavedModelBundle smb;
        private boolean isClose = false;

        public TFChordSTD(final float[] input_feeder) {
            if (input_feeder.length != Chroma.CHROMATIC_LENGTH) {
                throw new IllegalArgumentException("Input feeder buffer must be the length of chroma vector. " +
                        "Expected: "+Chroma.CHROMATIC_LENGTH+". Given: "+input_feeder.length);
            }
            URL url = this.getClass().getClassLoader().getResource("model_chord");
            this.iBuffer = DataBuffers.of(input_feeder);
            this.input = TFloat32.tensorOf(Shape.of(1, Chroma.CHROMATIC_LENGTH), iBuffer);
            assert url != null;
            smb = SavedModelBundle.load(new File(url.getPath()).getAbsolutePath(), "serve");
            try {
                if (isDebug) {
                    Graph graph = smb.graph();
                    System.out.println("Operations: ");
                    for (Iterator<GraphOperation> it = graph.operations(); it.hasNext(); ) {
                        GraphOperation go = it.next();
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
        public int predict() {
            assert !isClose;
            int max = 0;
            input.write(iBuffer);
                try(TFloat32 outTensor = (TFloat32) runner.run().get(0)) {
                    outTensor.read(oBuffer);
                    for (int i = 1; i < Chord.Total; ++i)
                        if (output[i] > output[max]) max = i;
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

    public void close() {
        chordModel.close();
        predicted.close();
    }
}
