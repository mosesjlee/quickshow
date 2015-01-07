/**
 * @file MediaItem.java
 * @author Kay Choi
 * @description An abstract wrapper class for all media items.
 */

package quickshow.datatypes;

public abstract class MediaItem {
    private String fileName;

    /**
     * Class constructor.
     * @param fileName the file name of the media file to load
     */
    public MediaItem(String fileName) {
        this.fileName = fileName;
    }

    /**
     * Retrieves the file name of the media item.
     * @return the name of the media file
     */
    public String getFileName() {
        return fileName;
    }

    /**
     * Retrieves the media type of this VIsualItem.
     * @return the item media type
     */
    public String checkType() {
        String result = null;

        short i;

        String[] fileNameParts = fileName.split("\\.");

        for(i = 0; i < FileExtensions.AUDIO_EXT.length; i++) {
            if(fileNameParts[fileNameParts.length-1]
                .equalsIgnoreCase(FileExtensions.AUDIO_EXT[i]))
            {
                result = "audio";
                break;
            }
        }

        if(i == FileExtensions.AUDIO_EXT.length) {
            for(i = 0; i < FileExtensions.IMG_EXT.length; i++) {
                if(fileNameParts[fileNameParts.length-1]
                    .equalsIgnoreCase(FileExtensions.IMG_EXT[i]))
                {
                    result = "image";
                    break;
                }
            }

            if(i == FileExtensions.IMG_EXT.length) {
                result = "video";
            }
        }

        return result;
    }
}
