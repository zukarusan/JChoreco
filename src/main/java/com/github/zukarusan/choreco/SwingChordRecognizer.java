package com.github.zukarusan.choreco;

import be.tarsos.dsp.AudioDispatcher;
import be.tarsos.dsp.AudioEvent;
import be.tarsos.dsp.AudioProcessor;
import be.tarsos.dsp.io.jvm.AudioDispatcherFactory;
import com.github.zukarusan.choreco.system.ChordProcessor;

import javax.sound.sampled.LineUnavailableException;
import javax.swing.*;
import java.awt.*;
import java.io.*;

public class SwingChordRecognizer {
    static final float SAMPLE_RATE = 44100;
    static final int BUFFER_SIZE = 44100;
    static ChordProcessor chordProcessor;
    static BufferedReader chordLabelReader;
    static int WIDTH = 800, HEIGHT = 700;

    public static void main(String[] args) throws LineUnavailableException {
        JFrame frame = new JFrame("Swing Chord Recognizer Example");
        JLabel title = new JLabel("CHORD RECOGNIZER");
        JLabel chord = new JLabel();
        title.setFont(new Font("Serif", Font.BOLD, 30));
        chord.setFont(new Font("Serif", Font.PLAIN, 25));
        frame.setSize(WIDTH, HEIGHT);
        title.setBounds(WIDTH/2, HEIGHT/4, WIDTH, HEIGHT);
        chord.setBounds(WIDTH/2, HEIGHT*3/4, WIDTH, HEIGHT);
        frame.add(title); frame.add(chord);
        frame.setVisible(true);

        ByteArrayOutputStream labelBuffer = new ByteArrayOutputStream();
//        chordLabelReader = new BufferedReader();
        AudioProcessor updateLabel = new AudioProcessor() {
            @Override
            public boolean process(AudioEvent audioEvent) {

                return true;
            }

            @Override
            public void processingFinished() {

            }
        };

        ChordProcessor.isDebug = false;
        AudioDispatcher dispatcher = AudioDispatcherFactory.fromDefaultMicrophone((int) SAMPLE_RATE, BUFFER_SIZE, BUFFER_SIZE/2);
        PrintStream output_chords = new PrintStream(labelBuffer);
        chordProcessor = new ChordProcessor(SAMPLE_RATE, BUFFER_SIZE, output_chords);
        dispatcher.addAudioProcessor(chordProcessor);
        chordProcessor.close();
    }
}
