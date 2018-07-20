package fr.dz.chuse.core.check.service;

import java.util.List;

import fr.dz.chuse.core.data.CheckedFile;

/**
 * Check service.
 * Is is responsible for checking all given files for updates.
 */
public interface CheckService {

    /**
     * Checks if at least one of the given file have changed.
     * It can be :
     * - a file that have a different last modified time
     * - a file that doesn't exist anymore
     * - a file that has just been created
     *
     * @param actualFiles
     *            The actual files to be compared to the ones in the repository
     * @return true if there is at least one change
     */
    boolean hasChanged(final List<CheckedFile> actualFiles);

    /**
     * Updates the checked files.
     * Removes all existing checked files and inserts the given ones.
     *
     * @param actualFiles
     *            The actual files to be updated in the repository
     */
    void updateCheckedFiles(final List<CheckedFile> actualFiles);

    /**
     * Extracts checked files from the given resources.
     * It searches for files in src/test/resources and then src/main/resources.
     * Only found resources are returned.
     * 
     * @param filenames
     *            The resources to find
     * @param additionalFolders
     *            Additional resources folders
     * @return The found resources
     */
    List<CheckedFile> extractResources(final List<String> filenames,
            final List<String> additionalFolders);

    /**
     * Extracts checked files from the given class names.
     * It searches for files in src/test/java and then src/main/java.
     * Only found classes are returned.
     * 
     * @param classes
     *            The class names to find
     * @param additionalFolders
     *            Additional resources folders
     * @return The found classes
     */
    List<CheckedFile> extractJavaClasses(final List<String> classes,
            final List<String> additionalFolders);
}
