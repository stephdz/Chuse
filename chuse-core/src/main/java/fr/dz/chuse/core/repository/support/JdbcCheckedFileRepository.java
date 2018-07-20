package fr.dz.chuse.core.repository.support;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.apache.commons.dbutils.QueryRunner;

import fr.dz.chuse.core.data.CheckedFile;
import fr.dz.chuse.core.repository.CheckedFileRepository;
import fr.dz.chuse.core.repository.RepositoryConstants;
import fr.dz.chuse.core.utils.DateUtils;

/**
 * Repository for checked files using JDBC.
 */
public class JdbcCheckedFileRepository implements CheckedFileRepository {

    // @formatter:off
    private static final String CREATE_TABLE_QUERY = MessageFormat.format(
            "CREATE TABLE IF NOT EXISTS {0} ( "
                    + "{1} VARCHAR(4096) NOT NULL, "
                    + "{2} TIMESTAMP NOT NULL, "
                    + "PRIMARY KEY({1}) )",
            RepositoryConstants.CHECKED_FILES_TABLE_NAME,
            RepositoryConstants.CHECKED_FILES_NAME_FIELD,
            RepositoryConstants.CHECKED_FILES_LAST_MODIFIED_TIME_FIELD);
    // @formatter:on

    // @formatter:off
    private static final String UPDATE_QUERY = MessageFormat.format(
            "UPDATE {0} SET {2}=? WHERE {1}=?",
            RepositoryConstants.CHECKED_FILES_TABLE_NAME,
            RepositoryConstants.CHECKED_FILES_NAME_FIELD,
            RepositoryConstants.CHECKED_FILES_LAST_MODIFIED_TIME_FIELD);
    // @formatter:on

    // @formatter:off
    private static final String INSERT_QUERY = MessageFormat.format(
            "INSERT INTO {0}({1},{2}) VALUES (?,?)",
            RepositoryConstants.CHECKED_FILES_TABLE_NAME,
            RepositoryConstants.CHECKED_FILES_NAME_FIELD,
            RepositoryConstants.CHECKED_FILES_LAST_MODIFIED_TIME_FIELD);
    // @formatter:on

    // @formatter:off
    private static final String SELECT_QUERY = MessageFormat.format(
            "SELECT {1}, {2} FROM {0} ORDER BY {1}",
            RepositoryConstants.CHECKED_FILES_TABLE_NAME,
            RepositoryConstants.CHECKED_FILES_NAME_FIELD,
            RepositoryConstants.CHECKED_FILES_LAST_MODIFIED_TIME_FIELD);
    // @formatter:on

    // @formatter:off
    private static final String DELETE_ALL_QUERY = MessageFormat.format(
            "TRUNCATE TABLE {0}",
            RepositoryConstants.CHECKED_FILES_TABLE_NAME);
    // @formatter:on

    private final QueryRunner queryRunner;

    /**
     * Constructor.
     *
     * @param datasource
     *            The datasource
     */
    public JdbcCheckedFileRepository(final DataSource datasource) {
        this.queryRunner = new QueryRunner(datasource);
    }

    @Override
    public void insertOrUpdateFile(final CheckedFile file) {
        this.createTableIfNecessary();

        // First, try to update the potentially existing line
        Timestamp timestampLastModifiedTime = DateUtils.toSqlTimestamp(file.getLastModifiedTime());
        int modified;
        try {
            modified = this.queryRunner.update(UPDATE_QUERY, timestampLastModifiedTime,
                    file.getName());
        } catch (SQLException e) {
            throw new IllegalStateException("Unable to update a checked file", e);
        }

        // No modified line : we create it
        if (modified == 0) {
            try {
                this.queryRunner.update(INSERT_QUERY, file.getName(), timestampLastModifiedTime);
            } catch (SQLException e) {
                throw new IllegalStateException("Unable to insert a checked file", e);
            }
        }
    }

    @Override
    public List<CheckedFile> findAll() {
        this.createTableIfNecessary();
        try {
            return this.queryRunner.query(SELECT_QUERY,
                    JdbcCheckedFileRepository::mapToCheckedFile);
        } catch (SQLException e) {
            throw new IllegalStateException("Unable to find checked files", e);
        }
    }

    @Override
    public void deleteAll() {
        this.createTableIfNecessary();
        try {
            this.queryRunner.update(DELETE_ALL_QUERY);
        } catch (SQLException e) {
            throw new IllegalStateException("Unable to delete all checked files table", e);
        }
    }

    /**
     * Creates the CHECKED_FILES table if it doesn't exist.
     */
    protected void createTableIfNecessary() {
        try {
            this.queryRunner.update(CREATE_TABLE_QUERY);
        } catch (SQLException e) {
            throw new IllegalStateException("Unable to create checked files table", e);
        }
    }

    /**
     * Maps a ResultSet to a CheckedFile.
     *
     * @param rs
     *            The ResultSet
     * @return The CheckedFile
     * @throws SQLException
     *             SQL Error
     */
    private static List<CheckedFile> mapToCheckedFile(final ResultSet rs) throws SQLException {
        List<CheckedFile> result = new ArrayList<>();
        while (rs.next()) {
            CheckedFile file = new CheckedFile();
            file.setName(rs.getString(RepositoryConstants.CHECKED_FILES_NAME_FIELD));
            file.setLastModifiedTime(DateUtils.toLocalDateTime(
                    rs.getTimestamp(RepositoryConstants.CHECKED_FILES_LAST_MODIFIED_TIME_FIELD)));
            result.add(file);
        }
        return result;
    }
}
