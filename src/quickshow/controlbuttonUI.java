/**
 * @file controlbuttonUI.java
 * @author Moses Lee, Kay Choi
 * @description A handler class for the main Quickshow window UI controls.
 */

package quickshow;

import controlP5.Button;
import controlP5.ControlP5;
import controlP5.ControlP5Constants;
import controlP5.Controller;
import controlP5.Group;
import controlP5.Toggle;

@SuppressWarnings("rawtypes")
public class controlbuttonUI {
    private Group mainUIGroup;
    private Button playButton;
    private Button resetShowButton;
    private Button clearSongsButton;
    private Button clearVisualTimeline;
    private Button selectAllVideos;
    private Button selectAllImages;
    private Button upButton;
    private Button downButton;
    private Toggle shuffleToggle, transitionToggle;
    private Button nextSlides;
    private Button prevSlides;
    private Button nextSong;
    private Button prevSong;
    private Button loadMedia;
    private Button editVisualItem;
    private Controller[] lockControllers;
    private Button pageIndex;
    private Button timeLineIndex;
    private Button totalTime;

    private String indexString = "0 of 0";
    private static final String timeLineDefaultString = "0:00 - 0:00";
    private String slideShowTime = "Total Time: 0:00";

    /**
     * Class constructor.
     * @param control the ControlP5 object handling UI elements
     */
    public controlbuttonUI(ControlP5 control){
        mainUIGroup = control.addGroup("buttonUI").setLabel("");

        lockControllers = new Controller[16];

        //For the entire slideshow
        lockControllers[0] = playButton = control.addButton("Play")
            .setPosition(30, 10)
            .setSize(70, 15)
            .setGroup(mainUIGroup);
        playButton.getCaptionLabel().alignX(ControlP5Constants.CENTER);

        lockControllers[1] = resetShowButton = control.addButton("Reset")
            .setPosition(110, 10)
            .setSize(70, 15)
            .setGroup(mainUIGroup);
        resetShowButton.getCaptionLabel().alignX(ControlP5Constants.CENTER);

        lockControllers[12] = shuffleToggle = control
            .addToggle("Shuffle Slides")
            .setPosition(530, 10)
            .setSize(15, 15)
            .setCaptionLabel(" Shuffle Slides")
            .setGroup(mainUIGroup);
        shuffleToggle.getCaptionLabel()
            .align(ControlP5Constants.RIGHT_OUTSIDE, ControlP5Constants.CENTER);

        //For audioList
        lockControllers[2] = clearSongsButton = control
            .addButton("Clear selected songs")
            .setPosition(670, 402)
            .setSize(200, 15)
            .setGroup(mainUIGroup);
        clearSongsButton.getCaptionLabel().alignX(ControlP5Constants.CENTER);

        //For the Thumbnail selector
        lockControllers[3] = selectAllImages = control
            .addButton("Select All Pictures")
            .setPosition(140, 402)
            .setSize(140,15)
            .setGroup(mainUIGroup);
        selectAllImages.getCaptionLabel().alignX(ControlP5Constants.CENTER);

        lockControllers[4] = selectAllVideos = control
            .addButton("Select All Clips")
            .setPosition(290, 402)
            .setSize(115, 15)
            .setGroup(mainUIGroup);
        selectAllVideos.getCaptionLabel().alignX(ControlP5Constants.CENTER);

        lockControllers[5] = clearVisualTimeline = control
        .addButton("Clear slides")
            .setPosition(30, 402)
            .setSize(100,15)
            .setGroup(mainUIGroup);
        clearVisualTimeline.getCaptionLabel().alignX(ControlP5Constants.CENTER);

        lockControllers[6] = upButton = control.addButton("Up")
            .setPosition(465, 402)
            .setSize(50,15)
            .setGroup(mainUIGroup);
        upButton.getCaptionLabel().alignX(ControlP5Constants.CENTER);

        lockControllers[7] = downButton = control.addButton("Down")
            .setPosition(600, 402)
            .setSize(50,15)
            .setGroup(mainUIGroup);
        downButton.getCaptionLabel().alignX(ControlP5Constants.CENTER);

        //To control the visual timeline thumbnail
        lockControllers[8] = nextSlides = control.addButton("Next")
            .setPosition(851, 500)
            .setSize(20, 77)
            .setCaptionLabel(">")
            .setGroup(mainUIGroup);
        nextSlides.getCaptionLabel().alignX(ControlP5Constants.CENTER);

        lockControllers[9] = prevSlides = control.addButton("Previous")
            .setPosition(30, 500)
            .setSize(20, 77)
            .setCaptionLabel("<")
            .setGroup(mainUIGroup);
        prevSlides.getCaptionLabel().alignX(ControlP5Constants.CENTER);

        timeLineIndex = control.addButton("timeLineIndex")
            .setPosition(413, 580)
            .setSize(76, 15)
            .setCaptionLabel(timeLineDefaultString)
            .lock()
            .setGroup(mainUIGroup);
        timeLineIndex.getCaptionLabel().alignX(ControlP5Constants.CENTER);

        totalTime = control.addButton("totalTime")
            .setPosition(740, 580)
            .setSize(130, 15)
            .setCaptionLabel(slideShowTime)
            .lock()
            .setGroup(mainUIGroup);
        totalTime.getCaptionLabel().alignX(ControlP5Constants.CENTER);

        //Load media
        lockControllers[10] = loadMedia = control.addButton("Load Media")
            .setPosition(670, 10)
            .setSize(200, 15)
            .setGroup(mainUIGroup);
        loadMedia.getCaptionLabel().alignX(ControlP5Constants.CENTER);

        lockControllers[11] = editVisualItem = control
            .addButton("Visual Item Properties")
            .setPosition(30, 580)
            .setSize(175, 15)
            .setVisible(false)
            .setGroup(mainUIGroup);
        editVisualItem.getCaptionLabel().alignX(ControlP5Constants.CENTER);

        lockControllers[13] = nextSong = control.addButton("nextSong")
            .setPosition(851, 421)
            .setSize(20, 77)
            .setCaptionLabel(">")
            .setGroup(mainUIGroup);
        nextSong.getCaptionLabel().alignX(ControlP5Constants.CENTER);

        lockControllers[14] = prevSong = control.addButton("prevSong")
            .setPosition(30, 421)
            .setSize(20, 77)
            .setCaptionLabel("<")
            .setGroup(mainUIGroup);
        prevSong.getCaptionLabel().alignX(ControlP5Constants.CENTER);

        pageIndex = control.addButton("pageIndex")
            .setPosition(520, 402)
            .setSize(75, 15)
            .setCaptionLabel(indexString)
            .lock()
            .setGroup(mainUIGroup);
        pageIndex.getCaptionLabel().alignX(ControlP5Constants.CENTER);

        lockControllers[15] = transitionToggle = control
            .addToggle("transitionToggle")
            .setGroup(mainUIGroup)
            .setPosition(400, 10)
            .setSize(15, 15)
            .setCaptionLabel(" Fade Transition");
        transitionToggle.getCaptionLabel()
            .align(ControlP5Constants.RIGHT_OUTSIDE, ControlP5Constants.CENTER);
    }

    /**
     * Toggles the visibility of the UI elements.
     * @param visible whether or not the UI elements should be visible
     */
    public void toggle(boolean visible) {
        mainUIGroup.setVisible(visible);
    }

    /**
     * Sets the page indexing string for the label.
     * @param pages the total number of pages
     * @param index the current page number
     */
    public void setPageIndex(int pages, int index){
        indexString = index + " of " + pages;
        pageIndex.setCaptionLabel(indexString);
    }

    /**
     * Sets the page indexing string for the visual timeline label.
     * @param stamps the start and end timestamps of the current timeline page
     */
    public void setTimeLinePageIndex(int[] stamps) {
        int min = 0, sec = 0;

        boolean okay = stamps[1] > 0;

        if(okay) {
            min = stamps[0]/60;
            sec = stamps[0]%60;
        }

        StringBuilder build = new StringBuilder(
        String.format("%d:%02d", min, sec));

        if(okay) {
            min = stamps[1]/60;
            sec = stamps[1]%60;
        }

        build.append(String.format(" - %d:%02d", min, sec));

        timeLineIndex.setCaptionLabel(build.toString());
    }

    /**
     * Sets the slideshow time display at the bottom of the screen.
     * @param length the slide show play time in seconds
     */
    public void setSlideShowTime(int length){
        int min = length/60;
        int sec = length%60;
        totalTime.setCaptionLabel(String.format("Total Time: %d:%02d", min, sec));
    }

    /**
     * Sets the main UI element interactivity.
     * @param lock whether to lock the main UI elements
     */
    public void setLock(boolean lock) {
        for(Controller control : lockControllers) {
            control.setLock(lock);
        }
    }

    /**
     * Sets the visibility of the VisualItem properties button.
     * @param show whether to show the button
     */
    public void showCaptionButton(boolean show) {
        editVisualItem.setVisible(show);
        editVisualItem.setLock(!show);
    }
}
