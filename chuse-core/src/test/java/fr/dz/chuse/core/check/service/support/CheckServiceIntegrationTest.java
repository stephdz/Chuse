package fr.dz.chuse.core.check.service.support;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Assertions;

import fr.dz.chuse.core.data.CheckedFile;
import fr.dz.chuse.core.repository.CheckedFileRepository;
import fr.dz.chuse.core.repository.support.JdbcCheckedFileRepository;
import fr.dz.chuse.core.utils.DataSourceUtils;

/**
 * Tests for the CheckService.
 */
public class CheckServiceIntegrationTest extends AbstractCheckServiceTest {

    @Override
    protected CheckedFileRepository createRepository() {
        return new JdbcCheckedFileRepository(DataSourceUtils.createInMemoryDatabase());
    }

    @Override
    protected void insertFilesIntoRepository(final List<CheckedFile> files,
            final CheckedFileRepository repository) {
        files.forEach(repository::insertOrUpdateFile);
    }

    @Override
    protected void assertInRepository(final List<CheckedFile> files) {
        Set<CheckedFile> repositoryContent = new HashSet<>(this.repository.findAll());
        for (CheckedFile file : files) {
            Assertions.assertTrue(repositoryContent.contains(file),
                    file.getName() + " is missing in the repository");
        }
    }

    @Override
    protected void assertNotInRepository(final List<CheckedFile> files) {
        Set<CheckedFile> repositoryContent = new HashSet<>(this.repository.findAll());
        for (CheckedFile file : files) {
            Assertions.assertFalse(repositoryContent.contains(file),
                    file.getName() + " should not be in the repository");
        }
    }
}
