package fr.dz.chuse.core.check.service.support;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.dz.chuse.core.check.service.CheckService;
import fr.dz.chuse.core.data.CheckedFile;
import fr.dz.chuse.core.repository.CheckedFileRepository;
import lombok.AllArgsConstructor;

/**
 * Check service implementation.
 * Is is responsible for checking all given files for updates.
 */
@AllArgsConstructor
public class CheckServiceImpl implements CheckService {

    private static final Logger LOGGER = LoggerFactory.getLogger(CheckServiceImpl.class);

    private final CheckedFileRepository repository;

    @Override
    public boolean hasChanged(final List<CheckedFile> actualFiles) {

        // Get files from repository, grouped by name
        Map<String, CheckedFile> inRepository = this.repository.findAll().stream()
                .collect(Collectors.toMap(CheckedFile::getName, Function.identity()));

        // Get actual files grouped by name
        Map<String, CheckedFile> actualFilesByName = actualFiles.stream()
                .collect(Collectors.toMap(CheckedFile::getName, Function.identity()));

        /*
         * Identify changes (to log it properly and provide information, else we could have stopped
         * at the first change found)
         */

        // Added files
        List<CheckedFile> addedFiles = actualFiles.stream()
                .filter(f -> !inRepository.containsKey(f.getName())).collect(Collectors.toList());

        // Deleted files
        List<CheckedFile> deletedFiles = inRepository.values().stream()
                .filter(f -> !actualFilesByName.containsKey(f.getName()))
                .collect(Collectors.toList());

        // Modified files
        List<CheckedFile> modifiedFiles = actualFiles.stream()
                .filter(actual -> hasBeenModified(actual, inRepository))
                .collect(Collectors.toList());

        // Check changes
        boolean hasChanged = !addedFiles.isEmpty() || !deletedFiles.isEmpty()
                || !modifiedFiles.isEmpty();

        /*
         * User feedback
         */
        if (hasChanged) {
            LOGGER.info("Modifications have been detected :");
            logChanges(addedFiles, " - ", " has been created");
            logChanges(modifiedFiles, " - ", " has been modified");
            logChanges(deletedFiles, " - ", " has been deleted");
            LOGGER.info("Database will be re-built.");
        } else {
            LOGGER.info("No modification has been detected : Database will not be re-built.");
        }

        return hasChanged;
    }

    @Override
    public void updateCheckedFiles(final List<CheckedFile> actualFiles) {
        this.repository.deleteAll();
        actualFiles.forEach(this.repository::insertOrUpdateFile);
    }

    @Override
    public List<CheckedFile> extractResources(final List<String> filenames,
            final List<String> additionalFolders) {
        List<String> folders = new ArrayList<>(
                Arrays.asList("src/test/resources", "src/main/resources"));
        folders.addAll(additionalFolders);
        return filenames.stream()
                // Find the files
                .map(resource -> extractFile(resource, folders)
                        .orElseGet(() -> handleResourceNotFound(resource)))
                // Filter not found
                .filter(Objects::nonNull)
                // Deduplicate
                .distinct()
                // Convert to CheckedFile
                .map(CheckServiceImpl::toCheckedFile).collect(Collectors.toList());
    }

    @Override
    public List<CheckedFile> extractJavaClasses(final List<String> classes,
            final List<String> additionalFolders) {
        List<String> folders = new ArrayList<>(Arrays.asList("src/test/java", "src/main/java"));
        folders.addAll(additionalFolders);
        return classes.stream()
                // Find the files
                .map(className -> extractFile(classToFileName(className), folders)
                        .orElseGet(() -> handleClassNotFound(className)))
                // Filter not found
                .filter(Objects::nonNull)
                // Deduplicate
                .distinct()
                // Convert to CheckedFile
                .map(CheckServiceImpl::toCheckedFile).collect(Collectors.toList());
    }

    /**
     * Checks if a file has been modified.
     * It has been modified if :
     * - its last modified time has changed
     * - it is not already in the repository (it has just been created)
     *
     * @param actual
     *            The actual file
     * @param inRepository
     *            The repository
     * @return true if the file has changed
     */
    private static boolean hasBeenModified(final CheckedFile actual,
            final Map<String, CheckedFile> inRepository) {
        return inRepository.containsKey(actual.getName()) && !inRepository.get(actual.getName())
                .getLastModifiedTime().equals(actual.getLastModifiedTime());
    }

    private static File handleResourceNotFound(final String resource) {
        LOGGER.warn("Resource {} was not found : it will not be checked for modifications",
                resource);
        return null;
    }

    private static String classToFileName(final String className) {
        return className.replace(".", "/") + ".java";
    }

    private static File handleClassNotFound(final String resource) {
        LOGGER.warn("Class {} was not found : it will not be checked for modifications", resource);
        return null;
    }

    private static Optional<File> extractFile(final String filename, final List<String> folders) {
        return folders.stream().map(folder -> folder + "/" + filename).map(File::new)
                .filter(File::exists).findFirst();
    }

    private static CheckedFile toCheckedFile(final File file) {
        try {
            return new CheckedFile(file.getPath().replace("\\", "/"), LocalDateTime.ofInstant(
                    Files.getLastModifiedTime(file.toPath()).toInstant(), ZoneId.systemDefault()));
        } catch (IOException e) {
            throw new IllegalStateException(
                    "Unable to get last modified time for file " + file.getPath(), e);
        }
    }

    private static void logChanges(final List<CheckedFile> changes, final String prefix,
            final String suffix) {
        for (CheckedFile change : changes) {
            LOGGER.info("{}{}{}", prefix, change.getName(), suffix);
        }
    }
}
