import com.github.zukarusan.jchoreco.component.Chord;
import com.github.zukarusan.jchoreco.component.Signal;
import com.github.zukarusan.jchoreco.component.chroma.Chroma;
import com.github.zukarusan.jchoreco.component.sound.MP3File;
import com.github.zukarusan.jchoreco.system.CRPVectorFactory;
import com.github.zukarusan.jchoreco.system.ChordPredictor;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.tensorflow.SavedModelBundle;
import org.tensorflow.Session;
import org.tensorflow.ndarray.Shape;
import org.tensorflow.ndarray.buffer.DataBuffers;
import org.tensorflow.ndarray.buffer.FloatDataBuffer;
import org.tensorflow.types.TFloat32;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class PredictTest {

    @Test
    public void singletonTest() {
        String pathFile = Objects.requireNonNull(getClass().getResource("major.mp3")).getPath();
        Signal signal = new MP3File(new File(pathFile)).getSamples(0);

        try (ChordPredictor predictor = ChordPredictor.getInstance()) {
            String chord = predictor.predict(CRPVectorFactory.from_signal(signal));
            System.out.println(chord);
        }
    }

    @Test
    @Disabled
    public void tensorflowTest() throws IOException {
        URL url = getClass().getResource("_IV-raw-testing_.csv");
        URL url_exp = getClass().getResource("_DV-testing_.csv");
        URL url_model = this.getClass().getClassLoader().getResource("model_chord");
        assert url != null;
        assert url_exp != null;

        CSVFile iv = CSVFile.load(url);
        CSVFile dv = CSVFile.load(url_exp);

        float[] crp_input = iv.data.get(1);
        float[] crp_label = dv.data.get(1);
        assert crp_input.length == Chroma.CHROMATIC_LENGTH;
        assert crp_label.length == Chord.Total;
        int lbl_idx = findMax(crp_label);
        String expected_chord = Chord.get(lbl_idx);

        float[] chord_output = new float[Chord.Total];
        assert url_model != null;
        try (SavedModelBundle smb = SavedModelBundle.load(new File(url_model.getPath()).getAbsolutePath())) {
            FloatDataBuffer input_buffer = DataBuffers.of(crp_input);
            FloatDataBuffer output_buffer = DataBuffers.of(chord_output);
            TFloat32 input = TFloat32.tensorOf(Shape.of(1, Chroma.CHROMATIC_LENGTH), input_buffer);
            Session.Runner runner = smb.session().runner().feed("serving_default_input_1", input).fetch("StatefulPartitionedCall");

            try (TFloat32 output = (TFloat32) runner.run().get(0)) {
                output.read(output_buffer);
            } catch (Exception e) {
                input.close();
            }
            input.close();

            int max = findMax(chord_output);
            System.out.println("Expected chord: "+ expected_chord +"Predicted chord: "+Chord.get(max));
        }
    }

    static class CSVFile {
        public String[] header;
        public List<float[]> data;

        public static CSVFile load(URL path) throws IOException {
            assert path != null;
            return load(path.getPath());
        }

        public static CSVFile load(String path) throws IOException {
            CSVFile csvFile = new CSVFile();
            File csv = new File(path);
            BufferedReader csvReader = new BufferedReader(new FileReader(csv));
            String row;
            csvFile.header = csvReader.readLine().split(",");
            csvFile.data = new ArrayList<>();
            while ((row = csvReader.readLine()) != null) {
                String[] data = row.split(",");
                float[] float_data = new float[data.length];
                int i = 0;
                for (String datum : data) {
                    float_data[i++] = Float.parseFloat(datum);
                }
                csvFile.data.add(float_data);
            }
            csvReader.close();
            return csvFile;
        }
    }

    public int findMax(final float[] chord_exp) {
        assert chord_exp.length == Chord.Total;
        int max = 0;
        for (int i = 1; i < Chord.Total; ++i)
            max = (chord_exp[i] >= chord_exp[max]) ?
                    i :
                    max;
        return max;
    }
}
