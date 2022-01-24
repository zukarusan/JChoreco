import com.github.zukarusan.choreco.component.Chord;
import com.github.zukarusan.choreco.component.chroma.Chroma;
import com.github.zukarusan.choreco.system.ChordProcessor;
import lombok.Getter;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.tensorflow.SavedModelBundle;
import org.tensorflow.Session;
import org.tensorflow.ndarray.Shape;
import org.tensorflow.ndarray.buffer.DataBuffers;
import org.tensorflow.ndarray.buffer.FloatDataBuffer;
import org.tensorflow.types.TFloat32;

import java.io.*;
import java.net.URL;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

public class BufferTest {
    BufferTest() {

    }

    @Test @Disabled
    public void FloatDataBufferTest(){
        float[] b = new float[5];
        float[] t1 = {1f, 2f, 3f, 4f, 5f};
        float[] t2 = {1f, 2f, 3f, 4f, 5f};
        TFloat32 t_float = TFloat32.vectorOf(t1);
        FloatDataBuffer buffer = DataBuffers.of(b);
        t_float.read(buffer);
        for (int i = 0; i < buffer.size(); i++)
            b[i] += 1f;
        t_float.close();
        t_float = TFloat32.vectorOf(t2);
        t_float.read(buffer);
        for (int i = 0; i < buffer.size(); i++)
            b[i] += 1f;
        t_float.close();
    }

    @Test @Disabled
    public void TFloatTest() {
        float[] b = {1f, 2f, 3f, 4f, 5f};
        FloatDataBuffer buffer = DataBuffers.of(b);
        TFloat32 t_float = TFloat32.tensorOf(Shape.of(5,1), buffer);
        for (int i = 0; i < buffer.size(); i++)
            b[i] += 1f;
        t_float.close();
    }

    @Test @Disabled
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
