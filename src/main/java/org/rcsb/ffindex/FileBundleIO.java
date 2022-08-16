package org.rcsb.ffindex;

import org.rcsb.ffindex.impl.AppendableFileBundle;

import java.io.IOException;
import java.nio.file.Path;

/**
 * IO operations on a bunch of files. FFindex-style.
 */
public class FileBundleIO {
    /**
     * Opens a handle to a file bundle. Use a try-with-resource block for this like with other IO operations.
     * The referenced files are not required to exist beforehand. Use them also to create a new FFindex bundle.
     * @param dataPath the location of the data file
     * @param indexPath the location of the corresponding index file
     * @return a {@link FileBundle}, which supports read and limited write operations
     * @throws IOException e.g. upon missing read permissions
     */
    public static FileBundle open(Path dataPath, Path indexPath) throws IOException {
        return new AppendableFileBundle(dataPath, indexPath);
    }
}
