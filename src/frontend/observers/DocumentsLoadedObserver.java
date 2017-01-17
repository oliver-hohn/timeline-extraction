package frontend.observers;

import backend.process.FileData;

/**
 * Created by Oliver on 17/01/2017.
 */
public interface DocumentsLoadedObserver {
    void remove(FileData fileData);
}
