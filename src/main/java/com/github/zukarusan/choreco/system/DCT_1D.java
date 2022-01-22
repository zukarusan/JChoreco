package com.github.zukarusan.choreco.system;

public class DCT_1D {
    double[] cm = new double[2];
    double[][] cos_buff;
    int m;

    public DCT_1D(int m) {
        this.m = m;
        cm[0] = 1.0 / Math.sqrt(m);
        cm[1] = Math.sqrt(2) / Math.sqrt(m);

        cos_buff = new double[m][m];

        for (int i = 0; i < m; i++) {
            double _j = 1.0;
            for (int j = 0; j < m; j++, _j+= 2.0) {
                cos_buff[i][j] = Math.cos( _j * (double)i * Math.PI / (2.0*m));
            }
        }
    }

    public void transform(final float[] f, final float[] out) {
//        if (f.length!=m)
//            throw new IllegalArgumentException("Must be the same size matrix as DCT object is specified (m)");
        assert f.length == m;
        assert out.length == m;
        for (int i = 0; i < m; i++) {
            double ci = (i==0) ? cm[0] : cm[1];

            double t = 0.0;

            for (int k = 0; k < m; k++) {
                t += f[k] * cos_buff[i][k];
            }
            out[i] = (float) (ci * t);
        }
    }

    public void inverse(final float[] f, final float[] out) {
//        if (f.length!=m)
//            throw new IllegalArgumentException("Must be the same size matrix as DCT object is specified (m)");
        assert f.length == m;
        assert out.length == m;
        for (int k = 0; k < m; k++) {
            double t = 0.0;

            for (int i = 0; i < m; i++) {
                double ci = (i==0) ? cm[0] : cm[1];

                t += ci * cos_buff[i][k] * f[i];
            }
            out[k] = (float) (t);
        }
    }
}
