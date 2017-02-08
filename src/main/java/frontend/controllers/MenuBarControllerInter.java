package frontend.controllers;

/**
 * Implemented for Observers of a Controller that has a menu bar.
 */
public interface MenuBarControllerInter {
    /**
     * Called when the close menu item is selected.
     */
    void close();

    /**
     * Called when the about menu item is selected.
     */
    void about();

    /**
     * Called when the timeline menu item is selected.
     */
    void timeline();

    /**
     * Called when the preferences menu item is selected.
     */
    void preferences();
}
