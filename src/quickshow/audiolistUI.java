/**
 * @file audiolistUI.java
 * @author Moses Lee
 * @description A class for displaying and selecting audio items.
 */

package quickshow;

import java.util.ArrayList;
import java.util.ListIterator;

import processing.data.IntList;
import quickshow.datatypes.AudioItem;
import controlP5.ControlEvent;
import controlP5.ControlP5;
import controlP5.Group;
import controlP5.ListBox;
import controlP5.ListBoxItem;

public class audiolistUI {
    private boolean debug;

    private Group group;

    private ListBox list;
    private int num_items = 0;
    private final static int width = 200;
    private final static int height = 350;
    private final static int MAX_SONGS = 3;
    private final static String title = "Songs/Audio";
    private ArrayList<AudioItem> songList;
    private IntList selectedIndex;

    private int oldListSize;

    /**
     * Class constructor.
     * @param parent the instantiating Quickshow object
     * @param control the ControlP5 object handling UI elements
     */
    public audiolistUI(Quickshow parent, ControlP5 control){
        debug = parent.getDebugFlag();

        group = control.addGroup("AudioList").setLabel("");
        control.setFont(control.getFont().getFont(), 15);
        list = control.addListBox(title)
            .setSize(width, height)
            .setPosition(670, 50)
            .disableCollapse()
            .setBarHeight(18);
        list.setGroup(group);

        //Vectors to store information about the Listbox
        songList = new ArrayList<AudioItem>();
        selectedIndex = new IntList();

        //Need to find a way to display the list without initializing
        ListBoxItem lbi;
        for (int i=0;i<25;i++) {
            lbi = list.addItem("empty", i);
            lbi.setColorBackground(0xffff0000);
            lbi.setId(i);
            lbi.setText("Empty");
        }

    }

    /**
     * Clear songs in the selectedSongList.
     */
    public void clearSelectedSongs() {
        for(Integer val : selectedIndex) {
            list.getItem(val).setColorBackground(0xffff0000);
        }
        selectedIndex.clear();
    }

    /**
     * Retrieves the selected songs.
     * @return an ArrayList containing the selected AudioItems
     */
    public ArrayList<AudioItem> returnSelectedSongList(){
        ArrayList<AudioItem> result = new ArrayList<AudioItem>();

        for(Integer index : selectedIndex) {
            result.add(songList.get(index));
        }

        return result;
    }

    /**
     * Adds the loaded songs to the available song list.
     * @param fileList an ArrayList containing the AudioItems to be added
     */
    public void receiveSongs(ArrayList <AudioItem> fileList) {
        if(debug) {
            Quickshow.println("Size of fileList: " + fileList.size());
        }

        //only add if items are not already in list
        AudioItem item;
        ListIterator<AudioItem> itemIter;
        int i;
        for(AudioItem vItem : fileList) {
            itemIter = songList.listIterator();
            i = 0;

            while(itemIter.hasNext()) {
                item = itemIter.next();

                if(vItem.getFileName().equalsIgnoreCase(item.getFileName())) {
                    break;
                }

                i++;
            }

            if(i == songList.size()) {
                songList.add(vItem);
            }
        }

        //Display the songs on the list
        ListIterator<AudioItem> songIter = songList.listIterator(oldListSize);
        while(songIter.hasNext()){
            addToList(songIter.next());
        }
        oldListSize = songList.size();
    }


    /**
     * Toggles display of the audiolistUI.
     * @param visible whether the audiolistUI should be visible
     */
    public void toggle(boolean visible){
        group.setVisible(visible);
    }

    /*
     * Helper functions
     */
    //TODO Make sure to add more songs in the future and update the list
    /**
     * Adds an item to the audio list.
     * @param audio the AudioItem to add
     */
    private void addToList(AudioItem audio){
        StringBuilder builder = new StringBuilder(audio.getAuthor() + " - "
            + audio.getTitle() + " - " + audio.getLength());

        //Generate the Label for the listBoxItem
        String songDisplay = (builder.length() >= 25 ?
            builder.substring(0, 24) + ".." : builder.toString());

        if(debug) {
            Quickshow.println("Song being added: " + songDisplay);
        }

        if(num_items > 25) {
            list.addItem(songDisplay, num_items);
        }

        else {
            list.getItem(num_items).setText(songDisplay);
        }
        //Adds the actual song
        num_items++;
    }

    /**
     * Callback method for handling ControlP5 UI events.
     * @param theEvent the ControlEvent to handle
     */
    public void controlEvent(ControlEvent theEvent) {
        int val = (int)theEvent.getGroup().getValue();

        if(MAX_SONGS > selectedIndex.size() && num_items > 0 && val < num_items) {
            if(selectedIndex.hasValue(val)) {
                selectedIndex.remove(selectedIndex.index(val));

                list.getItem(val).setColorBackground(0xffff0000);
            }

            else {
                selectedIndex.append(val);

                list.getItem(val).setColorBackground(0xff2299ff);
            }
        }

    }

}
