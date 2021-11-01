import id.ac.president.choreco.component.AudioFile;
import id.ac.president.choreco.system.STFTBuffer;
import id.ac.president.choreco.component.Sound;
import id.ac.president.choreco.component.exception.STFTException;
import id.ac.president.choreco.component.exception.SoundException;
import id.ac.president.choreco.system.SignalProcessor;
import id.ac.president.choreco.util.PlotManager;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.net.URL;

public class PlotSoundTest {

    URL url = getClass().getResource("Voice_058.wav");
    AudioFile audioFile;


    PlotSoundTest() throws SoundException {
        assert url != null;
        File file = new File(url.getPath());
        audioFile = new AudioFile(file);
    }
    @Test @Disabled
    public void testPlot() throws SoundException {
        Sound sound = new Sound("Sound Test", audioFile);
//        sound.plot(0, 0.0f * sound.getMax_Second(), 340, "Test1");
//        sound.plot(1, 0.2f * sound.getMax_Second(), 2000, "Test2");
//        sound.plotJavaFx(0, 0.0f * sound.getMax_Second(), 340);
//        sound.plotJavaFx(1, 0.2f * sound.getMax_Second(), 2000);
        sound.plot();
        PlotManager.getInstance().waitForClose();
    }

    @Test @Disabled
    public void testFFTPlot() throws SoundException {
        assert url != null;
        Sound sound = new Sound("Bruhhhhh", audioFile);

        STFTBuffer STFTBuffer = new STFTBuffer(
                sound.getSamples(0),
                1024,
                512,
                sound.getSampleRate());

        float[] data = STFTBuffer.fftPower();

//        SignalProcessor.powerToDb(data);
        SignalProcessor.normalizePower(data);
        data = SignalProcessor.trimOfRange(data, 100, 5000, sound.getSampleRate());
        int[] peaks = SignalProcessor.peakDetection(data, 1f);

        PlotManager plotManager= PlotManager.getInstance();
        plotManager.createPlot(
            "TestFFT", "Test", peaks
        );
        plotManager.createPlot(
                "TestFFT", "Test data", data
        );
        plotManager.waitForClose();
    }

    @Test //@Disabled
    public void testSpectrogram() throws SoundException, STFTException {
        assert url != null;
        Sound sound = new Sound("Test spectrogram", audioFile);

        STFTBuffer stft = new STFTBuffer(
                sound.getSamples(0),
                1024,
                512,
                sound.getSampleRate());
        PlotManager plotManager= PlotManager.getInstance();
        float[][] data = stft.process();
        SignalProcessor.powerToDb(data);
        SignalProcessor.normalizePower(data);
//        data = SignalProcessor.trimOfRange(data, 100, 5000, sound.getSampleRate());

//        for (float[] d : data) {
//            plotManager.createPlot(
//                    "TestFFT", "Test", d
//            );
//        }
        plotManager.createSpectrogram(sound.getName(), data);
        plotManager.waitForClose();
    }
}
