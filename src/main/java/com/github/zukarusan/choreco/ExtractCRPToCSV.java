package com.github.zukarusan.choreco;

import com.github.zukarusan.choreco.component.LogFrequencyVector;
import com.github.zukarusan.choreco.component.Signal;
import com.github.zukarusan.choreco.component.SignalFFT;
import com.github.zukarusan.choreco.component.chroma.CRP;
import com.github.zukarusan.choreco.component.sound.MP3File;
import com.github.zukarusan.choreco.component.sound.SoundFile;
import com.github.zukarusan.choreco.component.sound.WAVFile;
import com.github.zukarusan.choreco.system.CommonProcessor;
import com.github.zukarusan.choreco.system.STFT;
import com.opencsv.CSVWriter;
import org.apache.commons.cli.*;
import org.apache.commons.io.FileUtils;


import java.io.Console;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ExtractCRPToCSV {
    public static String SRC_PATH;
    public static String DEST_PATH;
    public static String CHORD_NAME;
    public static Double LOG_CONSTANT = 100.0;
    public static File[] LIST_FILES;
    public static SoundFile[] SOUND_FILES;
    public static CRP[] EXTRACTED_CRP;
    public static File SRC_DIR, DEST_DIR;

    public static void main(String[] args) {
        Options options = new Options()
                .addOption("s", "src", true, "Source directory")
                .addOption("d", "dest", true, "Destination directory")
                .addOption("l", "log", true, "Log constant for CRP and Spectral analysis")
                .addOption("c", "chord", true, "Chord label or else default label will be the path");
        CommandLineParser parser = new DefaultParser();
        CommandLine cmd;
        try {
            cmd = parser.parse(options, args);
        } catch (ParseException pe) {
            pe.printStackTrace();
            System.exit(1);
            return;
        }
        if (cmd.hasOption("s") && cmd.hasOption("d")) {
            SRC_PATH = cmd.getOptionValue("s");
            DEST_PATH = cmd.getOptionValue("d");
        }
        else {
            System.out.println("Required source or destination arguments.");
            Console console = System.console();
            if (console == null) System.exit(-1);
            SRC_PATH = console.readLine("Source directory: ");
            DEST_PATH = console.readLine("Destination directory: ");
        }

        if (cmd.hasOption("c"))
            CHORD_NAME = cmd.getOptionValue('c');
        else
            CHORD_NAME = new File(SRC_PATH).getName();

        if (cmd.hasOption("l"))
            LOG_CONSTANT = Double.parseDouble(cmd.getOptionValue("l"));


        double start = System.currentTimeMillis()/1000.0;
        System.out.println(("====== CRP Extractor ======"));
        System.out.println("SOURCE PATH: "+SRC_PATH);
        System.out.println("DESTINATION PATH: "+DEST_PATH);
        try {
            createDir();
            GetSound();
            ExtractCRP();
            ExtractCSV();
        } catch (IOException e){
            System.out.println("Failed extracting, reason:");
            e.printStackTrace();
            System.exit(500);
            return;
        }
        double end = System.currentTimeMillis()/1000.0;
        System.out.println("Finished extracting. Time elapsed: "+(end - start)+" seconds\n");
        System.exit(0);
    }

    public static void createDir() throws IOException {
        SRC_DIR = new File(SRC_PATH);
        DEST_DIR = new File(DEST_PATH);
        if (!SRC_DIR.exists()) {
            System.out.println("No such source directory "+SRC_DIR.getName());
            System.exit(502);
        }
        if (!DEST_DIR.exists()) {
            if (!DEST_DIR.mkdir()){
                System.out.println("Cannot create dir "+SRC_DIR.getName());
                System.exit(502);
            }
        }
    }

    public static void GetSound() {
        System.out.println("Getting sound files..");
        File src = new File(SRC_PATH);
        File[] files = src.listFiles();
        List<File> fileList = new ArrayList<>();
        assert files != null;
        for (File f : files) {
            if (f.isFile() && (f.getName().contains(".wav") || f.getName().contains(".mp3")) ) {
                fileList.add(f);
            }
        }
        LIST_FILES = fileList.toArray(new File[0]);

        fileList = new ArrayList<>();
        List<SoundFile> soundList = new ArrayList<>();
        for (int i = 0; i < LIST_FILES.length; i++) {
            if (LIST_FILES[i].getName().contains(".wav")) {
                soundList.add(new WAVFile(LIST_FILES[i]));
                fileList.add(LIST_FILES[i]);
                System.out.println("Retrieved file "+ soundList.get(i).getName());
            }
            else if (LIST_FILES[i].getName().contains(".mp3")) {
                soundList.add(new MP3File(LIST_FILES[i]));
                fileList.add(LIST_FILES[i]);
                System.out.println("Retrieved file "+soundList.get(i).getName());
            }
        }
        LIST_FILES = fileList.toArray(new File[0]);
        SOUND_FILES = soundList.toArray(new SoundFile[0]);
    }

    public static void ExtractCRP() throws IOException {
        EXTRACTED_CRP = new CRP[SOUND_FILES.length];
        for (int i = 0; i < SOUND_FILES.length; i++) {
            System.out.write(("Extracting CRP... "+ ((i)*100/ SOUND_FILES.length) + "%\r").getBytes());
            Signal signal = SOUND_FILES[i].getSamples(0);
            SignalFFT fft = STFT.fftPower(signal, signal.getSampleRate());
            CommonProcessor.logCompress(fft, LOG_CONSTANT);
            EXTRACTED_CRP[i] = new CRP(new LogFrequencyVector(fft), LOG_CONSTANT);
        }
        System.out.println("Extracting CRP 100% done.");
    }

    public static void ExtractCSV() {
        File file = new File(DEST_PATH+File.separator+"CRP-"+new File(SRC_PATH).getName()+".csv");
        System.out.println("Parsing CRP data into CSV file: "+file.getName());
        try {
            FileWriter fw = new FileWriter(file);
            CSVWriter writer = new CSVWriter(
                    fw,
                    CSVWriter.DEFAULT_SEPARATOR,
                    CSVWriter.NO_QUOTE_CHARACTER,
                    CSVWriter.DEFAULT_ESCAPE_CHARACTER,
                    CSVWriter.DEFAULT_LINE_END
            );
            List<String[]> outs = new ArrayList<>();
            int p =0;
            for (CRP c : EXTRACTED_CRP) {
                System.out.write(("Parsing... "+(p++ * 100 / EXTRACTED_CRP.length)+"%\r").getBytes());
                float[] data = c.getPower();
                String[] out = new String[data.length+2];
//                String filename = LIST_FILES[p-1].getName();
//                out[0] = filename.substring(0, filename.length()-4);
                int i = 0;
                for( float datum : data ) {
                    out[i++] = Float.toString(datum);
                }
                out[data.length] = Double.toString(SOUND_FILES[p-1].getTotalSecond());
                out[data.length+1] = CHORD_NAME;
                outs.add(out);
            }
            System.out.println("Parsing 100% done.");
            System.out.println("Saving...");
            writer.writeAll(outs);
            System.out.println("Saved CSV file in "+file.getAbsolutePath());
            writer.close();
        } catch (IOException e) {
            System.out.println("Error in writing csv, reason: "+ e.getMessage());
            e.printStackTrace();
            System.exit(501);
        }
    }

}
