package id.ac.president.choreco.util;

import org.math.plot.Plot2DPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.util.concurrent.Phaser;

import static javax.swing.UIManager.getColor;

public class PlotManager {
    private static volatile PlotManager instance;
    private static final int WIDTH= 1280, HEIGHT= 720;
    private final Phaser closePhaser;

    public static PlotManager getInstance() {
        PlotManager result = instance;
        if (result != null) {
            return result;
        }
        synchronized (PlotManager.class) {
            if (instance == null) {
                instance = new PlotManager();
            }
            return instance;
        }
    }

    private PlotManager() {
        closePhaser = new Phaser();
    }

    public synchronized void waitForClose() {
        closePhaser.awaitAdvance(closePhaser.getPhase());
        instance = null;
    }

    private void createFrame(String name, JPanel panel) {
        JFrame frame = new JFrame("Plot - "+name);
        frame.setSize(WIDTH, HEIGHT);
        frame.setContentPane(panel);
        frame.setVisible(true);
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                super.windowClosing(e);
                closePhaser.arriveAndDeregister();
            }
        });
    }

    public void createSpectrogram(String name, float[][] normalized_data) {
        closePhaser.register();
        int nY = normalized_data[0].length,
            nX = normalized_data.length;

        BufferedImage specImg = new BufferedImage(
                nX,
                nY,
                BufferedImage.TYPE_INT_RGB);

        float ratio;
        for(int x = 0; x<nX; x++){
            for(int y = 0; y<nY; y++){
                ratio = normalized_data[x][y];

                Color newColor = Color.getHSBColor(
                        (ratio)*1.3f,
                        1,
                        ratio
                );
                specImg.setRGB(x, y, newColor.getRGB());

            }
        }

        JPanel spectrogram = new JPanel(){
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.drawImage(specImg, 0, 0, this);
            }
        };
        createFrame(name, spectrogram);
    }


    public void createPlot(String name, String label, float[] data) {
        Plot2DPanel plot = new Plot2DPanel();
        closePhaser.register();

        double[] out = new double[data.length];
        int i = 0;
        for (float s : data) {
            out[i++] = s;
        }
        plot.addLinePlot(label, out);
        createFrame(name, plot);
    }

    public void createPlot(String name, String label, int[] data) {
        Plot2DPanel plot = new Plot2DPanel();
        closePhaser.register();

        double[] out = new double[data.length];
        int i = 0;
        for (int s : data) {
            out[i++] = s;
        }
        plot.addLinePlot(label, out);
        createFrame(name, plot);
    }

    public void createPlot(String name, String label, double[] data) {
        Plot2DPanel plot = new Plot2DPanel();
        closePhaser.register();
        plot.addLinePlot(label, data);
        createFrame(name, plot);
    }

}
