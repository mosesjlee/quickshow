/**
 * @file visualTimeline.java
 * @author Moses Lee, Kay Choi
 * @description Renders the Quickshow visual item timeline.
 */

package quickshow;

import java.util.ArrayList;
import java.util.ListIterator;

import processing.core.PConstants;
import processing.core.PFont;
import processing.core.PImage;
import quickshow.datatypes.VisualItem;

public class visualTimeline {
    private final static int timeLineWidth = 800;
    private final static int timeLineHeight = 78;
    private final static int SEC_PER_PAGE = 40;
    private final static int WIDTH_PER_SEC = timeLineWidth/SEC_PER_PAGE;
    private int start_index = 0;
    private float scaleFactor;
    private Quickshow parent;
    private boolean debug;
    private int curr_items_displayed = 0;
    private int totalTime = 0;
    private int selectedIndex = -1;
    private ArrayList<int[]> timeStamps;
    private ArrayList<int[]> timeLineBounds;
    final static int[] bounds = {50, 499, 850, 577};

    private ArrayList <VisualItem> itemsForDisplay;
	private PFont font;

    /**
     * Class constructor.
     * @param parent the instantiating Quickshow object
     */
    public visualTimeline(Quickshow parent, PFont font){
        this.parent = parent;
        this.font = font;
        itemsForDisplay = new ArrayList<VisualItem>();
        timeStamps = new ArrayList<int[]>();
        timeLineBounds = new ArrayList<int[]>();
        debug = parent.getDebugFlag();
    }

    /**
     * Drawing a simple background canvas
     */
    public void drawBackgroundCanvas() {
        parent.rectMode(PConstants.CORNER);
        parent.imageMode(PConstants.CENTER);

        parent.fill(90,90,90);
        parent.stroke(0);
        parent.rect(bounds[0], bounds[1], timeLineWidth, timeLineHeight);
    }

    /**
     * Generates thumbnails for the selected items.
     */
    public void generateThumbnails() {
        if(start_index < itemsForDisplay.size()) {
            ListIterator<VisualItem> itemIter = itemsForDisplay
                .listIterator(start_index);

            //If empty exit function
            if(itemIter.hasNext()) {
                parent.imageMode(PConstants.CORNER);
                PImage image;

                int drawIndex = bounds[0];
                int y, duration;
                float new_height, new_width;
                int width_by_sec;
                int j = start_index;
                VisualItem item;

                ListIterator<int[]> stampIter = timeStamps
                    .listIterator(start_index);
                int[] stamp;
                int startTime = timeStamps.get(start_index)[0];

                do {
                    stamp = stampIter.next();
                    item = itemIter.next();
                    image = item.getThumbnail();

                    //Resize image to fit on timeline w/ Aspect Ratio
                    if (image.height > timeLineHeight){
                        scaleFactor = 1.0f/((float) image.height/
                            (float) (timeLineHeight-15));
                    }

                    new_height = scaleFactor * image.height;
                    new_width = scaleFactor * image.width;

                    duration = (
                        stamp[1] <= startTime + SEC_PER_PAGE ?
                        item.getDisplayTime() :
                        (startTime + SEC_PER_PAGE - stamp[0])
                    );

                    width_by_sec = duration * WIDTH_PER_SEC;

                    if(new_width > width_by_sec) {
                        new_width = width_by_sec - 1;
                        new_height = (int)((float)new_width*image.height/image.width);
                    }

                    y = (bounds[3]+bounds[1]-(int)Math.ceil(new_height))/2;

                    parent.rectMode(PConstants.CORNER);
                    parent.fill(0x6f40E0D0);
                    parent.stroke(0);
                    parent.rect(drawIndex, bounds[1], width_by_sec, timeLineHeight);

                    parent.image(image, drawIndex+1, y, new_width, new_height);

                    if(selectedIndex == j) {
                        parent.fill(0x55ff3210);
                        parent.stroke(0xffff2233);
                        parent.rectMode(PConstants.CORNER);
                        parent.rect(drawIndex, bounds[1]+1, width_by_sec-1, timeLineHeight-2);
                    }

                    //Increment the x index
                    drawIndex += width_by_sec;

                    j++;
                } while(itemIter.hasNext() &&
                    stamp[1] <= startTime + SEC_PER_PAGE);
            }
        }
    }

    /**
     * Adds the selected visual items to the timeline.
     * @param selectedList an ArrayList containing the selected VisualItems
     */
    public void receiveSelectedItems(ArrayList<VisualItem> selectedList){
        itemsForDisplay.clear();
        timeStamps.clear();
        timeLineBounds.clear();
        totalTime = 0;

        int tmp;
        int[] times;
        for(VisualItem item : selectedList) {
            itemsForDisplay.add(item);

            tmp = item.getDisplayTime();

            times = new int[2];
            times[0] = totalTime;
            totalTime += tmp;
            times[1] = totalTime;

            timeStamps.add(times);
        }
    }

    /**
     * Removes all visual items from the timeline.
     */
    public void clearSelectedSlides(){
        timeLineBounds.clear();
        timeStamps.clear();

        ListIterator<VisualItem> iter = itemsForDisplay.listIterator();
        VisualItem item;
        while(iter.hasNext()) {
            item = iter.next();
            item.clearTags();

            if(item.checkType().equalsIgnoreCase("image")) {
                ((quickshow.datatypes.ImageItem)item).setDisplayTime(5);
            }

            iter.remove();
        }

        //Reset the display index
        start_index = 0;
        totalTime = 0;
        curr_items_displayed = 0;
    }

    /**
     * Goes to the next page on the timeline.
     */
    public void showNextOnTimeline() {
        if(start_index + 1 < itemsForDisplay.size()) {
            if(debug) {
                Quickshow.println((timeStamps.get(start_index)[0]+SEC_PER_PAGE)+
                    " "+totalTime + "\ntrue ");
            }

            if(timeStamps.get(start_index)[0]+SEC_PER_PAGE < totalTime) {
                start_index++;

                if(debug) {
                    Quickshow.print("true");
                }
            }

            if(debug) {
                Quickshow.println();
            }
        }
    }

    /**
     * Goes to the previous page on the timeline.
     */
    public void showPrevOnTimeline(){
        if(start_index > 0) {
            start_index--;
        }
    }

    /**
     * Returns the total length of time for the slide show
     */
    public int getTotalTime(){
        return totalTime;
    }

    /**
     * Displays the marker in the visual timeline and the thumbnail
     *   that the image is hovering over.
     * @param mouseX the x-coordinate of the mouse
     */
    public void displayTimeMarker(int mouseX){
        int index = getTimelineIndex(mouseX);

        //If legal index was found then generate the marker and preview thumbnail
        if(index > -1) {
            PImage prevThumbnail = itemsForDisplay.get(index).getThumbnail();

            parent.fill(0xff555555);
            parent.stroke(0);
            parent.rectMode(PConstants.CORNER);

            int x_coord, align, textOffset;

            if(mouseX < 450) {
                x_coord = mouseX;
                align = PConstants.LEFT;
                textOffset = 5;
            }

            else {
                x_coord = mouseX-prevThumbnail.width;
                align = PConstants.RIGHT;
                textOffset = -5;
            }

            parent.rect(x_coord, bounds[1]-60,
        		prevThumbnail.width, prevThumbnail.height);

            parent.image(prevThumbnail, x_coord, bounds[1]-60);
            parent.stroke(0xffff0055);
            parent.line(mouseX, bounds[1] + 2 , mouseX, bounds[3] - 2);

            int[] stamp = timeStamps.get(index);
            String text = String.format("%d:%02d - %d:%02d", stamp[0]/60,
        		stamp[0]%60, stamp[1]/60, stamp[1]%60);

            parent.textFont(font);
            parent.textAlign(align);

            //text shadow
            parent.fill(0);
            parent.text(text, mouseX + textOffset + 1,
        		bounds[1] - 40 + prevThumbnail.height + 1);
            parent.text(text, mouseX + textOffset + 1,
        		bounds[1] - 40 + prevThumbnail.height - 1);
            parent.text(text, mouseX + textOffset - 1,
    			bounds[1] - 40 + prevThumbnail.height + 1);
            parent.text(text, mouseX + textOffset - 1,
        		bounds[1] - 40 + prevThumbnail.height - 1);

            //text
            parent.fill(0xffffffff);
            parent.text(text, mouseX + textOffset,
        		bounds[1] - 40 + prevThumbnail.height);
        }
    }

    /**
     * Retrieves the VisualItem at the specified index.
     * @param index the index of the VisualItem
     * @return VisualItem
     * @return null if index is out of bounds
     */
    public VisualItem getItemAt(int index) {
        return (index < 0 || index >= itemsForDisplay.size()) ?
            null : itemsForDisplay.get(index);
    }

    /**
     * Retieves the timestamps of the VisualItem at the specified index.
     * @param index the index of the VisualItem
     * @return int array containing the start and stop times
     * @return null if index is out of bounds
     */
    public int[] getItemTimeStamps(int index) {
        return (index < 0 || index >= timeStamps.size()) ?
            null : timeStamps.get(index);
    }

    /**
     * Determines the index of the VisualItem at the mouse pointer.
     * @param mouseX the x-coordinates of the mouse
     * @return integer
     */
    public int getTimelineIndex(int mouseX) {
        int index = -1;

        if(start_index < timeStamps.size()) {
            ListIterator<int[]> stampIter = timeStamps
                .listIterator(start_index);

            int[] stamp;

            if(stampIter.hasNext()) {
                int i = start_index;

                //get second associated with x-position
                int time = ((mouseX - bounds[0])/WIDTH_PER_SEC) +
                    timeStamps.get(start_index)[0];

                //find the image at current second
                do {
                    stamp = stampIter.next();

                    if(time >= stamp[0] &&
                        time < stamp[1])
                    {
                        index = i;
                        break;
                    }

                    i++;
                } while(stampIter.hasNext());
            }
        }

        return index;
    }

    /**
     * Updates the VisualItem timestamps.
     * @param index the index of the edited VisualItem
     */
    public void updateTimeStamps(int index) {
        if(debug) {
            Quickshow.println(""+itemsForDisplay.size()+' '+timeStamps.size());
        }

        if(index < itemsForDisplay.size()) {
            ListIterator<VisualItem> itemIter = itemsForDisplay.listIterator();

            if(itemIter.hasNext()) {
                int i = 0, tmp;
                int[] stamps;

                totalTime = 0;

                do {
                    totalTime += itemIter.next().getDisplayTime();
                    i++;
                } while(itemIter.hasNext() && i < index);

                ListIterator<int[]> stampIter = timeStamps.listIterator(i);
                while(itemIter.hasNext()) {
                    tmp = itemIter.next().getDisplayTime();

                    stamps = stampIter.next();

                    stamps[0] = totalTime;
                    totalTime += tmp;
                    stamps[1] = totalTime;
                }
            }
        }
    }


    /**
     * Designates an item on the timeline as selected.
     * @param index the index of the VisualItem to select
     */
    public void setSelectedIndex(int index) {
        selectedIndex = index;
    }

    /**
     * Retrieves the index of the first visible item on the timeline.
     * @return integer
     */
    public int getStartIndex(){
        return start_index;
    }

    /**
     * Retrieves the timestamps of the currently displayed timeline items.
     * @return integer array
     */
    public int[] getCurPageStamps() {
        int[] result = {0, 0};

        curr_items_displayed = 0;

        if(start_index < timeStamps.size()) {
            ListIterator<int[]> stampIter = timeStamps.listIterator(start_index);

            if(stampIter.hasNext()) {
                int[] stamp = stampIter.next();

                result[0] = stamp[0];
                result[1] = stamp[1];

                curr_items_displayed++;

                while(stampIter.hasNext() && result[1] < result[0] + SEC_PER_PAGE) {
                    stamp = stampIter.next();

                    result[1] = stamp[1];

                    curr_items_displayed++;
                }
            }

            if(debug) {
                Quickshow.println("start index: " + start_index +
                    "\nitems displayed: " + curr_items_displayed +
                    "\ncurrent page bounds: " + result[0] + '-' +
                    result[1] + " of " + totalTime);
            }
        }

        return result;
    }
}
