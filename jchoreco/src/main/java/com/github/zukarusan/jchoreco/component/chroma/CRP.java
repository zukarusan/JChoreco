package com.github.zukarusan.jchoreco.component.chroma;

import com.github.zukarusan.jchoreco.component.LogFrequency;
import com.github.zukarusan.jchoreco.component.LogFrequencyVector;
import com.github.zukarusan.jchoreco.system.DCT_1D;
import com.github.zukarusan.jchoreco.util.VectorUtils;
import lombok.Getter;

import java.util.Arrays;

public class CRP extends ChromaVector {
    static float[] _BUFFER_DCT_ = new float[LogFrequency.PITCH_LENGTH];
    static float[] _BUFFER_DCT_INVERSE_ = new float[LogFrequency.PITCH_LENGTH];
    static DCT_1D dct = new DCT_1D(LogFrequency.PITCH_LENGTH);
    @Getter
    private final double logConstant;
    public static int p_reduction = 55;

    public CRP(LogFrequencyVector logVector, double logConstant) {
        this.logConstant = logConstant;
        process(Arrays.copyOf(logVector.getPower(), LogFrequency.PITCH_LENGTH), logConstant, this.power);
    }

    public static void process(final float[] logBuffer, double logConstant, final float[] out) {
        assert out.length == Chroma.CHROMATIC_LENGTH;
        VectorUtils.mapFunc(logBuffer, (i) -> i * logConstant + 1);
        VectorUtils.mapFunc(logBuffer, Math::log);

        dct.transform(logBuffer, _BUFFER_DCT_);
            for (int i = 0; i < p_reduction; i++)
                _BUFFER_DCT_[i] = 0f;
        dct.inverse(_BUFFER_DCT_, _BUFFER_DCT_INVERSE_);

        mapPitch(_BUFFER_DCT_INVERSE_, out);
        VectorUtils.normalizeVector(out);
    }
    public CRP(float[] power, double logConstant) {super(power); this.logConstant = logConstant;}
}
