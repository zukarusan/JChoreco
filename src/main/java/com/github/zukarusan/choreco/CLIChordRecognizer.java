package com.github.zukarusan.choreco;

import be.tarsos.dsp.AudioDispatcher;
import be.tarsos.dsp.io.jvm.AudioDispatcherFactory;
import com.github.zukarusan.choreco.system.ChordProcessor;

import javax.sound.sampled.LineUnavailableException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.Scanner;

public class CLIChordRecognizer {
    static final float SAMPLE_RATE = 44100;
    static final int BUFFER_SIZE = 44100;
    static Thread chordThread = null;
    public static void main(String[] args) throws LineUnavailableException, IOException {
        ChordProcessor.isDebug = false;
        AudioDispatcher dispatcher = AudioDispatcherFactory.fromDefaultMicrophone((int) SAMPLE_RATE, BUFFER_SIZE, BUFFER_SIZE/2);
        PrintStream output_chords = new PrintStream(System.out);
        ChordProcessor chordProcessor = new ChordProcessor(SAMPLE_RATE, BUFFER_SIZE, output_chords);
        dispatcher.addAudioProcessor(chordProcessor);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {  // SAFE SHUTDOWN HOOK FOR RELEASING RESOURCES
                System.out.println("Releasing Resources");
                chordProcessor.close();
        }));
        chordThread = new Thread(dispatcher, "Chord recognizer");

        System.out.println("============================ CHORD RECOGNIZER ========================================");
        System.out.println("               Input chars and enter to exit (interrupt) the program                  ");
        System.out.println("======================================================================================");
        chordThread.start();
        new BufferedReader(new InputStreamReader(System.in)).readLine();
        chordThread.interrupt();

        dispatcher.stop(); // This will call the close() in the chord processor class

        chordProcessor.close(); // Again, just a safe method call to close the ChordProcessor to
                                // release the resources in tensorflow model and output stream
        System.exit(0);
    }
}
