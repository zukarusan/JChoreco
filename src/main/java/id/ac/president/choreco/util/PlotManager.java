package id.ac.president.choreco.util;

import org.math.plot.Plot2DPanel;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.concurrent.Phaser;

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
