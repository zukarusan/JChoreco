import id.ac.president.choreco.component.AudioFile;
import id.ac.president.choreco.component.Sound;
import id.ac.president.choreco.component.exception.SoundException;
import id.ac.president.choreco.util.PlotManager;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.net.URL;

public class PlotSoundTest {
    @Test
    public void testPlot() throws SoundException {
        URL url = getClass().getResource("layer_toggle.wav");

        assert url != null;
        File file = new File(url.getPath());
        AudioFile audioFile = new AudioFile(file);
        Sound sound = new Sound("Sound Test", audioFile);
//        sound.plot(0, 0.0f * sound.getMax_Second(), 340, "Test1");
//        sound.plot(1, 0.2f * sound.getMax_Second(), 2000, "Test2");
//        sound.plotJavaFx(0, 0.0f * sound.getMax_Second(), 340);
//        sound.plotJavaFx(1, 0.2f * sound.getMax_Second(), 2000);
        sound.plot();
        PlotManager.getInstance().waitForClose();
    }
}
