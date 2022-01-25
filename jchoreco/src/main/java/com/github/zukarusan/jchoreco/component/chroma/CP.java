package com.github.zukarusan.jchoreco.component.chroma;

import com.github.zukarusan.jchoreco.component.LogFrequencyVector;
import com.github.zukarusan.jchoreco.util.VectorUtils;

public class CP extends ChromaVector{
    public CP(LogFrequencyVector logVector) {
        super();
        mapPitch(logVector.getPower());
        VectorUtils.normalizeVector(this.power);
    }
    public CP(float[] power) {super(power);}
}
