/**
 * @file MovieItem.java
 * @author Kay Choi
 * @description A wrapper class for video media items.
 */

package quickshow.datatypes;

import processing.video.Movie;

public class MovieItem extends VisualItem {
    Movie movie;

    /**
     * Class constructor.
     * @param parent the Quickshow object
     * @param fileName the file name of the video file to load
     * @param thumb the MediaItem thumbnail
     */
    public MovieItem(quickshow.Quickshow parent, String fileName,
        processing.core.PImage thumb)
    {
        super(fileName, thumb);

        movie = new Movie(parent, fileName);

        movie.play();
        displayTime = (int)Math.ceil(movie.duration());
        movie.stop();
    }

    /**
     * Retrieves the video.
     * @return a Movie object
     */
    public Movie getMovie() {
        return movie;
    }
}
