package fr.dz.chuse.core.repository.support;

import java.time.LocalDateTime;
import java.util.List;

import javax.sql.DataSource;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import fr.dz.chuse.core.data.CheckedFile;
import fr.dz.chuse.core.utils.DataSourceUtils;

/**
 * Tests for the JdbcCheckedFileRepository.
 */
public class JdbcCheckedFileRepositoryTest {

    private DataSource datasource;

    private JdbcCheckedFileRepository repository;

    @BeforeEach
    public void init() {
        this.datasource = DataSourceUtils.createInMemoryDatabase();
        this.repository = new JdbcCheckedFileRepository(this.datasource);
    }

    /**
     * Insert or update test with not existing file.
     */
    @Test
    public void testInsertOrUpdateFile_notExisting() {

        // Arrange : A file that doesn't exist in database
        CheckedFile file = new CheckedFile("MyClass.java", LocalDateTime.now());

        // Act : Insert the file
        this.repository.insertOrUpdateFile(file);

        // Assert : The file should now exist
        List<CheckedFile> files = this.repository.findAll();
        Assertions.assertFalse(files.isEmpty(), "No file found whereas there should be one");
        Assertions.assertEquals(1, files.size(), "There should be only the inserted file");
        Assertions.assertEquals(file.getName(), files.get(0).getName(),
                "The found file name is not what it is expected");
        Assertions.assertEquals(file.getLastModifiedTime(), files.get(0).getLastModifiedTime(),
                "The found file last modified time is not what it is expected");
    }

    /**
     * Insert or update test with an existing file.
     */
    @Test
    public void testInsertOrUpdateFile_existing() {

        // Arrange : A file that already exist in database
        CheckedFile existingFile = new CheckedFile("MyClass.java", LocalDateTime.now());
        this.repository.insertOrUpdateFile(existingFile);
        CheckedFile updatedFile = new CheckedFile("MyClass.java",
                LocalDateTime.now().plusMinutes(10));

        // Act : Insert the file
        this.repository.insertOrUpdateFile(updatedFile);

        // Assert : The file should now exist
        List<CheckedFile> files = this.repository.findAll();
        Assertions.assertFalse(files.isEmpty(), "No file found whereas there should be one");
        Assertions.assertEquals(1, files.size(), "There should be only the inserted file");
        Assertions.assertEquals(updatedFile.getName(), files.get(0).getName(),
                "The found file name is not what it is expected");
        Assertions.assertEquals(updatedFile.getLastModifiedTime(),
                files.get(0).getLastModifiedTime(),
                "The found file last modified time is not what it is expected");
    }

    /**
     * Find all test without the table and without file.
     */
    @Test
    public void testFindAll_withoutTable() {

        // Arrange : No table and no file in database

        // Act : Find all files
        List<CheckedFile> files = this.repository.findAll();

        // Assert : No file found
        Assertions.assertTrue(files.isEmpty(), "File(s) found whereas there are none");
    }

    /**
     * Find all test with the table and without file.
     */
    @Test
    public void testFindAll_withTableAndNoFile() {

        // Arrange : Table created and no file in database
        this.repository.createTableIfNecessary();

        // Act : Find all files
        List<CheckedFile> files = this.repository.findAll();

        // Assert : No file found
        Assertions.assertTrue(files.isEmpty(), "File(s) found whereas there are none");
    }

    /**
     * Find all test with files.
     */
    @Test
    public void testFindAll_withFile() {

        // Arrange : One file in database
        CheckedFile file = new CheckedFile("MyClass.java", LocalDateTime.now());
        this.repository.insertOrUpdateFile(file);

        // Act : Find all files
        List<CheckedFile> files = this.repository.findAll();

        // Assert : A file found
        Assertions.assertFalse(files.isEmpty(), "No file found whereas there are some");
        Assertions.assertEquals(1, files.size(), "There should be only one file");
        Assertions.assertEquals(file.getName(), files.get(0).getName(),
                "The found file name is not what it is expected");
        Assertions.assertEquals(file.getLastModifiedTime(), files.get(0).getLastModifiedTime(),
                "The found file last modified time is not what it is expected");
    }

    /**
     * Delete all test without table.
     */
    @Test
    public void testDeleteAll_withoutTable() {

        // Arrange : One file in database
        CheckedFile file = new CheckedFile("MyClass.java", LocalDateTime.now());
        this.repository.insertOrUpdateFile(file);

        // Act : Delete all files
        this.repository.deleteAll();

        // Assert : No file left in the repository
        Assertions.assertTrue(this.repository.findAll().isEmpty(),
                "File(s) found whereas they have been deleted");
    }
}
