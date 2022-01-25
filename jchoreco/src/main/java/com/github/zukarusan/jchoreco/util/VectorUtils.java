package com.github.zukarusan.jchoreco.util;

import java.util.function.Function;

public final class VectorUtils {

    public static double euclidDistance(float[] vector) {
        double dist = 0;
        for (float v : vector) {
            dist += v * v;
        }
        return Math.sqrt(dist);
    }

    public static void normalizeVector(float[] vector) {
        double length = euclidDistance(vector);
        if (length == 0)
            return;//throw new IllegalStateException("Euclid distance cannot be 0 to normalize");
        for (int i = 0; i < vector.length; i++) {
            vector[i] /= length;
        }
    }

    public static void multiply(float[] vector, float scalar) {
        for (int i = 0; i < vector.length; i++) {
            vector[i] *= scalar;
        }
    }

    public static void addEach(float[] vector, float scalar) {
        for (int i = 0; i < vector.length; i++) {
            vector[i] += scalar;
        }
    }

    public static void mapFunc(float[] vector, Function<Float, Double> action)  {
        for (int i = 0; i < vector.length; i++) {
            vector[i] = (action.apply(vector[i])).floatValue();
        }
    }

}
