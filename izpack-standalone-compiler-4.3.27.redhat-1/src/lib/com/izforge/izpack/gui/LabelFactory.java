/*
 * IzPack - Copyright 2001-2008 Julien Ponge, All Rights Reserved.
 * 
 * http://izpack.org/
 * http://izpack.codehaus.org/
 * 
 * Copyright 2004 Klaus Bartz
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 *     
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.izforge.izpack.gui;

import java.awt.Font;
import javax.swing.*;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.Desktop;
import java.awt.Cursor;

import java.net.URI;
import java.net.URISyntaxException;
import java.io.IOException;
/**
 * <p>
 * A label factory which can handle modified look like to present icons or present it not.
 * </p>
 *
 * @author Klaus Bartz
 */
public class LabelFactory implements SwingConstants
{

    private static boolean useLabelIcons = true;
    private static float labelFontSizeVal = 1.0f;
    private static Font customLabelFontObj = null;

    /**
     * Returns whether the factory creates labels with icons or without icons.
     *
     * @return whether the factory creates labels with icons or without icons
     */
    public static boolean isUseLabelIcons()
    {
        return useLabelIcons;
    }

    /**
     * Sets the use icon state.
     *
     * @param b flag for the icon state
     */
    public static void setUseLabelIcons(boolean b)
    {
        useLabelIcons = b;
    }

    /**
     * Returns the current label-font-size multiplier.
     *
     * @return the current label-font-size multiplier (or 1.0 if none
     * has been entered).
     */
    public static float getLabelFontSize()
    {
        return labelFontSizeVal;
    }

    /**
     * Sets the label-font-size multiplier.  If the value is not greater
     * than zero or is greater than 5.0 then it will not be used.
     *
     * @param val label-font-size multiplier value to use.
     */
    public static void setLabelFontSize(float val)
    {
        if (val > 0.0f && val <= 5.0f && val != labelFontSizeVal)
        {
            labelFontSizeVal = val;
            final Font fontObj = (new JLabel()).getFont();
            customLabelFontObj =
                              fontObj.deriveFont(fontObj.getSize2D() * val);
        }
    }

    /**
     * Returns a new JLabel with the horizontal alignment CENTER. If isUseLabelIcons is true, the
     * given image will be set to the label, else an empty label returns.
     *
     * @param image the image to be used as label icon
     * @return new JLabel with the given parameters
     */
    public static JLabel create(Icon image)
    {
        return (create(image, CENTER));

    }

    /**
     * Returns a new JLabel with the given horizontal alignment. If isUseLabelIcons is true, the
     * given image will be set to the label, else an empty label returns.
     *
     * @param image               the image to be used as label icon
     * @param horizontalAlignment horizontal alignment of the label
     * @return new JLabel with the given parameters
     */
    public static JLabel create(Icon image, int horizontalAlignment)
    {
        return (create(null, image, horizontalAlignment));

    }

    /**
     * Returns a new JLabel with the horizontal alignment CENTER.
     *
     * @param text the text to be set
     * @return new JLabel with the given parameters
     */
    public static JLabel create(String text)
    {
        return (create(text, CENTER));

    }

    /**
     * Returns a new JLabel or FullLineLabel with the horizontal alignment CENTER.
     *
     * @param text       the text to be set
     * @param isFullLine determines whether a FullLineLabel or a JLabel should be created
     * @return new JLabel or FullLineLabel with the given parameters
     */
    public static JLabel create(String text, boolean isFullLine)
    {
        return (create(text, CENTER, isFullLine));

    }

    /**
     * Returns a new JLabel with the given horizontal alignment.
     *
     * @param text                the text to be set
     * @param horizontalAlignment horizontal alignment of the label
     * @return new JLabel with the given parameters
     */
    public static JLabel create(String text, int horizontalAlignment)
    {
        return (create(text, null, horizontalAlignment));

    }

    /**
     * Returns a new JLabel or FullLineLabel with the given horizontal alignment.
     *
     * @param text                the text to be set
     * @param horizontalAlignment horizontal alignment of the label
     * @param isFullLine          determines whether a FullLineLabel or a JLabel should be created
     * @return new JLabel or FullLineLabel with the given parameters
     */
    public static JLabel create(String text, int horizontalAlignment, boolean isFullLine)
    {
        return (create(text, null, horizontalAlignment, isFullLine));

    }

    /**
     * Returns a new JLabel with the given horizontal alignment. If isUseLabelIcons is true, the
     * given image will be set to the label. The given text will be set allways to the label. It is
     * allowed, that image and/or text are null.
     *
     * @param text                the text to be set
     * @param image               the image to be used as label icon
     * @param horizontalAlignment horizontal alignment of the label
     * @return new JLabel with the given parameters
     */
    public static JLabel create(String text, Icon image, int horizontalAlignment)
    {
        return (create(text, image, horizontalAlignment, false));
    }
    
    /**
     * Returns a new JLabel with a bold, size 2.0f font, aligned to the right
     * @param titleText the text for the title
     * @param isOpaque true if the title should be opaque; false if not
     * @return
     */
    public static JLabel createTitleLabel(String titleText, boolean isOpaque){
        JLabel title = new JLabel(titleText, SwingConstants.RIGHT);
        title.setOpaque(isOpaque);
        Font font = title.getFont();
        font = font.deriveFont(Font.BOLD, font.getSize()*2.0f);
        title.setFont(font);
        return title;
    }

    /**
     * Returns a new JLabel or FullLineLabel with the given horizontal alignment. If isUseLabelIcons
     * is true, the given image will be set to the label. The given text will be set allways to the
     * label. It is allowed, that image and/or text are null.
     *
     * @param text                the text to be set
     * @param image               the image to be used as label icon
     * @param horizontalAlignment horizontal alignment of the label
     * @param isFullLine          determines whether a FullLineLabel or a JLabel should be created
     * @return new JLabel or FullLineLabel with the given parameters
     */
    public static JLabel create(String text, Icon image, int horizontalAlignment, boolean isFullLine)
    {
        JLabel retval = null;
        if (image != null && isUseLabelIcons())
        {
            if (isFullLine)
            {
                retval = new FullLineLabel(image);
            }
            else
            {
                retval = new JLabel(image);
            }
        }
        else
        {
            if (isFullLine)
            {
                retval = new FullLineLabel();
            }
            else
            {
                retval = new JLabel();
            }
        }
        if (text != null)
        {
            retval.setText(text);
        }
        if (customLabelFontObj != null)
        {
            retval.setFont(customLabelFontObj);
        }
        retval.setHorizontalAlignment(horizontalAlignment);
        return (retval);
    }

    /**
     * Create a URL JLabel
     */
    public static JLabel create(final String text, final String URL) {
        JLabel retval = null;
        retval = new SwingLink(text, URL);
        if (customLabelFontObj != null) {
            retval.setFont(customLabelFontObj);
        }
        return retval;
    }
    /**
     * This class is only needed to signal a different layout handling. There is no additonal
     * functionality related to a JLabel. Only the needed constructors are implemented.
     * A FullLineLabel gets from the IzPanelLayout as default a constraints for a full line.
     * Therefore the width of this label do not determine the width of a column as a JLable
     * it do.
     *
     * @author Klaus Bartz
     */
    public static class FullLineLabel extends JLabel
    {

        /**
         * Required (serializable)
         */
        private static final long serialVersionUID = 2918265795390777147L;

        /**
         * Creates a <code>JLabel</code> instance with the specified image.
         * The label is centered vertically and horizontally
         * in its display area.
         *
         * @param image The image to be displayed by the label.
         */
        public FullLineLabel(Icon image)
        {
            super(image);
        }

        /**
         * Default constructor.
         */
        public FullLineLabel()
        {
            super();
        }
    }
    public static class SwingLink extends JLabel
    {
        private static final long serialVersionUID = 8273875024682878518L;

        public SwingLink(String text, Icon image, URI uri){
            super(image);
            setup(text,uri);
        }

        public SwingLink(String text, String uri){
            URI oURI;
            try {
                oURI = new URI(uri);
            } catch (URISyntaxException e) {
                // converts to runtime exception for ease of use
                // if you cannot be sure at compile time that your
                // uri is valid, construct your uri manually and
                // use the other constructor.
                throw new RuntimeException(e);
            }
            setup(text,oURI);
        }

        public void setup(String text, final URI uri){
            setText("<html><span style=\"color: #000099;\">"+text+"</span></html>");
            setToolTipText(uri.toString());
            addMouseListener(new MouseAdapter() {
                    public void mouseClicked(MouseEvent e) {
                        open(uri);
                    }
                    public void mouseEntered(MouseEvent e) {
                        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                    }
                    public void mouseExited(MouseEvent e) {
                        setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                    }
            });
        }

        private static void open(URI uri) {
            if (Desktop.isDesktopSupported()) {
                    Desktop desktop = Desktop.getDesktop();
                    try {
                            desktop.browse(uri);
                    } catch (IOException e) {
                        JOptionPane.showMessageDialog(null,
                                "Failed to launch the link, " +
                                "your computer is likely misconfigured.",
                                "Cannot Launch Link",JOptionPane.WARNING_MESSAGE);
                    }
            } else {
                JOptionPane.showMessageDialog(null,
                        "Java is not able to launch links on your computer.",
                        "Cannot Launch Link",JOptionPane.WARNING_MESSAGE);
            }
        }

    }
    /**
     * Creates a JTextArea that duplicates the look and feel of a label, but wraps words correctly
     * @param labelText text for the label
     * @param isOpaque indicates if this label should be opaque or not
     * @return
     */
    public static JTextArea createMultilineLabel(String labelText, boolean isOpaque){
        JTextArea area = new JTextArea(labelText);
        area.setFont(UIManager.getFont("Label.font"));
        area.setEditable(false);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setOpaque(isOpaque);
        return area;
    }
}
