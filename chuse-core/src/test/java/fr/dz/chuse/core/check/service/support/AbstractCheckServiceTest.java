package fr.dz.chuse.core.check.service.support;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import fr.dz.chuse.core.check.service.CheckService;
import fr.dz.chuse.core.data.CheckedFile;
import fr.dz.chuse.core.repository.CheckedFileRepository;

/**
 * Tests for the CheckService.
 */
public abstract class AbstractCheckServiceTest {

    protected CheckedFileRepository repository;

    protected CheckService service;

    /**
     * Creates the repository used for the tests.
     *
     * @return The repository
     */
    protected abstract CheckedFileRepository createRepository();

    /**
     * Inserts files into the repository.
     *
     * @param files
     *            Files to insert
     * @param repository
     *            The repository
     */
    protected abstract void insertFilesIntoRepository(final List<CheckedFile> files,
            final CheckedFileRepository repository);

    /**
     * Assert that files are in repository.
     * 
     * @param files
     *            Files to check
     */
    protected abstract void assertInRepository(final List<CheckedFile> files);

    /**
     * Assert that files are not in repository.
     * 
     * @param files
     *            Files to check
     */
    protected abstract void assertNotInRepository(final List<CheckedFile> files);

    @BeforeEach
    public void init() {
        this.repository = this.createRepository();
        this.service = new CheckServiceImpl(this.repository);
    }

    /**
     * Change detection test without change.
     */
    @Test
    public void testHasChanged_noChange() {

        // Arrange : No change between repository and actual
        List<CheckedFile> unchanged = createCheckedFiles();
        this.insertFilesIntoRepository(unchanged, this.repository);

        // Act : Check changes
        boolean changed = this.service.hasChanged(unchanged);

        // Assert : No change should be notified
        Assertions.assertFalse(changed, "A change has been detected whereas there was not");
    }

    /**
     * Change detection test with a modified file.
     */
    @Test
    public void testHasChanged_modifiedFile() {

        // Arrange : A file has been modified
        List<CheckedFile> inRepository = createCheckedFiles();
        this.insertFilesIntoRepository(inRepository, this.repository);
        List<CheckedFile> actual = createCheckedFiles();
        actual.get(2).setLastModifiedTime(LocalDateTime.parse("2018-07-18T19:26:32.000"));

        // Act : Check changes
        boolean changed = this.service.hasChanged(actual);

        // Assert : A change should be notified
        Assertions.assertTrue(changed,
                "No change has been detected whereas a file has been modified");
    }

    /**
     * Change detection with a created file.
     */
    @Test
    public void testHasChanged_createdFile() {

        // Arrange : A file has been created
        List<CheckedFile> inRepository = createCheckedFiles();
        this.insertFilesIntoRepository(inRepository, this.repository);
        List<CheckedFile> actual = new ArrayList<>(createCheckedFiles());
        actual.add(new CheckedFile("MyClass.java", LocalDateTime.parse("2018-07-18T19:26:32.000")));

        // Act : Check changes
        boolean changed = this.service.hasChanged(actual);

        // Assert : A change should be notified
        Assertions.assertTrue(changed,
                "No change has been detected whereas a file has been created");
    }

    /**
     * Change detection test with a deleted file.
     */
    @Test
    public void testHasChanged_deletedFile() {

        // Arrange : A file has been deleted
        List<CheckedFile> inRepository = createCheckedFiles();
        this.insertFilesIntoRepository(inRepository, this.repository);
        List<CheckedFile> actual = new ArrayList<>(createCheckedFiles());
        actual.remove(2);

        // Act : Check changes
        boolean changed = this.service.hasChanged(actual);

        // Assert : A change should be notified
        Assertions.assertTrue(changed,
                "No change has been detected whereas a file has been deleted");
    }

    /**
     * Update repository test.
     */
    @Test
    public void testUpdateCheckedFiles() {

        // Arrange : A file in the repository and new files to update
        List<CheckedFile> inRepository = Arrays
                .asList(new CheckedFile("MyFile.txt", LocalDateTime.now()));
        this.insertFilesIntoRepository(inRepository, this.repository);
        List<CheckedFile> toBeUpdated = createCheckedFiles();

        // Act : Update the repository
        this.service.updateCheckedFiles(toBeUpdated);

        // Assert : Old file should not be in repository and new files should be in
        this.assertNotInRepository(inRepository);
        this.assertInRepository(toBeUpdated);
    }

    /**
     * Create a set of checked files for tests.
     *
     * @return The checked files
     */
    private static List<CheckedFile> createCheckedFiles() {
        return Arrays.asList(
                new CheckedFile("Class1.java", LocalDateTime.parse("2017-07-01T19:26:32.000")),
                new CheckedFile("Class2.java", LocalDateTime.parse("2017-07-02T19:26:32.000")),
                new CheckedFile("Class3.java", LocalDateTime.parse("2017-07-03T19:26:32.000")),
                new CheckedFile("Class4.java", LocalDateTime.parse("2017-07-04T19:26:32.000")),
                new CheckedFile("Class5.java", LocalDateTime.parse("2017-07-05T19:26:32.000")),
                new CheckedFile("Class6.java", LocalDateTime.parse("2017-07-06T19:26:32.000")));
    }
}
