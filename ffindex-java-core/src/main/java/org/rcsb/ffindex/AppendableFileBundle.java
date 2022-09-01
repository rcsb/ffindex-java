package org.rcsb.ffindex;

/**
 * A file bundle that support both reading and writing. Content will be appended to the end of the data file.
 */
public interface AppendableFileBundle extends ReadableFileBundle, WritableFileBundle {

}
