package com.github.zukarusan.jchoreco.system;

public class DCT {
    double[] cm = new double[2];
    double[] cn = new double[2];
    double[][] cos1_buff;
    double[][] cos2_buff;
    int m, n;

    public DCT(int m, int n) {
        this.m = m;
        this.n = n;
        cm[0] = 1.0 / Math.sqrt(m);
        cm[1] = Math.sqrt(2) / Math.sqrt(m);
        cn[0] = 1.0 / Math.sqrt(n);
        cn[1] = Math.sqrt(2) / Math.sqrt(n);

        cos1_buff = new double[m][m];
        cos2_buff = new double[n][n];

        for (int i = 0; i < m; i++) {
            double _j = 1.0;
            for (int j = 0; j < m; j++, _j+= 2.0) {
                cos1_buff[i][j] = Math.cos( _j * (double)i * Math.PI / (2.0*m));
            }
        }

        for (int i = 0; i < n; i++) {
            double _j = 1.0;
            for (int j = 0; j < n; j++, _j+= 2.0) {
                cos1_buff[i][j] = Math.cos( _j * (double)i * Math.PI / (2.0*n));
            }
        }
    }

    public float[][] transform(float[][] f) {
        if (f.length!=m && f[0].length!=n)
            throw new IllegalArgumentException("Must be the same size matrix as DCT object is specified (mxn)");

        float[][] out = new float[m][n];
        for (int i = 0; i < m; i++) {
            double ci = (i==0) ? cm[0] : cm[1];

            for (int j = 0; j < n; j++) {
                double cj = (j==0) ? cn[0] : cn[1];
                double t = 0.0;

                for (int k = 0; k < m; k++) {
                    for (int l = 0; l < n; l++) {
                        t += f[k][l] * cos1_buff[i][k] * cos2_buff[j][l];
                    }
                }
                out[i][j] = (float) (ci * cj * t);
            }
        }
        return out;
    }

    public float[][] inverse(float[][] f) {
        if (f.length!=m && f[0].length!=n)
            throw new IllegalArgumentException("Must be the same size matrix as DCT object is specified (mxn)");

        float[][] out = new float[m][n];
        for (int k = 0; k < m; k++) {
            for (int l = 0; l < n; l++) {
                double t = 0.0;

                for (int i = 0; i < m; i++) {
                    double ci = (i==0) ? cm[0] : cm[1];

                    for (int j = 0; j < n; j++) {
                        double cj = (j==0) ? cn[0] : cn[1];
                        t += ci * cj * cos1_buff[i][k] * cos2_buff[j][l] * f[i][j];
                    }
                }
                out[k][l] = (float) (t);
            }
        }
        return out;
    }
}
