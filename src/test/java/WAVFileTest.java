import id.ac.president.choreco.component.WAVFile;
import id.ac.president.choreco.component.exception.SignalException;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.net.URL;

public class WAVFileTest {
    @Test @Disabled
    public void createAudioFile() throws SignalException, InterruptedException {
        URL url = getClass().getResource("ui_layer.wav");

        assert url != null;
        File file = new File(url.getPath());
        WAVFile WAVFile = new WAVFile(file);
        WAVFile.play();
    }
}
