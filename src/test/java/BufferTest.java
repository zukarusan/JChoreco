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


}
