/**
 * @file VisualItem.java
 * @author Kay Choi
 * @description An abstract wrapper class for video and image media items.
 */

package quickshow.datatypes;

import java.util.ArrayList;

import processing.core.PImage;

public abstract class VisualItem extends MediaItem {
    private ArrayList<String> tagTexts;
    private ArrayList<int[]> tagTimes;
    private PImage thumb;
    protected int displayTime = 0;
    private boolean atBottom = false;

    /**
     * Class constructor.
     * @param fileName the file name of the media file to load
     * @param thumb the media item thumbnail
     */
    public VisualItem(String fileName, PImage thumb) {
        super(fileName);

        this.thumb = thumb;

        tagTexts = new ArrayList<String>();
        tagTimes = new ArrayList<int[]>();
    }

    /**
     * Retrieves the thumbnail associated with this MediaItem.
     * @return the thumbnail
     */
    public PImage getThumbnail() {
        return thumb;
    }

    /**
     * Retrieves all annotations associated with the VisualItem.
     * @return StringList
     */
    public ArrayList<String> getTagTexts() {
        return tagTexts;
    }

    /**
     * Removes all annotations from the VisualItem.
     */
    public void clearTags() {
        tagTexts.clear();
        tagTimes.clear();
    }

    /**
     * Retrieves all timestamps associated with the VisualItem.
     * @return an ArrayList of arrays of Floats
     */
    public ArrayList<int[]> getTagTimes() {
        return tagTimes;
    }

    /**
     * Retrieves the time that the VisualItem will be displayed.
     * @return the time in seconds
     */
    public int getDisplayTime() {
        return displayTime;
    }

    /**
     * Sets the VisualItem captions.
     * @param tags ArrayList containing caption text
     * @param tagTimes ArrayList containing caption timestamps
     */
    public void setTags(ArrayList<String> tags, ArrayList<int[]> tagTimes) {
        clearTags();

        tagTexts.addAll(tags);
        this.tagTimes.addAll(tagTimes);
    }

    /**
     * Checks the position of the captions for this VisualItem.
     * @return true if the captions will be at the bottom of the screen
     */
    public boolean isAtBottom() {
        return atBottom;
    }

    /**
     * Sets the position of the captions for this VisualItem.
     * @param atBottom whether or not the captions will be at the bottom of the
     *   screen
     */
    public void setAtBottom(boolean atBottom) {
        this.atBottom = atBottom;
    }
}
