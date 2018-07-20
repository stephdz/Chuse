package fr.dz.chuse.core.repository;

import java.util.List;

import fr.dz.chuse.core.data.CheckedFile;

/**
 * Repository for checked files.
 */
public interface CheckedFileRepository {

    /**
     * Inserts a file in the repository or update it if it already exists.
     *
     * @param file
     *            The file to be inserted
     */
    void insertOrUpdateFile(final CheckedFile file);

    /**
     * Find all checked files from database.
     * It is the database current state.
     *
     * @return All checked files
     */
    List<CheckedFile> findAll();

    /**
     * Clears the repository.
     */
    void deleteAll();
}
