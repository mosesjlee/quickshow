/**
 * @file PopupDialogue.java
 * @author Kay Choi
 * @description A popup dialogue for modifying VisualItem parameters in the
 *   slide show.
 */

package quickshow;

import java.util.ArrayList;

import quickshow.datatypes.ImageItem;
import quickshow.datatypes.VisualItem;
import controlP5.Button;
import controlP5.ControlEvent;
import controlP5.ControlP5;
import controlP5.ControlP5Constants;
import controlP5.Controller;
import controlP5.DropdownList;
import controlP5.Group;
import controlP5.Slider;
import controlP5.Textfield;
import controlP5.Toggle;

@SuppressWarnings("rawtypes")
public class PopupDialogue {
    private boolean debug;
    private Quickshow parent;

    private VisualItem item = null;
    private int tagIndex = -1;

    private ArrayList<String> tags;
    private ArrayList<int[]> tagTimes;

    private Group popupGroup;
    private static final int[] popupOrigin = {315, 150};
    private Slider imgDisplaySlider;
    private Button popupAccept, popupCancel;
    private Button tagAdd, tagRemove;
    private Textfield tagField, tagStartField, tagEndField;
    private Controller[] popupLock;
    private DropdownList tagList;
    private Toggle tagPosition;
    private static final String tagListLbl = "Add New Caption";
    private static final String[] toggleLbl = {"Bottom", "Top"};
    private int toggleIndex = 1;

    /**
     * Class constructor.
     * @param parent the instantiating Quickshow object
     * @param control the ControlP5 object handling UI elements
     */
    public PopupDialogue(Quickshow parent, ControlP5 control) {
        this.parent = parent;

        debug = parent.getDebugFlag();

        tags = new ArrayList<String>();
        tagTimes = new ArrayList<int[]>();

        popupGroup = control.addGroup("popupUI")
            .setVisible(false)
            .setLabel("");

        popupLock = new Controller[9];

        int[] lblOffset = new int[2];

        lblOffset[1] = 5;

        control.addTextarea("tagFieldLabel")
            .setPosition(popupOrigin[0], popupOrigin[1] + lblOffset[1] + 20)
            .setText("SET CAPTION TEXT")
            .setGroup(popupGroup);

        popupLock[0] = tagField = control.addTextfield("tagText")
            .setPosition(popupOrigin[0], popupOrigin[1] + lblOffset[1])
            .setSize(250, 20)
            .setCaptionLabel("")
            .setAutoClear(false)
            .setGroup(popupGroup);

        lblOffset[1] = 56;
        control.addTextarea("tagLabel")
            .setPosition(popupOrigin[0], popupOrigin[1] + lblOffset[1] + 2)
            .setText("CAPTION TIMESTAMPS (SEC)")
            .setGroup(popupGroup);

        lblOffset[0] = 41;
        control.addTextarea("tagStartLabel")
            .setPosition(popupOrigin[0] + lblOffset[0] - 41,
                popupOrigin[1] + lblOffset[1] + 22)
            .setText("START")
            .setGroup(popupGroup);
        popupLock[1] = tagStartField = control.addTextfield("tagStartField")
            .setPosition(popupOrigin[0] + lblOffset[0],
                popupOrigin[1] + lblOffset[1] + 20)
            .setSize(40, 20)
            .setCaptionLabel("")
            .setAutoClear(false)
            .setGroup(popupGroup);

        lblOffset[0] = 120;
        control.addTextarea("tagEndLabel")
            .setPosition(popupOrigin[0] + lblOffset[0] - 28,
                popupOrigin[1] + lblOffset[1] + 22)
            .setText("END")
            .setGroup(popupGroup);
        popupLock[2] = tagEndField = control.addTextfield("tagEndField")
            .setPosition(popupOrigin[0] + lblOffset[0],
                popupOrigin[1] + lblOffset[1] + 20)
            .setSize(40, 20)
            .setCaptionLabel("")
            .setAutoClear(false)
            .setGroup(popupGroup);

        popupLock[3] = imgDisplaySlider = control.addSlider("Display Time")
            .setSize(250, 10)
            .setNumberOfTickMarks(9)
            .setSliderMode(Slider.FLEXIBLE)
            .setPosition(popupOrigin[0], popupOrigin[1] + 115)
            .setRange(2f, 10f)
            .setValue(-1f)
            .setGroup(popupGroup);
        imgDisplaySlider.getCaptionLabel()
            .align(ControlP5.LEFT, ControlP5.BOTTOM_OUTSIDE).setPaddingX(0);

        popupLock[4] = tagPosition = control.addToggle("tagPosition")
            .setCaptionLabel(" Caption on " + toggleLbl[toggleIndex])
            .setSize(30, 15)
            .setPosition(popupOrigin[0], popupOrigin[1] + 160)
            .setGroup(popupGroup)
            .setMode(ControlP5Constants.SWITCH);
        tagPosition.getCaptionLabel()
            .align(ControlP5Constants.RIGHT_OUTSIDE, ControlP5Constants.CENTER);

        lblOffset[0] = 160;
        lblOffset[1] = 195;
        popupLock[5] = popupAccept = control.addButton("Accept")
            .setSize(90, 15)
            .setPosition(popupOrigin[0] + lblOffset[0],
                popupOrigin[1] + lblOffset[1])
            .setGroup(popupGroup);
        popupAccept.getCaptionLabel().alignX(ControlP5Constants.CENTER);

        popupLock[6] = popupCancel = control.addButton("Cancel")
            .setSize(90, 15)
            .setPosition(popupOrigin[0] + lblOffset[0],
                popupOrigin[1] + lblOffset[1] + 17)
            .setGroup(popupGroup);
        popupCancel.getCaptionLabel().alignX(ControlP5Constants.CENTER);

        popupLock[7] = tagAdd = control.addButton("tagAdd")
            .setCaptionLabel("Add Caption")
            .setSize(140, 15)
            .setPosition(popupOrigin[0], popupOrigin[1] + lblOffset[1])
            .setGroup(popupGroup);
        tagAdd.getCaptionLabel().alignX(ControlP5Constants.CENTER);

        popupLock[8] = tagRemove = control.addButton("tagRemove")
            .setCaptionLabel("Remove Caption")
            .setSize(140, 15)
            .setPosition(popupOrigin[0], popupOrigin[1] + lblOffset[1] + 17)
            .setGroup(popupGroup);
        tagRemove.getCaptionLabel().alignX(ControlP5Constants.CENTER);

        tagList = control.addDropdownList("tagList")
            .actAsPulldownMenu(true)
            .setGroup(popupGroup)
            .setCaptionLabel(tagListLbl)
            .setSize(250, 100)
            .setBarHeight(20)
            .setPosition(popupOrigin[0], popupOrigin[1]);
        tagList.getCaptionLabel().align(ControlP5Constants.LEFT,
            ControlP5Constants.CENTER);
        tagList.addItem(tagListLbl, -1);
    }

    /**
     * Toggles the popup dialogue.
     * @param toggle whether or not to show the popup dialogue
     * @param item the VisualItem being modified
     * @param offset the timeline offset
     */
    public void togglePopup(boolean toggle, VisualItem item, Integer offset) {
        this.item = item;

        popupGroup.setVisible(toggle);

        if(item != null) {
            if(item.checkType().equalsIgnoreCase("video")) {
                imgDisplaySlider.setVisible(false);
                imgDisplaySlider.lock();
            }

            else {
                imgDisplaySlider.setValue(item.getDisplayTime());
            }

            populateTags();
        }

        else {
            tagList.clear();
            tags.clear();
            tagTimes.clear();

            tagList.addItem(tagListLbl, -1);

            imgDisplaySlider.setValue(-1f);

            tagStartField.setText("");
            tagEndField.setText("");
            tagField.setText("");
        }
    }

    /**
     * Loads the VisualItem captions.
     */
    private void populateTags() {
        tags.addAll(item.getTagTexts());
        tagTimes.addAll(item.getTagTimes());

        String tmp;
        for(int i = 0; i < tags.size(); i++) {
            tmp = tags.get(i);
            if(tmp.length() >= 30) {
                tmp = tags.get(i).substring(0, 29)+"..";
            }

            tagList.addItem(tags.get(i), i);
        }

        tagIndex = -1;
    }

    /**
     * Checks the state of the popup dialogue.
     * @return true if popup dialogue is open
     */
    public boolean isEnabled() {
        return popupGroup.isVisible();
    }

    /**
     * Retrieves the Cartesian coordinates of the top left corner of the popup
     *   dialogue.
     * @return integer array
     */
    public int[] getPopupOrigin() {
        return popupOrigin;
    }

    /**
     * Retrieves the value of the tag Textfield.
     * @return String
     */
    private String getTagText() {
        String result = tagField.getText();

        tagField.setText("");

        return result;
    }

    /**
     * Retrieves the value of the start time Textfield.
     * @return integer
     */
    private int getTagStartTime() {
        int result;

        try {
            result = Math.abs(Integer.parseInt(tagStartField.getText()));
        }

        catch(NumberFormatException e) {
            result = 0;
        }

        tagStartField.setText("");

        return result;
    }

    /**
     * Retrieves the value of the end time Textfield.
     * @return integer
     */
    private int getTagEndTime() {
        int result;

        try {
            result = Math.abs(Integer.parseInt(tagEndField.getText()));
        }

        catch(NumberFormatException e) {
            result = 5;
        }

        tagEndField.setText("");

        return result;
    }

    /**
     * Callback method for drawing the PopupDialogue.
     */
    public void draw() {
        parent.stroke(0);
        parent.fill(0xff3333aa);
        parent.rect(popupOrigin[0]-10, popupOrigin[1]-32, 270, 270);
        parent.line(popupOrigin[0]-10, popupOrigin[1]+48,
            popupOrigin[0]+260, popupOrigin[1]+48);
        parent.line(popupOrigin[0]-10, popupOrigin[1]+105,
            popupOrigin[0]+260, popupOrigin[1]+105);
        parent.line(popupOrigin[0]-10, popupOrigin[1]+150,
            popupOrigin[0]+260, popupOrigin[1]+150);
        parent.line(popupOrigin[0]-10, popupOrigin[1]+185,
            popupOrigin[0]+260, popupOrigin[1]+185);
        parent.line(popupOrigin[0]+150, popupOrigin[1]+185,
            popupOrigin[0]+150, popupOrigin[1]+238);
    }

    /**
     * Callback method for handling ControlP5 UI events.
     * @param event the ControlEvent to handle
     */
    public void controlEvent(ControlEvent event) {
        switch(event.getName()) {
        case "Accept":

            if(item.checkType().equalsIgnoreCase("image")) {
                int duration = (int)imgDisplaySlider.getValue();
                ((ImageItem)item).setDisplayTime(duration);
                Quickshow.println("Item duration: " + duration + 's');
            }

            tagAdd();

            if(debug) {
                Quickshow.println("passing: "+tags.size());
            }

            item.setTags(tags, tagTimes);
            item.setAtBottom(tagPosition.getState());

            togglePopup(false, null, null);

            break;

        case "tagAdd":
            tagAdd();

            break;

        case "tagList":
            tagList((int)event.getValue());

            break;

        case "tagRemove":
            tagRemove();

            break;

        case "tagPosition":
            tagPosition();

            break;

        case "Cancel":
            togglePopup(false, null, null);

            break;
        }

    }

    /**
     * ControlP5 UI handler. Toggles the caption position setting.
     */
    private void tagPosition() {
        toggleIndex = (++toggleIndex) % 2;
        tagPosition.setCaptionLabel(" Caption on " +
            toggleLbl[toggleIndex]);

        if(debug) {
            Quickshow.println("Caption on bottom: " + tagPosition.getState());
        }
    }

    /**
     * Removes the current caption from the selected VisualItem.
     */
    private void tagRemove() {
        if(debug) {
            Quickshow.println("Current caption count: " + tags.size());
        }

        if(tagIndex >= 0) {
            tags.remove(tagIndex);
            tagTimes.remove(tagIndex);

            tagList.clear();
            tagList.addItem(tagListLbl, -1);

            String tmp;
            for(int i = 0; i < tags.size(); i++) {
                tmp = tags.get(i);
                if(tmp.length() >= 30) {
                    tmp = tags.get(i).substring(0, 29)+"..";
                }

                tagList.addItem(tags.get(i), i);
            }

            tagStartField.setText("");
            tagEndField.setText("");

            tagField.setText("");

            tagIndex = -1;
        }

        if(debug) {
            Quickshow.println("New caption count: " + tags.size());
        }

        tagList.setCaptionLabel(tagListLbl);
    }

    /**
     * Adds a new caption to the selected VisualItem.
     */
    private void tagAdd() {
        String text = getTagText();
        int[] time = {-1, -1};

        if(debug) {
            Quickshow.println("Caption text: " + text +
                "\nCurrent caption count: " + tags.size());
        }

        if(!text.trim().equals("")) {
            time[0] = getTagStartTime();
            time[1] = getTagEndTime();

            if(time[0] >= 0 && time[1] >= 0) {
                if(tagIndex >= 0) {
                    tags.set(tagIndex, text);
                    tagTimes.set(tagIndex, time);
                }

                else {
                    tags.add(text);
                    tagTimes.add(time);
                }

                if(text.length() >= 30) {
                    text = text.substring(0, 29) + "..";
                }

                if(debug) {
                    for(String tag:tags) {
                        Quickshow.println(tag);
                    }
                }

                if(tagIndex >= 0) {
                    tagList.getItem(tagIndex+1).setText(text);
                }

                else {
                    tagList.addItem(text, tags.size()-1);
                }
            }
        }

        if(debug) {
            Quickshow.println(
                "Caption start: " + (time[0] >= 0 ? time[0] : "N/A") +
                "s\nCaption end: " + (time[1] >= 0 ? time[1] : "N/A") +
                "s\nNew caption count: " + tags.size()
            );
        }

        tagList.setCaptionLabel(tagListLbl);
    }

    /**
     * ControlP5 UI handler. Switches the caption being edited.
     * @param index
     */
    private void tagList(int index) {
        if(index >= 0) {
            tagIndex = index;

            tagStartField.setText(Integer.toString(tagTimes.get(tagIndex)[0]));
            tagEndField.setText(Integer.toString(tagTimes.get(tagIndex)[1]));

            tagField.setText(tags.get(tagIndex));

            tagAdd.setCaptionLabel("Edit Caption");
        }

        else {
            tagIndex = -1;

            tagAdd.setCaptionLabel("Add Caption");

            tagStartField.setText("");
            tagEndField.setText("");

            tagField.setText("");
        }

        if(debug) {
            Quickshow.println("tag index: " + tagIndex);
        }
    }
}
