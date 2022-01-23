package com.github.zukarusan.choreco;

import be.tarsos.dsp.AudioDispatcher;
import be.tarsos.dsp.AudioEvent;
import be.tarsos.dsp.AudioProcessor;
import be.tarsos.dsp.io.jvm.AudioDispatcherFactory;
import com.github.zukarusan.choreco.system.ChordProcessor;

import javax.sound.sampled.LineUnavailableException;
import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;

public class SwingChordRecognizer {
    static final float SAMPLE_RATE = 44100;
    static final int BUFFER_SIZE = 44100;
    static ChordProcessor chordProcessor;
    static BufferedReader chordLabelReader;
    static int WIDTH = 800, HEIGHT = 700;

    public static void main(String[] args) throws LineUnavailableException, IOException {
        JFrame frame = new JFrame("Swing Chord Recognizer Example");
        JPanel panel = new JPanel();
        panel.setOpaque(true);
        panel.setBackground(Color.WHITE);
        panel.setLayout(null);

        int h = HEIGHT/4;
        JLabel title = new JLabel("CHORD RECOGNIZER", JLabel.CENTER);
        title.setFont(new Font("Serif", Font.BOLD, 30));
        title.setSize(WIDTH, 30);
        title.setLocation(0, h);

        JLabel chord = new JLabel("", JLabel.CENTER);
        chord.setFont(new Font("Serif", Font.PLAIN, 25));
        chord.setSize(WIDTH, 30);
        chord.setLocation(0, h+60);

        panel.add(title); panel.add(chord);

        frame.setContentPane(panel);
        frame.setSize(WIDTH, HEIGHT);
        frame.setLocationByPlatform(true);
        frame.setVisible(true);


        PipedOutputStream loBuffer = new PipedOutputStream();
        PipedInputStream liBuffer = new PipedInputStream(loBuffer);
        BufferedReader labelReader = new BufferedReader(new InputStreamReader(liBuffer));

        AudioProcessor labelUpdater = new AudioProcessor() {
            @Override
            public boolean process(AudioEvent audioEvent) {
                try {
                    String cl;
                    if ((cl = labelReader.readLine()) != null) {
                        chord.setText(cl);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return true;
            }

            @Override
            public void processingFinished() {
                try {
                    loBuffer.close();
                    liBuffer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };


        Runtime.getRuntime().addShutdownHook(new Thread(() -> {  // SAFE SHUTDOWN HOOK FOR RELEASING RESOURCES
            try {
                System.out.println("Releasing Resources");
                chordProcessor.close();
                loBuffer.close();
                liBuffer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }));

        ChordProcessor.isDebug = false;
        AudioDispatcher dispatcher = AudioDispatcherFactory.fromDefaultMicrophone((int) SAMPLE_RATE, BUFFER_SIZE, BUFFER_SIZE/2);
        chordProcessor = new ChordProcessor(SAMPLE_RATE, BUFFER_SIZE, loBuffer);
        dispatcher.addAudioProcessor(chordProcessor);
        dispatcher.addAudioProcessor(labelUpdater);

        frame.addWindowListener(new WindowAdapter() { // RELEASE RESOURCES
            @Override
            public void windowClosing(WindowEvent e) {
                super.windowClosing(e);
                System.out.println("Releasing Resources");
                dispatcher.stop();
                chordProcessor.close();
                try {
                    loBuffer.close();
                    liBuffer.close();
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
                System.exit(0); // there is still a bug of which a thread running TODO
            }
        });

        dispatcher.run();

    }
}
