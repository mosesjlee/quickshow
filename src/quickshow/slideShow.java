/**
 * @file slideShow.java
 * @author Kay Choi, Moses Lee
 * @description Plays the slide show.
 */

package quickshow;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.PriorityQueue;

import processing.core.PConstants;
import processing.core.PFont;
import processing.core.PImage;
import processing.video.Movie;
import quickshow.datatypes.AudioItem;
import quickshow.datatypes.ImageItem;
import quickshow.datatypes.MovieItem;
import quickshow.datatypes.VisualItem;
import controlP5.Button;
import controlP5.ControlEvent;
import controlP5.ControlP5;
import controlP5.Group;
import controlP5.Toggle;

public class slideShow {
    private Quickshow parent;

    private boolean debug;

    private Group group;
    private Button stopButton;
    private Toggle playPause;

    private PImage curFrame, transitFrame;
    private int[] transitDelta = {0, 0}, transitDirection = {1, 1};
    private int fadeAlpha = 255;
    private boolean transit = false, fade = false;
    private Movie movie;

    private int frameWidth, frameHeight;

    private ArrayList<AudioItem> audios;
    private ArrayList<VisualItem> visuals;
    private Iterator<VisualItem> visualIter = null;
    private Iterator<AudioItem> audioIter = null;
    private AudioItem curAudioItem = null;
    private VisualItem curVisualItem = null;

    private ArrayList<String> curTagTexts;
    private ArrayList<int[]> curTagTimes;
    private String tagText = "";
    private PriorityQueue<Integer> tagStartTimes;
    private PriorityQueue<Integer> tagEndTimes;

    private float curImgTime;

    private boolean isPlaying = false, isEnabled = false, shuffle = false;

	private PFont font;

    /**
     * Class constructor.
     * @param parent the instantiating Quickshow object
     * @param control the ControlP5 object handling UI elements
     */
    public slideShow(Quickshow parent, ControlP5 control) {
        this.parent = parent;

        font = parent.loadFont("data/SansSerif.bold-32.vlw");

        debug = parent.getDebugFlag();

        audios = new ArrayList<AudioItem>();
        visuals = new ArrayList<VisualItem>();

        curTagTexts = new ArrayList<String>();
        curTagTimes = new ArrayList<int[]>();
        tagStartTimes = new PriorityQueue<Integer>();
        tagEndTimes = new PriorityQueue<Integer>();

        group = control.addGroup("slideShow")
            .setCaptionLabel("")
            .setVisible(false);

        playPause = new NewToggle(control, "playToggle")
            .setCaptionLabel("")
            .setPosition(10, 10)
            .setSize(30, 30)
            .setGroup(group)
            .setImages(
                parent.loadImage("data/img/playbutton.png"),
                null,
                parent.loadImage("data/img/pausebutton.png")
            ).setLock(true);

        stopButton = control.addButton("stopButton")
            .setCaptionLabel("")
            .setLock(true)
            .setPosition(50, 10)
            .setSize(30, 30)
            .setImage(parent.loadImage("data/img/stopbutton.png"))
            .setGroup(group);

        curFrame = parent.createImage(0, 0, PConstants.RGB);
    }

    /**
     * Populates the audio component of the slide show.
     * @param newAudio an ArrayList containing the new AudioItems
     */
    public void addAudio(ArrayList<AudioItem> newAudio) {
        audios.addAll(newAudio);

        if(debug) {
            Quickshow.println("#audio items in slide show: " + audios.size());
        }

        if(!shuffle) {
            audioIter = audios.iterator();
        }

        nextAudioItem();
    }

    /**
     * Populates the visual component of the slide show.
     * @param newVisual an ArrayList containing the new VisualItems
     */
    public void addVisual(ArrayList<VisualItem> newVisual) {
        visuals.addAll(newVisual);

        if(debug) {
            Quickshow.println("#visual items slide show: " + visuals.size());
        }

        if(!shuffle) {
            visualIter = visuals.iterator();
        }

        nextVisualItem();
    }

    /**
     * Callback method for handling ControlP5 UI events.
     * @param e the ControlEvent to handle
     */
    public void controlEvent(ControlEvent e) {
        switch(e.getName()) {
        case "playToggle":
            playToggle(playPause.getState());

            break;

        case "stopButton":
            stopButton();

            break;

        case "Shuffle Slides":
            if(debug) {
                Quickshow.println("shuffle: " +
                    ((Toggle)e.getController()).getState());
            }

            toggleShuffle(((Toggle)e.getController()).getState());

            break;

        case "transitionToggle":
            if(debug) {
                Quickshow.println("shuffle: " +
                    ((Toggle)e.getController()).getState());
            }

            toggleFade(((Toggle)e.getController()).getState());
        }
    }

    /**
     * Callback method for displaying the slide show.
     */
    public void draw() {
        parent.background(0xff555555);

        if(isPlaying) {
            if(!transit) {
                if(curAudioItem != null) {
                    if(!curAudioItem.getAudio().isPlaying()) {
                        nextAudioItem();
                    }
                }

                if(curVisualItem != null) {
                    curImgTime += 1f/parent.frameRate;

                    if(movie != null) {
                        if(movie.available()) {
                            movie.read();
                        }

                        curFrame = movie.get();
                    }

                    if(curImgTime >= (float)curVisualItem.getDisplayTime()) {
                        if(debug) {
                            Quickshow.println("slide show transition begin");
                        }

                        curImgTime = 0f;

                        transit = true;

                        //create transition frame
                        transitFrame = parent.createImage(
                            parent.width,
                            parent.height,
                            PConstants.RGB
                        );
                        transitFrame.set(
                            (parent.width - curFrame.width)/2,
                            (parent.height - curFrame.height)/2,
                            curFrame
                        );

                        //Compensate for transparency
                        transitFrame.loadPixels();
                        float a, r, g, b;
                        for(int i = 0; i < transitFrame.pixels.length; i++) {
                            a = parent.alpha(transitFrame.pixels[i]) / 255f;
                            if(a < 1f) {
                                r = a*parent.red(transitFrame.pixels[i]) +
                            		0x55*(1f-a);
                                g = a*parent.green(transitFrame.pixels[i]) +
                            		0x55*(1f-a);
                                b = a*parent.blue(transitFrame.pixels[i]) +
                            		0x55*(1f-a);

                                transitFrame.pixels[i] = parent.color(r, g, b);
                            }
                        }
                        transitFrame.updatePixels();

                        if(!fade) {
                            //set horizontal transition direction
                            double rand = Math.random();
                            transitDirection[0] = (rand < 0.33 ? 1 :
                                (rand < 0.66 ? 0 : -1));

                            //set vertical transition direction
                            rand = Math.random();
                            if(transitDirection[0] != 0) {
                                transitDirection[1] = (rand < 0.33 ? 1 :
                                    (rand < 0.66 ? 0 : -1));
                            }

                            else {
                                transitDirection[1] = (rand < 0.5 ? 1 : -1);
                            }
                        }

                        nextVisualItem();

                        if(movie != null && movie.available()) {
                            movie.read();

                            curFrame = movie.get();
                        }
                    }

                    if(frameWidth != curFrame.width ||
                        frameHeight != curFrame.height)
                    {
                        calcFrameDims();

                        curFrame.resize(frameWidth, frameHeight);
                    }
                }

                else {
                    stopButton();
                }
            }
        }

        parent.tint(255, 255);
        parent.imageMode(PConstants.CENTER);
        parent.image(curFrame, parent.width/2, parent.height/2);

        if(!transit) {
            if(!tagStartTimes.isEmpty() && tagStartTimes.peek() <= curImgTime) {
                tagStartTimes.poll();
                tagText = genTagString();
            }

            if(!tagEndTimes.isEmpty() && tagEndTimes.peek() <= curImgTime) {
                tagEndTimes.poll();
                tagText = genTagString();
            }

            if(!tagText.equals("")) {
                parent.fill(0);
                parent.textFont(font);
                parent.textAlign(PConstants.CENTER, PConstants.CENTER);

                if(curVisualItem != null) {
                    if(curVisualItem.isAtBottom()) {
                    	//text shadow
                        parent.text(tagText, parent.width/2 + 1,
                            parent.height * 11/12 + 1);
                        parent.text(tagText, parent.width/2 + 1,
                            parent.height * 11/12 - 1);
                        parent.text(tagText, parent.width/2 - 1,
                            parent.height * 11/12 + 1);
                        parent.text(tagText, parent.width/2 - 1,
                            parent.height * 11/12 - 1);

                        //text
                        parent.fill(0xffffffff);
                        parent.text(tagText, parent.width/2,
                            parent.height * 11/12);
                    }

                    else {
                    	//text shadow
                        parent.text(tagText, parent.width/2 + 1,
                            parent.height / 12 + 1);
                        parent.text(tagText, parent.width/2 + 1,
                            parent.height / 12 - 1);
                        parent.text(tagText, parent.width/2 - 1,
                            parent.height / 12 + 1);
                        parent.text(tagText, parent.width/2 - 1,
                            parent.height / 12 - 1);

                        //text
                        parent.fill(0xffffffff);
                        parent.text(tagText, parent.width/2,
                            parent.height / 12);
                    }
                }
            }
        }

        else {
            if(fade) {
                parent.tint(255, fadeAlpha);
            }
            parent.image(
                transitFrame,
                parent.width/2 + transitDelta[0],
                parent.height/2 + transitDelta[1]
            );

            if(isPlaying) {
                if(fade) {
                    fadeAlpha -= 255/25;

                    if(fadeAlpha <= 0) {
                        fadeAlpha = 255;

                        transit = false;

                        if(debug) {
                            Quickshow.println("slide show transition end");
                        }
                    }
                }

                else {
                    transitDelta[0] += (int)(1f / 25f * parent.width) *
                        transitDirection[0];
                    transitDelta[1] += (int)(1f / 25f * parent.height) *
                        transitDirection[1];

                    if(
                        (int)(1.5f*parent.width) <= transitDelta[0] ||
                        (int)(-1.5f*parent.width) >= transitDelta[0] ||
                        (int)(1.5f*parent.height) <= transitDelta[1] ||
                        (int)(-1.5f*parent.height) >= transitDelta[1]
                    ) {
                        transitDelta[0] = transitDelta[1] = 0;

                        transit = false;

                        if(debug) {
                            Quickshow.println("slide show transition end");
                        }
                    }
                }
            }
        }
    }

    /**
     * ControlP5 UI handler. Pauses and resumes slide show playback.
     * @param mode the new playback mode
     */
    public void playToggle(boolean mode){
        isPlaying = mode;

        if(debug) {
            Quickshow.println("slide show playing: " + isPlaying);
        }

        if(!isPlaying) {
            if(curAudioItem != null) {
                curAudioItem.getAudio().pause();
            }

            if(movie != null) {
                movie.pause();
            }
        }

        else {
            if(curAudioItem != null) {
                curAudioItem.getAudio().play();
            }

            if(movie != null) {
                movie.play();
            }
        }
    }

    /**
     * Calculates the dimensions of the VisualItem frame.
     */
    private void calcFrameDims() {
        float aspect = 1f * curFrame.width / curFrame.height;

        frameWidth = (curFrame.width > parent.width ?
            parent.width : curFrame.width);
        frameHeight = (int)(frameWidth / aspect);

        if(frameHeight > parent.height) {
            frameHeight = parent.height;
            frameWidth = (int)(frameHeight * aspect);
        }
    }

    /**
     * Prepares the next AudioItem in the playlist.
     */
    private void nextAudioItem() {
        if(shuffle && !audios.isEmpty()) {
            int shuffleIndex = (int)(Math.random()*audios.size());

            curAudioItem = audios.get(shuffleIndex);

            audios.remove(shuffleIndex);
        }

        else if(audioIter != null && audioIter.hasNext()) {
            curAudioItem = audioIter.next();
            curAudioItem.getAudio().play();
        }

        else {
            curAudioItem = null;
        }
    }

    /**
     * Prepares the next VisualItem in the playlist.
     */
    private void nextVisualItem() {
        movie = null;

        if(shuffle && !visuals.isEmpty()) {
            int shuffleIndex = (int)(Math.random()*visuals.size());

            curVisualItem = visuals.get(shuffleIndex);

            visuals.remove(shuffleIndex);
        }

        else if(visualIter != null && visualIter.hasNext()) {
            curVisualItem = visualIter.next();
        }

        else {
            stopButton();
        }

        if(curVisualItem != null) {
            curTagTexts.clear();
            curTagTimes.clear();

            tagStartTimes.clear();
            tagEndTimes.clear();

            curTagTexts.addAll(curVisualItem.getTagTexts());
            curTagTimes.addAll(curVisualItem.getTagTimes());

            tagText = "";

            Iterator<int[]> timeIter = curTagTimes.iterator();
            int[] time;
            while(timeIter.hasNext()) {
                time = timeIter.next();

                tagStartTimes.add(time[0]);
                tagEndTimes.add(time[1]);
            }

            if(curVisualItem.checkType().equals("video")) {
                movie = ((MovieItem)curVisualItem).getMovie();
                movie.play();
                movie.volume(0.0f);
            }

            else {
                curFrame = ((ImageItem)curVisualItem).getImage();
            }

            if(debug) {
                Quickshow.println(
                    "visual item type: " + curVisualItem.checkType() +
                    "\nduration: " + curVisualItem.getDisplayTime()
                );
            }
        }
    }

    /**
     * ControlP5 UI handler. Stops slide show playback.
     */
    public void stopButton() {
        transit = isEnabled = false;

        audioIter = null;
        if(curAudioItem != null) {
            curAudioItem.getAudio().pause();
        }
        curAudioItem = null;

        visualIter = null;
        curVisualItem = null;

        if(movie != null) {
            movie.stop();
            movie = null;
        }

        toggleUI(false);

        visuals.clear();
        audios.clear();

        parent.toggleMain(true);
    }

    /**
     * Retrieves the current play mode of the slide show.
     * @return true if the slide show is playing
     */
    public boolean isPlaying() {
        return isPlaying;
    }

    /**
     * Sets the visibility of the slide show UI components.
     * @param visible the visibility state
     */
    private void toggleUI(boolean visible) {
        group.setVisible(visible);

        playPause.setLock(!visible);

        stopButton.setLock(!visible);
    }

    /**
     * Retrieves the current state of the slide show.
     * @return true if the slide show is active
     */
    public boolean isEnabled() {
        return isEnabled;
    }

    /**
     * Enables the slide show and begins playback.
     */
    public void startPlaying() {
        isEnabled = true;

        playPause.setState(isEnabled);

        if(movie != null) {
            movie.play();
        }

        if(curAudioItem != null) {
            if(debug) {
                Quickshow.println("starting audio file");
            }
            curAudioItem.getAudio().play();
        }

        toggleUI(true);

        curImgTime = 0;
    }

    /**
     * Toggles the slide show shuffle.
     * @param shuffle whether or not to shuffle the slide show
     */
    private void toggleShuffle(boolean shuffle) {
        this.shuffle = shuffle;
    }

    /**
     * Callback method for handling keyboard events.
     * @param key the ASCII character of the pressed key
     * @param keyCode the code of the pressed key
     */
    public void keyPressed(char key, int keyCode) {
        if(debug) {
            Quickshow.println("Key pressed: '" + key +
                "'\nKey code: " + keyCode);
        }

        switch(key) {
        case ' ':
            playPause.toggle();
            break;

        case 'q':
            stopButton();
            break;
        }
    }

    /**
     * Toggles the slide show fade transition.
     * @param fade whether or not to fade transition between VisualItems
     */
    private void toggleFade(boolean fade) {
        this.fade = fade;
    }

    /**
     * Generates the new caption string, comprised of all captions starting
     *   before and ends after the current image timestamp.
     * @return String.
     */
    private String genTagString() {
        StringBuilder build = new StringBuilder();

        Iterator<String> textIter = curTagTexts.iterator();
        Iterator<int[]> timeIter = curTagTimes.iterator();
        int[] time;
        String tmp;

        while(textIter.hasNext()) {
            tmp = textIter.next();
            time = timeIter.next();

            if(curImgTime > time[0] && curImgTime < time[1]) {
                build.append(tmp).append('\n');
            }
        }

        return build.toString();
    }

    /**
     * An extension of the ControlP5 Toggle class that does not react in
     *   response to mouse onEnter events.
     * @author Kay Choi
     */
    private class NewToggle extends Toggle {
        /**
         * Class constructor.
         * @param control the instantiating ControlP5 object
         * @param label the label for the NewToggle
         */
        private NewToggle(ControlP5 control, String label) {
            super(control, label);
        }

        public void onEnter() {}
    }
}
