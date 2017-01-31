package frontend.observers;

/**
 * Interface for the Observer of the StartUpController. It extends the MenuBarObserver as the StartUp scene has a menu
 * bar.
 */
public interface StartUpObserver extends MenuBarObserver {//menu options included

    /**
     * Called when the load files button is pressed.
     */
    void loadFiles();
}
