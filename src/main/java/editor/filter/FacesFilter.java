package editor.filter;

import java.awt.Image;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.ImageIcon;

public enum FacesFilter
{
    ANY,
    ALL,
    FRONT,
    BACK;
    
    private final ImageIcon icon;

    private FacesFilter()
    {
        ImageIcon img;
        try
        {
            img = new ImageIcon(ImageIO.read(getClass().getResourceAsStream("/images/faces/" + toString() + ".png")));
        }
        catch (IOException e)
        {
            img = new ImageIcon();
            e.printStackTrace();
        }
        icon = img;
    }

    public Icon getIcon()
    {
        return icon;
    }

    public Icon getIcon(int width)
    {
        return new ImageIcon(icon.getImage().getScaledInstance(width, -1, Image.SCALE_SMOOTH));
    }
}
