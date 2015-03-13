package org.apache.ignite.internal.processors.igfs;

import java.io.*;
import java.util.*;

/**
 * Universal interface to an underlying filesystem.
 * Typically used for secondary filesystem.
 * To be used solely in tests.
 */
public interface UniversalFileSystemAdapter {

    /**
     * Gets name of the FS.
     * @return name of this file system.
     */
    String name();

    /**
     * Answers if a file denoted by path exists.
     * @param path path of the file to check.
     * @return if the file exists.
     * @throws IOException in case of failure.
     */
    boolean exists(String path) throws IOException;

    /**
     * Deletes a file or directory.
     * @param path the path to delete.
     * @param recursive instructs to delete a directory recursively.
     * @return true on success, false otherwise.
     * @throws IOException On failure.
     */
    boolean delete(String path, boolean recursive) throws IOException;

    /**
     * Makes directories, creating missing parent directories as needed.
     * @param path the directory to create.
     * @throws IOException On failure.
     */
    void mkdirs(String path) throws IOException;

    /**
     * Clears (formats) entire the filesystem.
     * All the data in the filesystem are DESTROYED.
     * @throws IOException
     */
    void format() throws IOException;

    /**
     * Gets properties (such as owner, group, and permissions) of a file.
     * @param path the path to the file to get properties of.
     * @return the properties.
     */
    Map<String,String> properties(String path) throws IOException;

    /**
     * Opens input stream to read file contents.
     * @param path the path to the file.
     */
    InputStream openInputStream(String path) throws IOException;

    /**
     * Opens output stream to write file contents.
     * @param path the path to the file to be written.
     * @param append if to append to the end of existing data.
     * @return the OutputStream to write into.
     * @throws IOException On failure.
     */
    OutputStream openOutputStream(String path, boolean append) throws IOException;

    /**
     * Gets an entity of the given type (class) associated with this universal adapter.
     * @param clazz The class representing the type we wish to adapt to.
     * @param <T> The type we need to adapt to.
     * @return the adapter object of the given type.
     */
    <T> T getAdapter(Class<T> clazz);
}
