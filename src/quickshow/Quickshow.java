/**
 * @file Quickshow.java
 * @author Kay Choi, Moses Lee
 * @description The main Quickshow class. Functions as a hub for all other
 *   application components.
 */

package quickshow;

import java.util.ArrayList;

import processing.core.PApplet;
import processing.core.PFont;
import quickshow.datatypes.AudioItem;
import quickshow.datatypes.MediaItem;
import quickshow.datatypes.VisualItem;
import controlP5.ControlEvent;
import controlP5.ControlP5;
import ddf.minim.Minim;

@SuppressWarnings("serial")
public class Quickshow extends PApplet {
    private boolean debug = false;

    private ControlP5 control;
    private audiolistUI audioListbox;

    private Minim minim;

    private visualthumbnailUI thumbnails;

    private controlbuttonUI cbU;
    private FileBrowser browse;
    private slideShow show;
    private PopupDialogue popup;

    //Test variables for debug purposes
    private audioTimeline aT;
    private visualTimeline vTimeline;
    private VisualItem selectedItem = null;
    private int selectedItemIndex = -1;
    private int visualOffset;

    private boolean ignoreNextMouseEvent = false;

    //These variables are for the Visual Thumbnail UI to bound where the mouse responds

    public void setup() {
        size(900, 600);
        frameRate(24);

        if(frame != null) {
            frame.setTitle("Quickshow");
        }

        control = new ControlP5(this);
        control.setFont(control.getFont().getFont(), 15);

        minim = new Minim(this);

        show = new slideShow(this, control);

        audioListbox = new audiolistUI(this, control);

        cbU = new controlbuttonUI(control);

        popup = new PopupDialogue(this, control);

        PFont font = loadFont("data/SansSerif.plain-15.vlw");

        aT = new audioTimeline(this, minim, font);

        vTimeline = new visualTimeline(this, font);
        thumbnails = new visualthumbnailUI(this, font);

        browse = new FileBrowser(this, minim, control, ".", font);
    }

    public void draw() {
        background(38, 38, 38);

        if(browse.isEnabled()) {
            browse.draw();
        }

        else if(show.isEnabled()){
            show.draw();
        }

        else {
            //Background for the thumbnails
            aT.drawBackgroundCanvas();
            vTimeline.drawBackgroundCanvas();

            //This line is a place holder
            aT.drawWaveform();
            thumbnails.drawThumbNails();
            vTimeline.generateThumbnails();

            if(popup.isEnabled()) {
                popup.draw();
            }

            if(browse.isReady()) {
                closeFBActions();
            }

            //check if mouse over timelines, do popups
            mouseOver();
        }
    }

    /**
     * Callback method for handling ControlP5 UI events.
     * @param theEvent the initiating ControlEvent
     */
    public void controlEvent(ControlEvent theEvent) {
        String srcName = (
                theEvent.isController() ?
                theEvent.getController() :
                theEvent.getGroup()
            ).getParent().getName();

        if(debug) {
            println("Event source: " + srcName + "\nEvent name: " +
                theEvent.getName());
        }

        switch(srcName) {
        case "fileBrowser":
            if(browse.isEnabled()) {
                browse.controlEvent(theEvent);

                if(!browse.isEnabled()) {
                    if(debug) {
                        println("FileBrowser closed");
                    }
                    closeFBActions();
                }
            }
            break;

        case "buttonUI":
            switch(theEvent.getName()){
            case "Play":
                toggleMain(false);

                show.addAudio(audioListbox.returnSelectedSongList());
                show.addVisual(thumbnails.returnSelectedItems());
                show.startPlaying();

                break;

            case "Reset":
                thumbnails.clearSelectedItems();
                vTimeline.clearSelectedSlides();
                audioListbox.clearSelectedSongs();
                vTimeline.receiveSelectedItems(thumbnails.returnSelectedItems());
                cbU.setTimeLinePageIndex(vTimeline.getCurPageStamps());
                cbU.setSlideShowTime(vTimeline.getTotalTime());

                break;

            case "Shuffle Slides":
            case "transitionToggle":
                show.controlEvent(theEvent);

                break;

            case "Clear selected songs":
                audioListbox.clearSelectedSongs();
                aT.clear();

                break;

            case "Select All Pictures":
                thumbnails.selectAllImages();
                vTimeline.receiveSelectedItems(thumbnails.returnSelectedItems());
                cbU.setTimeLinePageIndex(vTimeline.getCurPageStamps());
                cbU.setSlideShowTime(vTimeline.getTotalTime());
                break;

            case "Select All Clips":
                thumbnails.selectAllClips();
                vTimeline.receiveSelectedItems(thumbnails.returnSelectedItems());
                cbU.setTimeLinePageIndex(vTimeline.getCurPageStamps());
                cbU.setSlideShowTime(vTimeline.getTotalTime());
                break;

            case "Clear slides":
                vTimeline.clearSelectedSlides();
                thumbnails.clearSelectedItems();
                vTimeline.receiveSelectedItems(thumbnails.returnSelectedItems());
                cbU.setTimeLinePageIndex(vTimeline.getCurPageStamps());
                cbU.setSlideShowTime(vTimeline.getTotalTime());
                break;

            case "Up":
                thumbnails.showPrevItems();
                cbU.setPageIndex(thumbnails.getNumPages(), thumbnails.getCurrIndex());

                break;

            case "Down":
                thumbnails.showNextItems();
                cbU.setPageIndex(thumbnails.getNumPages(), thumbnails.getCurrIndex());

                break;

            case "Next":
                vTimeline.showNextOnTimeline();
                cbU.setTimeLinePageIndex(vTimeline.getCurPageStamps());

                break;

            case "Previous":
                vTimeline.showPrevOnTimeline();
                cbU.setTimeLinePageIndex(vTimeline.getCurPageStamps());

                break;

            case "Load Media":
                browse.toggle(true);
                toggleMain(false);

                break;

            case "nextSong":
                aT.nextSong();
                break;

            case "prevSong" :
                aT.prevSong();
                break;

            case "Visual Item Properties":
                popup.togglePopup(true, selectedItem, visualOffset);

                if(popup.isEnabled()) {
                    cbU.setLock(true);
                }

                break;
            }
            break;

        case "popupUI":
            popup.controlEvent(theEvent);

            if(!popup.isEnabled()) {
                cbU.setLock(false);

                vTimeline.updateTimeStamps(selectedItemIndex);

                cbU.setTimeLinePageIndex(vTimeline.getCurPageStamps());
                cbU.setSlideShowTime(vTimeline.getTotalTime());

                ignoreNextMouseEvent  = true;
            }

            break;

        case "AudioList":
            if(!popup.isEnabled()) {
                audioListbox.controlEvent(theEvent);
                aT.receiveSelectedSongs(audioListbox.returnSelectedSongList());
            }

            break;

        case "slideShow":
            show.controlEvent(theEvent);
            break;
        }
    }

    public void mouseClicked() {
        if(browse.isEnabled()) {
            browse.mouseClicked(mouseX, mouseY);

            if(!browse.isEnabled()) {
                closeFBActions();
            }
        }

        else if(ignoreNextMouseEvent) {
            ignoreNextMouseEvent = false;
        }

        else if(!popup.isEnabled() && !ignoreNextMouseEvent) {
            selectedItem = null;
            vTimeline.setSelectedIndex(-1);

            //thumbnail window
            int[] bounds = thumbnails.getBounds();
            if(mouseX > bounds[0] && mouseX < bounds[2] &&
                mouseY > bounds[1] && mouseY < bounds[3])
            {
                thumbnails.selectImage(mouseX, mouseY);
                vTimeline.receiveSelectedItems(thumbnails.returnSelectedItems());
                cbU.setTimeLinePageIndex(vTimeline.getCurPageStamps());
                cbU.setSlideShowTime(vTimeline.getTotalTime());
            }

            else {
                //visual timeline
                bounds = visualTimeline.bounds;
                if(mouseX > bounds[0] && mouseX < bounds[2] &&
                    mouseY > bounds[1] && mouseY < bounds[3])
                {
                    selectedItemIndex = vTimeline.getTimelineIndex(mouseX);
                    vTimeline.setSelectedIndex(selectedItemIndex);
                    selectedItem = vTimeline.getItemAt(selectedItemIndex);

                    int[] tmp = vTimeline.getItemTimeStamps(selectedItemIndex);
                    if( tmp!= null){
                        visualOffset = tmp[0];
                    }
                }
            }

            cbU.showCaptionButton(selectedItem != null);
        }

    }

    public void mouseDragged() {
        if(browse.isEnabled()) {
            browse.mouseDragged(mouseX, mouseY);
        }
    }

    public void mouseReleased() {
        if(browse.isEnabled()) {
            browse.mouseReleased(mouseX, mouseY);
        }
    }

    public void mousePressed() {
        if(browse.isEnabled()) {
            browse.mousePressed(mouseX, mouseY);
        }
    }

    /**
     * Handler for mouse over events.
     */
    public void mouseOver() {
        if(!browse.isEnabled() && !popup.isEnabled()) {
            //audio timeline
            int[] bounds = audioTimeline.bounds;
            if(mouseX > bounds[0] && mouseX < bounds[2] + 1 &&
                mouseY > bounds[1] && mouseY < bounds[3])
            {
                aT.displayTimeMarkers(mouseX, mouseY);
            }

            else {
                //visual timeline
                bounds = visualTimeline.bounds;
                if(mouseX > bounds[0] && mouseX < bounds[2] &&
                    mouseY > bounds[1] && mouseY < bounds[3])
                {
                    vTimeline.displayTimeMarker(mouseX);
                }
            }
        }
    }

    /**
     * Additional actions to be taken when the FileBrowser is closed.
     */
    private void closeFBActions() {
        if(browse.isReady()) {
            ArrayList<MediaItem> results = browse.getResults();

            if(debug) {
                println("RESULT SIZE " + results.size());
            }

            if(browse.isAudioMode()) {
                ArrayList<AudioItem> audios = new ArrayList<AudioItem>();

                for(MediaItem item : results) {
                    audios.add((AudioItem)item);
                }
                audioListbox.receiveSongs(audios);
            }

            else {
                ArrayList<VisualItem> visuals = new ArrayList<VisualItem>();

                for(MediaItem item : results) {
                    visuals.add((VisualItem)item);
                }

                thumbnails.receiveVisualItems(visuals);
            }
        }
        cbU.setPageIndex(thumbnails.getNumPages(), thumbnails.getCurrIndex());
        toggleMain(true);
    }

    /**
     * Toggles the main Quickshow UI components.
     * @param visible whether or not to show the main UI components
     */
    public void toggleMain(boolean visible) {
        cbU.toggle(visible);
        audioListbox.toggle(visible);
    }

    /**
     * Retrieves the Quickshow debug status.
     * @return true if debug statements are enabled
     */
    public boolean getDebugFlag() {
        return debug;
    }

    /**
     * Main method for executing Quickshow as a Java application.
     * @param args command line arguments
     */
    public static void main(String[] args) {
        PApplet.main(new String[] { quickshow.Quickshow.class.getName() });
    }

    public void keyPressed() {
        if(show.isEnabled()) {
            show.keyPressed(key, keyCode);
        }
    }
}
