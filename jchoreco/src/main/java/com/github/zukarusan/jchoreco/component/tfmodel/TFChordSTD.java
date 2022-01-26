package com.github.zukarusan.jchoreco.component.tfmodel;

import com.github.zukarusan.jchoreco.component.Chord;
import com.github.zukarusan.jchoreco.component.chroma.Chroma;
import org.tensorflow.Graph;
import org.tensorflow.GraphOperation;
import org.tensorflow.SavedModelBundle;
import org.tensorflow.Session;
import org.tensorflow.ndarray.Shape;
import org.tensorflow.ndarray.buffer.DataBuffers;
import org.tensorflow.ndarray.buffer.FloatDataBuffer;
import org.tensorflow.types.TFloat32;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

final public class TFChordSTD implements TFChordModel {
    public static boolean isDebug = false;
    private final Session.Runner runner;
    private final TFloat32 input;
    private final float[] output = new float[Chord.Total];
    private final FloatDataBuffer oBuffer = DataBuffers.of(output);
    private final FloatDataBuffer iBuffer;

    static File model_dir = new File(".cache_model");
    SavedModelBundle smb;
    private boolean isClose = false;

    private boolean clean(File cacheDir) {
        File[] allContents = cacheDir.listFiles();
        if (allContents != null) {
            for (File file : allContents) {
                if (clean(file)) throw new IllegalStateException("Cannot clean model cache");
            }
        }
        return !cacheDir.delete();
    }

    private String extractCache(boolean overwrite) {
        URL check = (getClass().getClassLoader().getResource("model_chord"));
        if (check == null) {
            throw new IllegalStateException("Model not found");
        }

        try {
            if (!model_dir.exists() || overwrite) {
                if (overwrite) {
                    if (clean(model_dir)) throw new IllegalStateException("Cannot overwrite model cache");;
                }
                if (!model_dir.mkdirs()) throw new IllegalStateException("Cannot create dir model cache");
                URL url = getClass().getProtectionDomain().getCodeSource().getLocation();
                JarFile jar = new JarFile(url.getPath());
                JarEntry en;
                Enumeration<JarEntry> entries = jar.entries();
                String en_name = "model_chord/";
                while (entries.hasMoreElements()) {
                    en = entries.nextElement();
                    String name = en.getName();
                    if (name.startsWith(en_name) && !name.equals(en_name)) {
                        Path dest = Paths.get(model_dir.toPath().toString(), name.substring(en_name.length()));
                        if (en.isDirectory()) {
                            if (!dest.toFile().mkdirs()) throw new IllegalStateException("Cannot create dir model cache");
                            continue;
                        }
                        InputStream link = jar.getInputStream(en);
                        Files.copy(link, dest);
                    }
                }
            }
        }
        catch (FileNotFoundException e) {
            return new File(check.getPath()).getAbsolutePath();
        }
        catch (IOException e) {
            throw new IllegalStateException("Cannot extract model cache", e);
        }
        return model_dir.getAbsolutePath();
    }

    @FunctionalInterface
    private interface TryLoad {
        SavedModelBundle tryWith(String path) throws Exception;
    }

    private SavedModelBundle tryLoadOrOverwrite(TryLoad action) throws Exception {
        try {
            return action.tryWith(extractCache(false));
        } catch (Exception e) {
            System.out.println("Overwriting model cache...");
            return action.tryWith(extractCache(true));
        }
    }

    @Override
    public void load() {
        try {
            smb = tryLoadOrOverwrite(SavedModelBundle::load);
        } catch (Exception e) {
            throw new IllegalStateException("Error in loading model", e);
        }

    }

    public TFChordSTD(final float[] input_feeder) {
        if (input_feeder.length != Chroma.CHROMATIC_LENGTH) {
            throw new IllegalArgumentException("Input feeder buffer must be the length of chroma vector. " +
                    "Expected: "+Chroma.CHROMATIC_LENGTH+". Given: "+input_feeder.length);
        }
        load();
        this.iBuffer = DataBuffers.of(input_feeder);
        this.input = TFloat32.tensorOf(Shape.of(1, Chroma.CHROMATIC_LENGTH), iBuffer);

        try {
            if (isDebug) {
                Graph graph = smb.graph();
                System.out.println("Operations: ");
                for (Iterator<GraphOperation> it = graph.operations(); it.hasNext(); ) {
                    GraphOperation go = it.next();
                    System.out.println("\""+ go.name() + "\" => " + go.type());
                }
            }
            runner = smb.session().runner().feed("serving_default_input_1", input).fetch("StatefulPartitionedCall");
        } catch (Exception e) {
            input.close();
            smb.close();
            throw new IllegalStateException("Failed to instantiate session of model", e);
        }
    }

    @Override
    public int predict() {
        if (isClose) {
            throw new IllegalCallerException("Processor is closed!");
        }
        int max = 0;
        input.write(iBuffer);
        try(TFloat32 outTensor = (TFloat32) runner.run().get(0)) {
            outTensor.read(oBuffer);
            for (int i = 1; i < Chord.Total; ++i)
                if (output[i] > output[max]) max = i;
        } catch (Exception e) {
            input.close();
            smb.close();
            throw new IllegalStateException("Cannot fetch model: ", e);
        }
        return max;
    }

    @Override
    public void close() {
        smb.close();
        input.close();
        if (clean(model_dir)) System.out.println("Warning: cache deletion");
        isClose = true;
    }
}