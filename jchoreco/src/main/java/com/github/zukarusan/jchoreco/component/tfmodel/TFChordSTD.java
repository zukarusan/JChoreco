package com.github.zukarusan.jchoreco.component.tfmodel;

import com.github.zukarusan.jchoreco.component.Chord;
import com.github.zukarusan.jchoreco.component.chroma.Chroma;
import org.tensorflow.Graph;
import org.tensorflow.GraphOperation;
import org.tensorflow.SavedModelBundle;
import org.tensorflow.Session;
import org.tensorflow.ndarray.Shape;
import org.tensorflow.ndarray.buffer.DataBuffers;
import org.tensorflow.ndarray.buffer.FloatDataBuffer;
import org.tensorflow.types.TFloat32;

import java.io.File;
import java.net.URL;
import java.util.Iterator;

final public class TFChordSTD implements TFChordModel {
    public static boolean isDebug = false;
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
        smb = SavedModelBundle.load(url.getPath(), "serve");
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