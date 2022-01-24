package com.github.zukarusan.choreco.system;


import com.github.zukarusan.choreco.component.Chord;
import com.github.zukarusan.choreco.component.chroma.Chroma;
import com.github.zukarusan.choreco.component.tfmodel.TFChordLite;
import com.github.zukarusan.choreco.component.tfmodel.TFChordModel;
import com.github.zukarusan.choreco.component.tfmodel.TFChordSTD;

import java.util.ArrayList;
import java.util.List;


public final class ChordPredictor implements AutoCloseable {
    static boolean IS_ANDROID = (System.getProperty("java.specification.vendor").contains("Android"));

    private static volatile ChordPredictor instance;
    private static TFChordModel chordModel;
    private static float[] chromaBuffer;

    /*** Call this within try-with-resources block or cautiously
     *      use singleton needs with close() ***/
    public static ChordPredictor getInstance() {
        ChordPredictor result = instance;
        if (result != null) {
            return result;
        }
        synchronized (ChordPredictor.class) {
            if (instance == null) {
                instance = new ChordPredictor();
            }
            return instance;
        }
    }

    private ChordPredictor() {
        chromaBuffer = new float[Chroma.CHROMATIC_LENGTH];
        if (!IS_ANDROID)
            chordModel = new TFChordSTD(chromaBuffer);
        else
            chordModel = new TFChordLite();
    }

    public String predict(final float[] crpVector) {
        int i = 0;
        for (float f : crpVector) {
            chromaBuffer[i++] = f;
        }
        return Chord.get(chordModel.predict());
    }

    public List<String> predict(final List<float[]> crpVectors) {
        List<String> predictedList = new ArrayList<>();
        for (float[] vector : crpVectors) {
            predictedList.add(predict(vector));
        }
        return predictedList;
    }

    @Override
    public void close() {
        synchronized (ChordPredictor.class) {
            chordModel.close();
            instance = null;
        }
    }
}
