package frontend.observers;

/**
 * Implemented by Observers of Controllers that contain a Menu bar, to handle the relevant events.
 */
public interface MenuBarObserver {
    /**
     * Called when the about menu item is pressed.
     */
    void showAbout();

    /**
     * Called when the close menu item is pressed.
     */
    void close();

    /**
     * Called when the timeline menu item is pressed.
     */
    void timeline();

    /**
     * Called when the preferences menu item is pressed.
     */
    void preferences();
}
