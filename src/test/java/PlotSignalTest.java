import id.ac.president.choreco.component.Spectrum;
import id.ac.president.choreco.component.sound.SoundFile;
import id.ac.president.choreco.component.sound.WAVFile;
import id.ac.president.choreco.system.STFT;
import id.ac.president.choreco.component.Signal;
import id.ac.president.choreco.system.exception.STFTException;
import id.ac.president.choreco.component.exception.SignalException;
import id.ac.president.choreco.system.SignalProcessor;
import id.ac.president.choreco.util.PlotManager;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.net.URL;

public class PlotSignalTest {

    URL url = getClass().getResource("Voice_058.wav");
    SoundFile WAVFile;


    PlotSignalTest() {
        assert url != null;
        File file = new File(url.getPath());
        WAVFile = new WAVFile(file);
    }
    @Test @Disabled
    public void testPlot() {

        Signal signal = WAVFile.getSamples(0);
        signal.setName("Test audio plot");
//        sound.plot(0, 0.0f * sound.getMax_Second(), 340, "Test1");
//        sound.plot(1, 0.2f * sound.getMax_Second(), 2000, "Test2");
//        sound.plotJavaFx(0, 0.0f * sound.getMax_Second(), 340);
//        sound.plotJavaFx(1, 0.2f * sound.getMax_Second(), 2000);
        signal.plot();
        PlotManager.getInstance().waitForClose();
    }

    @Test @Disabled
    public void testFFTPlot() throws STFTException {
        assert url != null;
        Signal signal = WAVFile.getSamples(0);
        signal.setName("testFFTPlot");

        STFT stft = new STFT(
                signal,
                1024,
                512,
                signal.getSampleRate());

        Signal signalFFT = stft.fftPower();
        int length = signal.getData().length;

//        SignalProcessor.powerToDb(signalFFT);
        SignalProcessor.normalize(signalFFT);
        signalFFT = SignalProcessor.trimOfRange(signalFFT, 0, 5000, stft.getFrequencyResolutionFFT());
        int[] peaks = SignalProcessor.peakDetection(signalFFT.getData(), 4.3f);
        float[] freq_peaks = SignalProcessor.idxToFreq(peaks, signal.getSampleRate(), length);

        PlotManager plotManager= PlotManager.getInstance();
        plotManager.createPlot(
            "TestFFT", "Test", freq_peaks
        );
        plotManager.createPlot(
                "TestFFT", "Test signalFFT", signalFFT.getData()
        );
        plotManager.waitForClose();
    }

    @Test @Disabled
    public void testSpectrogram() throws STFTException {
        assert url != null;

        Signal signal = WAVFile.getSamples(0);
        signal.setName("Test Spectrogram");

        STFT stft = new STFT(
                signal,
                1024,
                512,
                signal.getSampleRate());
        PlotManager plotManager= PlotManager.getInstance();
        Spectrum spectrum = stft.process();
        SignalProcessor.powerToDb(spectrum);
        SignalProcessor.normalize(spectrum);
//        spectrum = SignalProcessor.trimOfRange(spectrum, 100, 5000, sound.getSampleRate());

//        for (float[] d : spectrum) {
//            plotManager.createPlot(
//                    "TestFFT", "Test", d
//            );
//        }
        plotManager.createSpectrogram(signal.getName(), spectrum.getDataBuffer());
        plotManager.waitForClose();
    }
}
