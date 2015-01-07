/**
 * @file FileBrowser.java
 * @author Kay Choi
 * @description The Quickshow file browser class.
 */

package quickshow;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.ListIterator;

import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PFont;
import processing.core.PImage;
import processing.data.IntList;
import processing.video.Movie;
import quickshow.datatypes.AudioItem;
import quickshow.datatypes.FileExtensions;
import quickshow.datatypes.ImageItem;
import quickshow.datatypes.MediaItem;
import quickshow.datatypes.MovieItem;
import controlP5.Button;
import controlP5.ControlEvent;
import controlP5.ControlP5;
import controlP5.ControlP5Constants;
import controlP5.Controller;
import controlP5.DropdownList;
import controlP5.Group;
import controlP5.Textfield;

@SuppressWarnings("rawtypes")
public class FileBrowser {
    private boolean debug;

    private Quickshow parent;
    private String curDir;
    private static char separator;

    private ArrayList<String> fileNames;
    private ArrayList<PImage> thumbs;

    private ArrayList<QItem> q;
    private ListIterator<QItem> qIter;
    private QItem curQItem = null;
    private Movie movie = null;

    private IntList selectedIndex;

    private ArrayList<MediaItem> results;

    private int[] selectBox = {0, 0, 0, 0};
    private boolean isSelecting = false;

    private long clickTime = 0;
    private boolean dblClick = false;

    private Group group;
    private Controller[] controllers;
    private Textfield pathField;
    private Button pageLabel;
    private DropdownList mediaTypeList;

    private ddf.minim.Minim minim;

    private int curDisplayIndex = 0;

    private boolean isAudioMode = false;

	private PFont fileFont;

    private static final int thumbWidth = 136, thumbHeight = 102;

    /**
     * Class constructor.
     * @param parent the instantiating Quickshow object
     * @param minim the Minim object handling the audio files
     * @param control the ControlP5 object handling UI elements
     * @param curDir the initial FileBrowser directory
     */
    public FileBrowser(Quickshow parent, ddf.minim.Minim minim,
        ControlP5 control, String curDir, PFont font)
    {
        this.parent = parent;

        fileFont = font;

        debug = parent.getDebugFlag();
        
        separator = File.separatorChar;

        String[] pathParts = (new File(curDir)).getAbsolutePath().split("\\" +
            separator);
        StringBuilder path = new StringBuilder();

        if(debug) {
            PApplet.print("curDir: ");
            for(String part:pathParts) {
                PApplet.print(part + separator);
            }
            Quickshow.println("\ncurDir depth: " + pathParts.length);
        }

        for(short i = 0; i < pathParts.length - 1; i++) {
            path.append(pathParts[i] + separator);
        }

        path.deleteCharAt(path.length()-1);

        this.curDir = path.toString();

        this.minim = minim;

        fileNames = new ArrayList<String>();
        thumbs = new ArrayList<PImage>();
        q = new ArrayList<QItem>();
        qIter = q.listIterator();

        selectedIndex = new IntList(20);

        results = new ArrayList<MediaItem>();

        group = control.addGroup("fileBrowser").setLabel("").setVisible(false);

        controllers = new Controller[8];

        controllers[7] = pathField = control.addTextfield("pathField")
            .setCaptionLabel("")
            .setText(this.curDir)
            .setPosition(30, 30)
            .setSize(780, 30)
            .setFocus(false)
            .setAutoClear(false)
            .setGroup(group);

        controllers[0] = control.addButton("openButton")
            .setCaptionLabel("Open")
            .setPosition(750, 540)
            .setSize(55, 30)
            .setGroup(group);
        controllers[0].getCaptionLabel().align(ControlP5Constants.CENTER,
            ControlP5Constants.CENTER);

        controllers[1] = control.addButton("cancelButton")
            .setCaptionLabel("Cancel")
            .setSize(55, 30)
            .setPosition(815, 540)
            .setGroup(group);
        controllers[1].getCaptionLabel().align(ControlP5Constants.CENTER,
            ControlP5Constants.CENTER);

        controllers[2] = control.addButton("scrollUpButton")
            .setSize(30, 75)
            .setPosition(840, 146)
            .setCaptionLabel("^")
            .setGroup(group);
        controllers[2].getCaptionLabel().align(ControlP5Constants.CENTER,
            ControlP5Constants.CENTER);

        controllers[3] = control.addButton("scrollTopButton")
            .setSize(30, 75)
            .setPosition(840, 71)
            .setCaptionLabel("^\n^")
            .setGroup(group);
        controllers[3].getCaptionLabel().align(ControlP5Constants.CENTER,
            ControlP5Constants.CENTER);

        controllers[4] = control.addButton("scrollDownButton")
            .setSize(30, 75)
            .setPosition(840, 380)
            .setCaptionLabel("v")
            .setGroup(group);
        controllers[4].getCaptionLabel().align(ControlP5Constants.CENTER,
            ControlP5Constants.CENTER);

        controllers[5] = control.addButton("scrollBottomButton")
            .setSize(30, 75)
            .setPosition(840, 455)
            .setCaptionLabel("v\nv")
            .setGroup(group);
        controllers[5].getCaptionLabel().align(ControlP5Constants.CENTER,
            ControlP5Constants.CENTER);

        controllers[6] = control.addButton("parentDirButton")
            .setCaptionLabel("..")
            .setGroup(group)
            .setPosition(815, 30)
            .setSize(55, 30);
        controllers[6].getCaptionLabel().align(ControlP5Constants.CENTER,
            ControlP5Constants.CENTER);

        String label = "Visual (bmp, jpg, png, gif, mov, avi, mpg, mp4)";
        mediaTypeList = control.addDropdownList("mediaTypeList")
            .setCaptionLabel(label)
            .setPosition(30, 570)
            .setSize(710, 30)
            .setBarHeight(30)
            .setGroup(group);
        mediaTypeList.getCaptionLabel().align(ControlP5Constants.LEFT,
            ControlP5Constants.CENTER);
        mediaTypeList.addItem(label, 0);
        mediaTypeList.addItem("Audio (mp3, wav, aiff, au, snd)", 1);

        pageLabel = control.addButton("pageLabel")
            .setLock(true)
            .setPosition(840, 225)
            .setSize(30, 151)
            .setCaptionLabel("")
            .setGroup(group);
        pageLabel.getCaptionLabel().align(ControlP5Constants.CENTER,
            ControlP5Constants.TOP);

        changeDir(this.curDir);
    }

    /**
     * Callback method for handling ControlP5 UI events.
     * @param e the ControlEvent to handle
     */
    public void controlEvent(ControlEvent e) {
        switch(e.getName()) {
        case "scrollUpButton":
            scrollUpButton();
            break;

        case "scrollDownButton":
            scrollDownButton();
            break;

        case "parentDirButton":
            parentDirButton();
            break;

        case "scrollTopButton":
            scrollTopButton();
            break;

        case "scrollBottomButton":
            scrollBottomButton();
            break;

        case "pathField":
            String path = pathField.getText().trim();
            if(!path.equals("")) {
                File file = new File(path);

                if(file.exists()) {
                    if(file.isDirectory()) {
                        changeDir(path);
                    }

                    else {
                        loadFile(file);

                        if(!results.isEmpty()) {
                            toggle(false);
                        }
                    }
                }
            }

            pathField.setFocus(false).setText(curDir);

            break;

        case "openButton":
            openButton();

            break;

        case "cancelButton":
            cancelButton();
            break;

        case "mediaTypeList":
            mediaTypeList(e);
            break;
        }
    }

    /**
     * Loads a single file.
     * @param file a Java File object representing the file to load
     */
    private void loadFile(File file) {
        String path;

        try {
            path = file.getCanonicalPath();

            if(debug) {
                Quickshow.println("loading file: " + path);
            }
        }

        catch (IOException e1) {
            if(debug) {
                e1.printStackTrace();
            }

            return;
        }

        String[] fileNameParts = path.split("\\.");

        int i;
        for(i = 0; i < FileExtensions.AUDIO_EXT.length; i++) {
            if(fileNameParts[fileNameParts.length-1]
                .equalsIgnoreCase(FileExtensions.AUDIO_EXT[i]))
            {
                results.add(new AudioItem(minim, path));

                break;
            }
        }

        if(i == FileExtensions.AUDIO_EXT.length) {
            for(i = 0; i < FileExtensions.VIDEO_EXT.length; i++) {
                if(fileNameParts[fileNameParts.length-1]
                    .equalsIgnoreCase(FileExtensions.VIDEO_EXT[i]))
                {
                    qIter.add(new QItem(-1, path));

                    getNextQItem();

                    break;
                }
            }

            if(i == FileExtensions.VIDEO_EXT.length) {
                PImage thumb;
                int[] thumbDims;

                for(i = 0; i < FileExtensions.IMG_EXT.length; i++) {
                    if(fileNameParts[fileNameParts.length-1]
                        .equalsIgnoreCase(FileExtensions.IMG_EXT[i]))
                    {
                        thumb = parent.loadImage(path);

                        thumbDims = newImageDims(thumb);
                        thumb.resize(thumbDims[0], thumbDims[1]);

                        results.add(new ImageItem(parent, path, thumb));

                        break;
                    }
                }
            }
        }

        if(debug) {
            Quickshow.println("Files loaded: " + results.size());
        }
    }

    /**
     * Retrieves the next video queue item for thumbnail generation.
     */
    private void getNextQItem() {
        if(qIter.hasPrevious()) {
            if(curQItem == null || curQItem.isHandled) {
                curQItem = qIter.previous();

                qIter.remove();

                movie = new Movie(parent, curQItem.name);
                movie.play();
            }
        }

        else {
            curQItem = null;
        }
    }

    /**
     * ControlP5 UI handler. Changes to parent directory if applicable.
     */
    private void parentDirButton() {
        File file = (new File(curDir)).getParentFile();
        String parentName = "";

        if(file != null) {
            parentName = file.getAbsolutePath();
            changeDir(parentName);
        }
    }

    /**
     * ControlP5 UI handler. Closes the FileBrowser without loading any items.
     */
    private void cancelButton() {
        selectedIndex.clear();
        results.clear();

        toggle(false);
    }

    /**
     * ControlP5 UI handler. Enters selected directory or loads selected files.
     */
    private void openButton() {
        switch(selectedIndex.size()) {
        case 0:
            loadAll();

            break;

        case 1:
            File file = new File(curDir + separator +
                fileNames.get(selectedIndex.get(0)));

            if(file.isDirectory()) {
                changeDir(file.getAbsolutePath());

                return;
            }

        default:
            if(isAudioMode) {
                loadAudio();
            }

            else {
                loadVisual();
            }
        }

        selectedIndex.clear();

        if(!results.isEmpty()) {
            toggle(false);
        }
    }

    /**
     * Generates thumbnails when paginating through a directory if necessary.
     */
    private void updateThumbs(int displayIndex) {
        if(displayIndex < fileNames.size()) {
            ListIterator<String> fileIter = fileNames
                .listIterator(displayIndex);

            if(fileIter.hasNext()) {
                String fullPath;
                String[] fileNameParts;
                PImage thumb;
                PImage thumb1 = parent.loadImage("data" + separator + "img" +
                    separator + "folderThumbNail.png");
                PImage thumb2 = parent.loadImage("data" + separator + "img" +
                    separator + "audioThumbNail.png");
                int[] thumbDims = newImageDims(thumb1);

                thumb1.resize(thumbDims[0], thumbDims[1]);
                thumb2.resize(thumbDims[0], thumbDims[1]);

                short j;
                int i = displayIndex;

                ListIterator<PImage> thumbIter = thumbs
                    .listIterator(displayIndex);
                String fileName;

                do {
                    thumb = thumbIter.next();
                    fileName = fileIter.next();

                    if(thumb == null) {
                        fullPath = curDir + separator + fileName;

                        //directory thumbnail
                        thumb = thumb1;

                        //non-directory thumbnail
                        if(!(new File(fullPath)).isDirectory()) {
                            if(isAudioMode) {       //audio thumbnail
                                thumb = thumb2;
                            }

                            else {
                                fileNameParts = fileName.split("\\.");

                                //image thumbnail
                                for(j = 0; j < FileExtensions.IMG_EXT.length;
                                    j++)
                                {
                                    if(
                                        fileNameParts[fileNameParts.length-1].
                                        equalsIgnoreCase(FileExtensions
                                            .IMG_EXT[j])
                                    ) {
                                        thumb = parent.loadImage(fullPath);

                                        thumbDims = newImageDims(thumb);
                                        thumb.resize(thumbDims[0],
                                            thumbDims[1]);

                                        break;
                                    }
                                }

                                //queue video for thumbnail generation
                                if(j == FileExtensions.IMG_EXT.length) {
                                    qIter.add(new QItem(i, fullPath));

                                    thumb = null;
                                }
                            }
                        }

                        //set new thumbnail
                        thumbs.set(i, thumb);
                    }

                    i++;
                } while(fileIter.hasNext() && i < displayIndex + 20);

                getNextQItem();
            }
        }

    }

    /**
     * ControlP5 UI handler. Displays the previous page.
     */
    private void scrollUpButton() {
        selectedIndex.clear();

        if(curDisplayIndex > 0) {
            curDisplayIndex -= 20;

            updateThumbs(curDisplayIndex);

            int lastPage = (int)(Math.ceil(fileNames.size()/20.));

            pageLabel.setCaptionLabel("\n\n\n" + ((curDisplayIndex/20) + 1) +
                "\n\nof\n\n" + lastPage);
        }

        if(debug) {
            Quickshow.println("curDisplayIndex: " + curDisplayIndex);
        }
    }

    /**
     * ControlP5 UI handler. Displays the first page.
     */
    private void scrollTopButton() {
        selectedIndex.clear();

        curDisplayIndex = 0;

        updateThumbs(curDisplayIndex);

        int lastPage = (int)(Math.ceil(fileNames.size()/20.));

        pageLabel.setCaptionLabel("\n\n\n1\n\nof\n\n" +
            lastPage);

        if(debug) {
            Quickshow.println("curDisplayIndex: " + curDisplayIndex);
        }
    }

    /**
     * ControlP5 UI handler. Displays the next page.
     */
    private void scrollDownButton() {
        selectedIndex.clear();

        if(curDisplayIndex + 20 < fileNames.size()) {
            curDisplayIndex += 20;

            updateThumbs(curDisplayIndex);

            int lastPage = (int)(Math.ceil(fileNames.size()/20.));

            pageLabel.setCaptionLabel("\n\n\n" + ((curDisplayIndex/20) + 1) +
                "\n\nof\n\n" + lastPage);
        }

        if(debug) {
            Quickshow.println("curDisplayIndex: " + curDisplayIndex);
        }
    }

    /**
     * ControlP5 UI handler. Displays the last page.
     */
    private void scrollBottomButton() {
        selectedIndex.clear();

        int lastPage = (int)(Math.ceil(fileNames.size()/20.));

        curDisplayIndex = (lastPage - 1) * 20;

        updateThumbs(curDisplayIndex);

        pageLabel.setCaptionLabel("\n\n\n" + lastPage +
            "\n\nof\n\n" + lastPage);

        if(debug) {
            Quickshow.println("curDisplayIndex: " + curDisplayIndex);
        }
    }

    /**
     * ControlP5 UI handler. Switches the media file type being scanned for.
     * @param e the initiating ControlEvent
     */
    private void mediaTypeList(ControlEvent e) {
        isAudioMode = e.getValue() != 0;

        changeDir(curDir);
    }

    /**
     * Callback method for drawing the FileBrowser UI.
     */
    public void draw() {
        if(dblClick && System.currentTimeMillis() - 500 > clickTime) {
            dblClick = false;
        }

        if(movie != null) {
            if(movie.available()) {
                movie.read();

                int[] thumbDims = newImageDims(movie);

                PImage thumb = movie.get();

                movie.stop();
                movie = null;

                thumb.resize(thumbDims[0], thumbDims[1]);

                if(curQItem.index >= 0) {
                    thumbs.set(curQItem.index, thumb);
                }

                else {
                    results.add(new MovieItem(parent, curQItem.name, thumb));

                    toggle(false);
                }

                curQItem.isHandled = true;

                getNextQItem();
            }
        }

        //draw thumbnail window
        parent.fill(0xff5a5a5a);
        parent.stroke(0);
        parent.rectMode(PConstants.CORNERS);
        parent.rect(30, 70, 839, 530);
        parent.noFill();
        parent.rect(839, 70, 870, 530);

        //thumbnail pic
        parent.imageMode(PConstants.CENTER);

        //filename
        parent.textAlign(PConstants.CENTER, PConstants.CENTER);
        parent.textFont(fileFont);

        //selected highlight
        parent.rectMode(PConstants.CENTER);
        parent.stroke(0xff5522ff);

        short row, col, imgIndex;
        String fileName;
        int i = curDisplayIndex;

        ListIterator<PImage> thumbIter = thumbs.listIterator(curDisplayIndex);
        PImage thumb;
        for(imgIndex = 0, row = 0; row < 4; row++) {
            //draw thumbnail rows
            for(col = 0; col < 5 && i < fileNames.size();
                col++, imgIndex++, i++)
            {
                thumb = thumbIter.next();

                //draw thumbnail columns
                if(thumb != null) {
                    parent.image(thumb, 109 + col*162, 125 + row*115);
                }

                fileName = fileNames.get(i);

                if(fileName.length() >= 15) {
                    fileName = fileName.substring(0, 14) + "..";
                }

                //text shadow
                parent.fill(0);
                parent.text(fileName, 112 + col*162, 174 + row*115);
                parent.text(fileName, 112 + col*162, 172 + row*115);
                parent.text(fileName, 110 + col*162, 174 + row*115);
                parent.text(fileName, 110 + col*162, 172 + row*115);

                //text
                parent.fill(0xffffffff);
                parent.text(fileName, 111 + col*162, 173 + row*115);

                parent.noFill();
                if(selectedIndex.size() > 0 &&
                    selectedIndex.hasValue((int)imgIndex+curDisplayIndex))
                {
                    parent.rect(109 + col*162, 128 + row*115,
                		thumbWidth + 10, thumbHeight + 10);
                }
            }
        }

        //draw selection box
        if(isSelecting) {
            parent.rectMode(PConstants.CORNERS);
            parent.stroke(0xff00FF5E);
            parent.rect(selectBox[0], selectBox[1], selectBox[2], selectBox[3]);
        }
    }

    /**
     * Determines the thumbnail dimensions of an image.
     * @param image the image to scale
     * @return an integer array containing the new image dimensions
     */
    private int[] newImageDims(PImage image) {
        int[] results = new int[2];

        float aspect = 1f * image.width / image.height;

        if(aspect > 1f) {
            results[0] = thumbWidth;
            results[1] = (int)(results[0] / aspect);
        }

        else {
            results[1] = thumbHeight;
            results[0] = (int)(results[1] * aspect);
        }

        return results;
    }

    /**
     * Changes the FileBrowser directory.
     * @param newDir the new directory path
     */
    private void changeDir(String newDir) {
        File file = new File(newDir);

        if(file.exists()) {
            try {
                curDir = file.getCanonicalPath();
            }
            catch (IOException e) {
                return;
            }

            thumbs.clear();
            fileNames.clear();
            selectedIndex.clear();

            curDisplayIndex = 0;

            if(debug) {
                Quickshow.println("cd " + curDir + "\nls");
            }

            ArrayList<File> files =
                new ArrayList<File>(java.util.Arrays.asList(file.listFiles()));
            ListIterator<File> fileIter = files.listIterator();

            String fileName, fullPath;
            String[] fileNameParts;
            short i, j;
            int[] thumbDims;

            PImage thumb = parent.loadImage("data" + separator + "img" + 
                separator + "folderThumbNail.png");
            thumb.resize(thumbWidth, thumbHeight);

            pathField.setText(file.getAbsolutePath());

            //directories listed first
            j = 0;
            while(fileIter.hasNext()) {
                file = fileIter.next();

                if(file.isDirectory()) {
                    fileNames.add(file.getName());

                    if(j < 20) {
                        thumbs.add(thumb);
                        j++;
                    }

                    else {
                        thumbs.add(null);
                    }

                    fileIter.remove();
                }
            }

            //list audio files
            if(isAudioMode) {
                thumb = parent.loadImage("data" + separator + "img" +
                    separator + "audioThumbNail.png");
                thumb.resize(thumbWidth, thumbHeight);

                fileIter = files.listIterator();
                while(fileIter.hasNext()) {
                    fileName = fileIter.next().getName();
                    fileNameParts = fileName.split("\\.");

                    if(debug) {
                        Quickshow.println(curDir + separator + fileName);
                    }

                    for(i = 0; i < FileExtensions.AUDIO_EXT.length; i++) {
                        if(fileNameParts[fileNameParts.length-1]
                            .equalsIgnoreCase(FileExtensions.AUDIO_EXT[i]))
                        {
                            fileNames.add(fileName);

                            if(j < 20) {
                                thumbs.add(thumb);

                                j++;
                            }

                            else {
                                thumbs.add(null);
                            }
                        }
                    }
                }
            }

            else {
                //list images
                fileIter = files.listIterator();
                while(fileIter.hasNext()) {
                    fileName = fileIter.next().getName();
                    fileNameParts = fileName.split("\\.");
                    fullPath = curDir + separator + fileName;

                    if(debug) {
                        Quickshow.println(fullPath);
                    }

                    //create image thumbnail
                    for(i = 0; i < FileExtensions.IMG_EXT.length; i++) {
                        if(fileNameParts[fileNameParts.length-1]
                            .equalsIgnoreCase(FileExtensions.IMG_EXT[i]))
                        {
                            if(j < 20) {
                                thumb = parent.loadImage(fullPath);

                                thumbDims = newImageDims(thumb);
                                thumb.resize(thumbDims[0], thumbDims[1]);
                                thumbs.add(thumb);

                                if(debug) {
                                    Quickshow.println("" + thumbDims[0] + ' ' +
                                        thumbDims[1]);
                                }

                                j++;
                            }

                            else {
                                thumbs.add(null);
                            }

                            fileNames.add(fileName);

                            fileIter.remove();

                            break;
                        }
                    }
                }

                //list videos
                fileIter = files.listIterator();
                while(fileIter.hasNext()) {
                    fileName = fileIter.next().getName();
                    fileNameParts = fileName.split("\\.");
                    fullPath = curDir + separator + fileName;

                    //queue video for thumbnail generation
                    for(i = 0; i < FileExtensions.VIDEO_EXT.length; i++) {
                        if(fileNameParts[fileNameParts.length-1]
                            .equalsIgnoreCase(FileExtensions.VIDEO_EXT[i]))
                        {
                            qIter.add(new QItem(thumbs.size(), fullPath));

                            thumbs.add(null);

                            if(j < 20) {
                                j++;
                            }

                            fileNames.add(fileName);

                            break;
                        }
                    }
                }
            }

            pageLabel.setCaptionLabel("\n\n\n1\n\nof\n\n" +
                ((int)Math.ceil(fileNames.size()/20.)));

            pathField.setText(curDir);

            if(dblClick) {
                if(System.currentTimeMillis() - clickTime > 500) {
                    dblClick = false;
                }
            }

            if(debug) {
                Quickshow.println("#valid items in directory: " +
                    fileNames.size());
            }
        }

        getNextQItem();
    }

    /**
     * Loads the selected audio files.
     */
    private void loadAudio() {
        File file;
        String fileName;

        for(Integer index : selectedIndex) {
            if(index < fileNames.size()) {
                fileName = fileNames.get(index);
                file = new File(curDir + separator + fileName);

                if(file.isFile()) {
                    results.add(new AudioItem(minim,
                        curDir + separator + fileNames.get(index)));
                }
            }
        }
    }

    /**
     * Loads the selected visual media files.
     */
    private void loadVisual() {
        String[] fileNameParts;
        String fileName;
        short i;
        File file;

        for(Integer index : selectedIndex) {
            if(index < fileNames.size()) {
                fileName = fileNames.get(index);
                file = new File(curDir + separator + fileName);

                if(file.isFile()) {
                    fileNameParts = fileName.split("\\.");

                    //file is image
                    for(i = 0; i < FileExtensions.IMG_EXT.length; i++) {
                        if(fileNameParts[fileNameParts.length-1]
                            .equalsIgnoreCase(FileExtensions.IMG_EXT[i]))
                        {
                            results.add(
                                new ImageItem(
                                    parent,
                                    curDir + separator + fileName,
                                    thumbs.get(index)
                                )
                            );

                            break;
                        }
                    }

                    //file is video
                    if(i == FileExtensions.IMG_EXT.length) {
                        if(debug) {
                            Quickshow
                                .println("Adding video to results arraylist");
                        }

                        results.add(
                            new MovieItem(
                                parent,
                                curDir + separator + fileName,
                                thumbs.get(index)
                            )
                        );
                    }
                }
            }
        }
    }

    /**
     * Loads all applicable media files in the current directory.
     */
    private void loadAll() {
        File file;

        if(isAudioMode) {
            for(String fileName : fileNames) {
                file = new File(curDir + separator + fileName);

                if(file.isFile()) {
                    results.add(new AudioItem(minim, file.getAbsolutePath()));
                }
            }
        }

        else {
            //ensure all thumbnails actually loaded
            for(int j = 0; j < fileNames.size(); j += 20) {
                updateThumbs(j);
            }

            ListIterator<String> fileNameIter = fileNames.listIterator();

            if(fileNameIter.hasNext()) {
                String[] fileNameParts;
                String fileName;
                short i;

                ListIterator<PImage> thumbIter = thumbs.listIterator();
                PImage thumb;

                do {
                    fileName = fileNameIter.next();
                    thumb = thumbIter.next();

                    file = new File(curDir + separator + fileName);

                    if(file.isFile()) {
                        fileNameParts = fileName.split("\\.");

                        //file is image
                        for(i = 0; i < FileExtensions.IMG_EXT.length; i++) {
                            if(fileNameParts[fileNameParts.length-1]
                                .equalsIgnoreCase(FileExtensions.IMG_EXT[i]))
                            {
                                results.add(
                                    new ImageItem(
                                        parent,
                                        curDir + separator + fileName,
                                        thumb
                                    )
                                );

                                break;
                            }
                        }

                        //file is video
                        if(i == FileExtensions.IMG_EXT.length) {
                            results.add(
                                new MovieItem(
                                    parent,
                                    curDir + separator + fileName,
                                    thumb
                                )
                            );
                        }
                    }
                } while(fileNameIter.hasNext());
            }
        }
    }

    /**
     * Returns the current FileBrowser directory.
     * @return the current FileBrowser directory path
     */
    public String getCurDir() {
        return curDir;
    }

    /**
     * Handler for mouse click. Selects a single file if applicable.
     * @param mouseX the x-coordinate of the mouse
     * @param mouseY the y-coordinate of the mouse
     */
    public void mouseClicked(int mouseX, int mouseY) {
        if(mouseX >= 30 && mouseX <= 840  && mouseY >= 70 && mouseY <= 530) {
            selectedIndex.clear();

            short row = (short)((mouseY - 68)/115);
            short col = (short)((mouseX - 61)/162);

            boolean rowSelect = row*115 + 130 - 50 <= mouseY &&
                row*115 + 130 + 50 >= mouseY;
            boolean colSelect = col*162 + 111 - 62 <= mouseX &&
                col*162 + 111 + 62 >= mouseX;

            if(rowSelect && colSelect) {
                int tmp = curDisplayIndex+5*row+col;

                if(tmp < fileNames.size()) {
                    selectedIndex.append(tmp);
                }
            }

            if(!dblClick) {
                dblClick = true;

                clickTime = System.currentTimeMillis();
            }

            else {
                openButton();
            }

            if(debug) {
                Quickshow.println(
                    "mouse clicked: " + mouseX + ',' + mouseY +
                    "\nthumbnail: " + row + ' ' + col +
                    "\nrowSelect: " + rowSelect +
                    "\ncolSelect: " + colSelect +
                    (selectedIndex.size() > 0 ?
                        ", selectedIndex: " + selectedIndex.get(0) : "")
                );
            }
        }
    }

    /**
     * Constrains the mouse coordinates within the item window.
     * @param mouseX the x-coordinate of the mouse
     * @param mouseY the y-coordinate of the mouse
     * @return an integer array containing the adjusted coordinates
     */
    private int[] constrainMouse(int mouseX, int mouseY) {
        int[] result = new int[2];

        if(mouseX < 30) {
            result[0] = 30;
        }

        else if(mouseX > 840) {
            result[0] = 840;
        }

        else {
            result[0] = mouseX;
        }

        if(mouseY < 70) {
            result[1] = 70;
        }

        else if(mouseY > 530) {
            result[1] = 530;
        }

        else {
            result[1] = mouseY;
        }

        return result;
    }

    /**
     * Handler for mouse dragging. Updates the corners of the selection box if
     *   applicable.
     * @param mouseX the x-coordinate of the mouse
     * @param mouseY the y-coordinate of the mouse
     */
    public void mouseDragged(int mouseX, int mouseY) {
        if(isSelecting) {
            int tmp[] = constrainMouse(mouseX, mouseY);

            selectBox[2] = tmp[0];
            selectBox[3] = tmp[1];

            if(debug) {
                Quickshow.println("mouse dragged: " + tmp[0] + ' ' + tmp[1]);
            }
        }
    }

    /**
     * Handler for mouse release. Selects all items within the selection box if
     *   applicable.
     * @param mouseX the x-coordinate of the mouse
     * @param mouseY the y-coordinate of the mouse
     */
    public void mouseReleased(int mouseX, int mouseY) {
        if(isSelecting) {
            int minSelX = (selectBox[0] > selectBox[2] ?
                selectBox[2] : selectBox[0]);
            int maxSelX = (selectBox[0] < selectBox[2] ?
                selectBox[2] : selectBox[0]);
            int minSelY = (selectBox[1] > selectBox[3] ?
                selectBox[3] : selectBox[1]);
            int maxSelY = (selectBox[1] < selectBox[3] ?
                selectBox[3] : selectBox[1]);

            short maxRow = (short)((maxSelY - 68)/115);
            short minRow = (short)((minSelY - 68)/115);
            short maxCol = (short)((maxSelX - 61)/162);
            short minCol = (short)((minSelX - 61)/162);

            boolean rowSelect, colSelect;
            short i, j;
            for(i = minRow; i <= maxRow; i++) {
                rowSelect = true;

                if(i == minRow) {
                    rowSelect = i*115 + 130 > minSelY;
                }

                else if(i == maxRow) {
                    rowSelect = i*115 + 130 < maxSelY;
                }

                for(j = minCol; j <= maxCol; j++) {
                    colSelect = true;

                    if(j == minCol) {
                        colSelect = j*162 + 111 > minSelX;
                    }

                    else if(j == maxCol) {
                        colSelect = j*162 + 111 < maxSelX;
                    }

                    if(rowSelect && colSelect) {
                        selectedIndex.append(curDisplayIndex+i*5+j);
                    }
                }
            }

            if(debug) {
                Quickshow.println(
                    "mouse released: " + mouseX + ' ' + mouseY +
                    "\nminSel: " + minRow + ' ' + minCol +
                    "\nmaxSel: " + maxRow + ' ' + maxCol +
                    "\n#items selected: " + selectedIndex.size()
                );
            }

            isSelecting = false;
        }
    }

    /**
     * Handler for mouse press. Initializes the selection box if applicable.
     * @param mouseX the x-coordinate of the mouse
     * @param mouseY the y-coordinate of the mouse
     */
    public void mousePressed(int mouseX, int mouseY) {
        if(mouseX >= 30 && mouseX <= 840  && mouseY >= 70 && mouseY <= 530) {
            selectedIndex.clear();

            selectBox[0] = selectBox[2] = mouseX;
            selectBox[1] = selectBox[3] = mouseY;

            isSelecting = true;
        }

        if(debug) {
            Quickshow.println("mouse pressed: " + mouseX + ' ' + mouseY);
        }
    }

    /**
     * Toggles display of the FileBrowser.
     * @param visible whether the FileBrowser should be visible
     */
    public void toggle(boolean visible) {
        group.setVisible(visible);
    }

    /**
     * Returns the status of the FileBrowser.
     * @return true if the FileBrowser is visible
     */
    public boolean isEnabled() {
        return pathField.isVisible();
    }

    /**
     * Retrieves the loaded media items. The loaded items are then cleared from
     *   the FileBrowser.
     * @return an ArrayList containing the selected MediaItems
     */
    public ArrayList<MediaItem> getResults() {
        @SuppressWarnings("unchecked")
        ArrayList<MediaItem> tmp = (ArrayList<MediaItem>) results.clone();

        results.clear();

        tmp.trimToSize();

        return tmp;
    }

    /**
     * Checks if MediaItems have been loaded.
     * @return true if MediaItems have been loaded
     */
    public boolean isReady() {
        return !results.isEmpty();
    }

    /**
     * Checks the file type being scanned for.
     * @return true if the FIleBrowser is scanning for audio files
     */
    public boolean isAudioMode() {
        return isAudioMode;
    }

    /**
     * Private class for queueing video files for thumbnail generation.
     * @author Kay Choi
     */
    private class QItem {
        private int index;
        private String name;
        private boolean isHandled = false;

        /**
         * Class constructor.
         * @param i the thumbnail index
         * @param name filename to load
         */
        private QItem(int i, String name) {
            this.index = i;
            this.name = name;
        }
    }
}
