package com.github.zukarusan.jchoreco.component.chroma;

import com.github.zukarusan.jchoreco.component.LogFrequency;
import com.github.zukarusan.jchoreco.component.LogFrequencyVector;
import com.github.zukarusan.jchoreco.util.VectorUtils;
import lombok.Getter;

import java.util.Arrays;

public class CLP extends ChromaVector{

    @Getter
    private final double logConstant;

    public CLP(LogFrequencyVector logVector, double constant) {
        super();
        this.logConstant = constant;
        float[] pitches = Arrays.copyOf(logVector.getPower(), LogFrequency.PITCH_LENGTH);
        VectorUtils.mapFunc(pitches, (i) -> i * logConstant + 1);
        VectorUtils.mapFunc(pitches, Math::log);
        mapPitch(pitches);
        VectorUtils.normalizeVector(this.power);
    }
    public CLP(float[] power, double logConstant) {super(power); this.logConstant = logConstant;}
}
