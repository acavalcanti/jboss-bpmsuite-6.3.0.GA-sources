package com.izforge.izpack.gui;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;

/**
 * Created by thauser on 5/7/15.
 */
public class ScrollPaneFactory {

    public static JScrollPane createScroller() {
        return createPanelScroller(BorderFactory.createEmptyBorder(),
                null,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED,
                true);
    }

    public static JScrollPane createScroller(boolean isOpaque) {
        return createPanelScroller(BorderFactory.createEmptyBorder(),
                null,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED,
                isOpaque);
    }

    public static JScrollPane createScroller(Component view) {
        return createPanelScroller(BorderFactory.createEmptyBorder(),
                view,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED,
                true);
    }

    public static JScrollPane createScroller(Component view, boolean isOpaque) {
        return createPanelScroller(BorderFactory.createEmptyBorder(),
                view,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED,
                isOpaque);
    }

    public static JScrollPane createScroller(Border border, Component view, boolean isOpaque) {
        return createPanelScroller(border,
                view,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED,
                isOpaque);
    }

    public static JScrollPane createNoHorizontalPanelScroller(Component view) {
        return createPanelScroller(BorderFactory.createEmptyBorder(),
                view,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER,
                true);
    }

    public static JScrollPane createNoVerticalPanelScroller(Component view) {
        return createPanelScroller(BorderFactory.createEmptyBorder(),
                view,
                JScrollPane.VERTICAL_SCROLLBAR_NEVER,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED,
                true);

    }

    public static JScrollPane createAlwaysHorizontalPanelScroller(Component view) {
        return createPanelScroller(BorderFactory.createEmptyBorder(),
                view,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS,
                true);

    }

    public static JScrollPane createAlwaysVerticalPanelScroller(Component view) {
        return createPanelScroller(BorderFactory.createEmptyBorder(),
                view,
                JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED,
                true);
    }

    public static JScrollPane createAlwaysVerticalAndHorizontalPanelScroller(Component view) {
        return createPanelScroller(BorderFactory.createEmptyBorder(),
                view,
                JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS,
                true);
    }


    public static JScrollPane createNoHorizontalPanelScroller(Component view, boolean isOpaque) {
        return createPanelScroller(BorderFactory.createEmptyBorder(),
                view,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER,
                isOpaque);
    }

    public static JScrollPane createNoVerticalPanelScroller(Component view, boolean isOpaque) {
        return createPanelScroller(BorderFactory.createEmptyBorder(),
                view,
                JScrollPane.VERTICAL_SCROLLBAR_NEVER,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED,
                isOpaque);

    }

    public static JScrollPane createAlwaysHorizontalPanelScroller(Component view, boolean isOpaque) {
        return createPanelScroller(BorderFactory.createEmptyBorder(),
                view,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS,
                isOpaque);

    }

    public static JScrollPane createAlwaysVerticalPanelScroller(Component view, boolean isOpaque) {
        return createPanelScroller(BorderFactory.createEmptyBorder(),
                view,
                JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED,
                isOpaque);
    }

    public static JScrollPane createAlwaysVerticalAndHorizontalPanelScroller(Component view, boolean isOpaque) {
        return createPanelScroller(BorderFactory.createEmptyBorder(),
                view,
                JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS,
                isOpaque);
    }

    public static JScrollPane createNoHorizontalPanelScroller(Border border, Component view, boolean isOpaque) {
        return createPanelScroller(border,
                view,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER,
                isOpaque);
    }

    public static JScrollPane createNoVerticalPanelScroller(Border border, Component view, boolean isOpaque) {
        return createPanelScroller(border,
                view,
                JScrollPane.VERTICAL_SCROLLBAR_NEVER,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED,
                isOpaque);

    }

    public static JScrollPane createAlwaysHorizontalPanelScroller(Border border, Component view, boolean isOpaque) {
        return createPanelScroller(border,
                view,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS,
                isOpaque);

    }

    public static JScrollPane createAlwaysVerticalPanelScroller(Border border, Component view, boolean isOpaque) {
        return createPanelScroller(border,
                view,
                JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED,
                isOpaque);
    }

    public static JScrollPane createAlwaysVerticalAndHorizontalPanelScroller(Border border, Component view, boolean isOpaque) {
        return createPanelScroller(border,
                view,
                JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS,
                isOpaque);
    }

    private static JScrollPane createPanelScroller(Border border, Component view, int vertical, int horizontal, boolean isOpaque) {
        JScrollPane scroller = new JScrollPane(view, vertical, horizontal);
        //Disable until UX improvements are go
        //scroller.setViewportBorder(border);
        /*scroller.getVerticalScrollBar().setBorder(border);
        scroller.getHorizontalScrollBar().setBorder(border);*/
        scroller.getViewport().setOpaque(isOpaque);
        scroller.setOpaque(isOpaque);
        //scroller.setBorder(border);
        scroller.getVerticalScrollBar().setUnitIncrement(15);
        return scroller;
    }
}
