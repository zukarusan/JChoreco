package com.github.zukarusan.examples;

import com.github.zukarusan.jchoreco.system.ChordPredictor;

public class ChordExtractor {
    public static void main(String[]args) {
        ChordPredictor predictor = ChordPredictor.getInstance();
        String chord = predictor.predict(new float[12]);
        System.out.println(chord);
        predictor.close();
    }
}
