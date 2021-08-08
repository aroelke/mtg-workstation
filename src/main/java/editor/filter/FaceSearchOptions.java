package editor.filter;

import java.awt.Image;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.ImageIcon;

/**
 * This enumerats all the ways that a user can select to filter faces when filtering cards: any face, all faces,
 * front face only, or back face only.  When a card has more than two faces (like Who/What/When/Where/Why), it isn't
 * possible to only filter using middle faces.  This doesn't affect cards that have only one face.
 */
public enum FaceSearchOptions
{
    /** Any face of a card can match the filter. */
    ANY,
    /** All faces of a card must match the filter. */
    ALL,
    /** Front (or only) face of a card must match the filter. */
    FRONT,
    /** Back (or only) face of a card must match the filter. */
    BACK;
    
    /** Icon to display on a filter line to indicate which face to search for that line. */
    private final ImageIcon icon;

    private FaceSearchOptions()
    {
        ImageIcon img;
        try
        {
            img = new ImageIcon(ImageIO.read(getClass().getResourceAsStream("/images/faces/" + toString().toLowerCase() + ".png")));
        }
        catch (IOException e)
        {
            img = new ImageIcon();
            e.printStackTrace();
        }
        icon = img;
    }

    /**
     * @return The icon corresponding to this FacesFilter at its original size.
     */
    public Icon getIcon()
    {
        return icon;
    }

    /**
     * Scale this FacesFilter's icon.
     * 
     * @param width new width of the icon
     * @return The icon scaled so that its width is the specified size.
     */
    public Icon getIcon(int width)
    {
        return new ImageIcon(icon.getImage().getScaledInstance(width, -1, Image.SCALE_SMOOTH));
    }
}
