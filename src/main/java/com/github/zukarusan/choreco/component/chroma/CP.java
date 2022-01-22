package com.github.zukarusan.choreco.component.chroma;

import com.github.zukarusan.choreco.component.LogFrequencyVector;
import com.github.zukarusan.choreco.util.VectorUtils;

public class CP extends ChromaVector{
    public CP(LogFrequencyVector logVector) {
        super();
        mapPitch(logVector.getPower());
        VectorUtils.normalizeVector(this.power);
    }
    public CP(float[] power) {super(power);}
}
