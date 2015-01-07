/**
 * @file AudioItem.java
 * @author Kay Choi
 * @description A wrapper class for audio media items.
 */

package quickshow.datatypes;

import ddf.minim.AudioMetaData;
import ddf.minim.AudioPlayer;
import ddf.minim.AudioSample;
import ddf.minim.Minim;

public class AudioItem extends MediaItem {
    private AudioPlayer audio;
    private String title;
    private String author;
    private int length;
    private AudioSample sample;
    private String time;

    /**
     * Class constructor.
     * @param minim the Minim object controlling the audio
     * @param fileName the file name of the audio file to load
     */
    public AudioItem(Minim minim, String fileName) {
        super(fileName);

        audio = minim.loadFile(fileName);

        //Need this to generate waveform in the UI
        sample = minim.loadSample(fileName);

        AudioMetaData meta = audio.getMetaData();

        title = (meta.title().trim().equals("") ? "N/A" : meta.title());
        author = (meta.author().trim().equals("") ? "N/A" : meta.author());
        length = meta.length()/1000;

        int minutes = length/60;
        int seconds = length%60;
        time = String.format("%d:%02d", minutes, seconds);
    }

    /**
     * Retrieves the AudioPlayer object associated with the audio file.
     * @return an AudioPlayer object
     */
    public AudioPlayer getAudio() {
        return audio;
    }

    /**
     * Retrieves the author of the audio file.
     * @return the author as a String
     */
    public String getAuthor() {
        return author;
    }

    /**
     * Retrieves the title of the audio file.
     * @return the title as a String
     */
    public String getTitle() {
        return title;
    }

    /**
     * Retrieves the length of the audio file.
     * @return the length in seconds
     */
    public int getLength() {
        return length;
    }

    /**
     * Retrieves the time of the audio file.
     * @return the length in MM:SS format
     */
    public String getTime() {
        return time;
    }

    /**
     * Retrieves the AudioSample object associated with the audio file.
     * @return an AudioSample object
     */
    public AudioSample getAudioSample(){
        return sample;
    }
}
