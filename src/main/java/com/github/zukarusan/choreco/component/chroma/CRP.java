package com.github.zukarusan.choreco.component.chroma;

import com.github.zukarusan.choreco.component.LogFrequency;
import com.github.zukarusan.choreco.component.LogFrequencyVector;
import com.github.zukarusan.choreco.system.DCT;
import com.github.zukarusan.choreco.system.DCT_1D;
import com.github.zukarusan.choreco.util.VectorUtils;
import lombok.Getter;

import java.util.Arrays;

public class CRP extends ChromaVector {
    @Getter
    private final double logConstant;
    public static int p_reduction = 55;

    public CRP(LogFrequencyVector logVector, double logConstant) {
        super(logVector);
        this.logConstant = logConstant;
        float[] pitches = Arrays.copyOf(logVector.getPower(), LogFrequency.PITCH_LENGTH);
        VectorUtils.mapFunc(pitches, (i) -> i * logConstant + 1);
        VectorUtils.mapFunc(pitches, Math::log);

        DCT_1D dct = new DCT_1D(LogFrequency.PITCH_LENGTH);
        float[] t_pitches = dct.transform(pitches);
        for (int i = 0; i < p_reduction; i++) {
            t_pitches[i] = 0f;
        }
        t_pitches = dct.inverse(t_pitches);

        mapPitch(t_pitches);
        VectorUtils.normalizeVector(this.power);
    }
    public CRP(float[] power, double logConstant) {super(power); this.logConstant = logConstant;}
}
