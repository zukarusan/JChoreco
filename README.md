# Java Chord Recognition

---

A java musical chord recognition utility using pre-trained neural network Tensorflow model and DSP tools extended from [TarsosDSP][1]. 

> ***WARNING***: This library is still in development that has unorganized source code and some features are not implemented on top of another

The **main processing tools** in package dir you might want to use are:

- `Chord Processor`: A sub-*TarsosDSP* class extended from [`AudioProcessor`][3] that wraps Tensorflow model and
  other main processing components
- `Chord Predictor`: A singleton class that predict the chroma given in the params.
- `CRP Vector Factory`: A factory for creating chroma object, specifically CRP chroma, from samples given.

This toolkit has currently the following main component classes:

- `Sound File` : A naive Interface for retrieving full samples from WAV or MP3 file.
- `Signal`: A naive float array of raw sample retrieved from Sound File or from other src
- `Log Frequency Vector`: A full descriptive class that has pitch information from a sample
- `Chroma Vector`: Interface class that has currently 3 different types of chroma classes.
  For the details of these different chroma, please refer to the *Method and Approach* section
- `Spectrum`: A full description abstract class that has spectral information with the given sub-classed type
- `Tensorflow Model`: A specific tensorflow model loader and runner with its [Java API][2]. The loaded model is
  pretrained and stored in main resources package
  
## Chord sets and model

---

Avalaible chord sets that can be predicted by the pre-trained neural model is located in the Chord.java class. They are in all keys with only major and minor scale. The chords list given the model output are respectively:
```java
{
    "A#maj", "A#min", "Amaj", "Amin", "Bmaj", "Bmin", "C#maj", "C#min",
    "Cmaj", "Cmin", "D#maj", "D#min", "Dmaj", "Dmin", "Emaj", "Emin",
    "F#maj", "F#min", "Fmaj", "Fmin", "G#maj", "G#min", "Gmaj", "Gmin"
}
```  
The model is set to have ~0.7 testing accuracy with the testing subset consisting of ~400 samples of chroma (specifically CRP) vectors

## Install and Dependencies

---
### Require min: Java 11

**For easy quick use**:<br>Import locally the full-bundled fat jar library, [`jchoreco-x.x.x-all.jar`][6], without implementing any dependencies in your code
(But this might cause big-sized package in your built app)

***Otherwise, for lightweight use***:<br>
Use locally the smaller-packaged, [`jchoreco-x.x.x.jar`][7], and the following dependencies must be manually included in your project. 

```java
// gradle example (kotlin)
implementation("com.github.axet:TarsosDSP:2.4") // TarsosDSP libraries
implementation("com.github.wendykierp:JTransforms:3.1") // Pure java FFT libraries
implementation("org.tensorflow:tensorflow-core-platform:0.4.0") // Tensorflow API
implementation("javazoom:jlayer:1.0.1") // MP3 Decoder
```

### Optional dependencies
```kotlin
implementation("org.apache.commons:commons-math3:3.6.1") // For CommonProcessor tools
implementation("com.github.yannrichet:JMathPlot:1.0.1") // For plotting using swing ui
```

## Quick wrap example

---

*For all examples, it is assumed the chord model is loaded in the package.
Refer to Usage Example for details.*
<br><br>From file, predict chord **cautiously** of *all samples in the file* with the following code
```java
String pathFile = "/path/to/file.wav";
Signal signal = new WAVFile(new File(pathFile)).getSamples(0);

try (ChordPredictor predictor = ChordPredictor.getInstance()) {
    String chord = predictor.predict(CRPVectorFactory.from_signal(signal));
    System.out.println("Predicted chord: "+chord);
}
```
## Usage example

---

### Extracting samples
Use either `SoundFile` or *TarsosDSP*'s [`AudioDispatcher`][4] to retrieve, respectively, full or buffer 
samples from an audio. For `SoundFile`:
```java
File file = new File(pathString); // java.io.File

// Caution: This retrieves all sample from the file

SoundFile sound = new MP3File(file);
// or
SoundFile sound = new WAVFile(file);

Signal signal = sound.getSamples(0); // get signal from channel 1
// or
float[] samples = signal.getData(); // if you need float samples, retrieve its data of the audio
float sampleRate = signal.getSampleRate();
```
For *TarsosDSP*'s` approach, refer to its [example applications][5]

### Collecting  chroma (CRP) vector

Simply use `CRPVectorFactory` to exctract the chroma vector from samples sources
```java
CRP chromaCRP = CRPVectorFactory.from_signal(signal);
// or
float[] chromaCRP = CRPVectorFactory.from_floatSamples(samples, sampleRate);
```

### Predicting chord from chroma vector

> ***CAUTION*** : Using this library chord predictor/processor is not thread-safe. This is because the current tensorflow
> model java api (v0.4.0) declare that some of its resources must be manually released by the caller.
> 
> To avoid memory leak, use pre-cautiously with try-with-resources block or call `close()` method 
> from the predictor/processor class to release the resources.

> **Alert**: Make sure the pre-trained tensorflow model is provided in the main resource package 

#### Using singleton `ChordPredictor`

Surround with try-with-resources block:

```java
// use try-with-resources block
try (ChordPredictor predictor = ChordPredictor.getInstance()) {
      String chord = predictor.predict(chromaCRP); // predict the chord
      System.out.println(chord); // printing the chord
      
      // or predict list of processed crp
      
      List<float[]> chromaList = ...;
      List<String> chords = predictor.predict(chromaList);
}
```
or manually call the `close()`:

```java
// use with manual close()
ChordPredictor predictor = ChordPredictor.getInstance();
System.out.println(predictor.predict(chromaCRP)); 
predictor.close(); // call the close() whenever finished using it
```

#### Using `ChordProcessor` and `AudioDispatcher` from TarsosDSP

*TarsosDSP*'s `AudioDispatcher` runs on an audio input stream and provide the sample buffer
every once a time defined by the constructor. Adding the `ChordProcessor` to the dispatcher
is an idea to predict chord in buffer.
> **ALERT**: `ChordProcessor` is always closed after the dispatcher finished.
> But it is always a best practice to surround it with try-with-resources block or call the close() again.
 
To retrieve the chord bytes of lined string ( the processor output a form of`(chord + '\n')`) ,
create a class of java `OutputStream` first:

```java
int BUFFER_SIZE = 1024 * 16; // 16384 bytes or more is good for chord audio buffering
AudioDispatcher dispatcher = AudioDispatcherFactory.fromFile(file, BUFFER_SIZE, BUFFER_SIZE/2);

OutputStream output = System.out; // Set the output stream as the console

try (ChordProcessor chordProcessor = 
    new ChordProcessor(SAMPLE_RATE, BUFFER_SIZE, output)) {
    
    dispatcher.addAudioProcessor(chordProcessor);
    dispatcher.run();
}
```

### Example applications


There are 2 provided examples in the main package:

- **CLIChordRecognizer**: a chord processing from microphone printing to console. *Warning*:
Interrupt the program by entering chars, not other keyboard interrupt. 
- **SwingChordRecognizer**: a chord processing from microphone with simple swing GUI.

## Method and Approach

---

The methodology for this project to achieve chord recognition is based on the following works:

1. 

[1]: <https://github.com/JorenSix/TarsosDSP> "TarsosDSP"
[2]: https://www.tensorflow.org/jvm/api_docs/
[3]: https://0110.be/releases/TarsosDSP/TarsosDSP-2.4/TarsosDSP-2.4-Documentation/be/tarsos/dsp/AudioProcessor.html
[4]: https://0110.be/releases/TarsosDSP/TarsosDSP-2.4/TarsosDSP-2.4-Documentation/be/tarsos/dsp/AudioDispatcher.html
[5]: https://github.com/JorenSix/TarsosDSP#tarsosdsp-example-applications
[6]: https://github.com/zukarusan/JChoreco/releases/download/v0.9.1/jchoreco-0.9.1-all.jar
[7]: https://github.com/zukarusan/JChoreco/releases/download/v0.9.1/jchoreco-0.9.1.jar
