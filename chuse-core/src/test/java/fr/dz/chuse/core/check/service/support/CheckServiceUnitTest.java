package fr.dz.chuse.core.check.service.support;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.mockito.Mockito;

import fr.dz.chuse.core.data.CheckedFile;
import fr.dz.chuse.core.repository.CheckedFileRepository;

/**
 * Unit tests for the CheckService.
 */
@TestInstance(Lifecycle.PER_CLASS)
public class CheckServiceUnitTest extends AbstractCheckServiceTest {

    @BeforeEach
    public void resetMocks() {
        Mockito.reset(this.repository);
    }

    @Override
    protected CheckedFileRepository createRepository() {
        return Mockito.mock(CheckedFileRepository.class);
    }

    @Override
    protected void insertFilesIntoRepository(final List<CheckedFile> files,
            final CheckedFileRepository repository) {
        Mockito.when(this.repository.findAll()).thenReturn(files);
    }

    @Override
    protected void assertInRepository(final List<CheckedFile> files) {
        for (CheckedFile file : files) {
            Mockito.verify(this.repository, Mockito.times(1)).insertOrUpdateFile(file);
        }
    }

    @Override
    protected void assertNotInRepository(final List<CheckedFile> files) {
        Mockito.verify(this.repository, Mockito.times(1)).deleteAll();
    }

    /**
     * Resource extraction test.
     */
    @Test
    public void testExtractResources() {

        // Arrange : 1 existing file and 1 non existing
        List<String> files = Arrays.asList("existing-file.txt", "non-existing-file.txt");

        // Act : Extract the resources
        List<CheckedFile> resources = this.service.extractResources(files, Collections.emptyList());

        // Assert : The existing file should be extracted
        Assertions.assertEquals(1, resources.size());
        Assertions.assertEquals("src/test/resources/existing-file.txt", resources.get(0).getName());
    }

    /**
     * Java class extraction test.
     */
    @Test
    public void testExtractJavaClasses() {

        // Arrange : 1 existing Java class and 1 non existing
        List<String> classes = Arrays.asList(CheckServiceUnitTest.class.getName(),
                "fr.dz.chuse.core.NonExistingClass");

        // Act : Extract the resources
        List<CheckedFile> resources = this.service.extractJavaClasses(classes,
                Collections.emptyList());

        // Assert : The existing file should be extracted
        Assertions.assertEquals(1, resources.size());
        Assertions.assertEquals(
                "src/test/java/" + CheckServiceUnitTest.class.getName().replace(".", "/") + ".java",
                resources.get(0).getName());
    }
}
