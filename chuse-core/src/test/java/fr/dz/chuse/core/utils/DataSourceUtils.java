package fr.dz.chuse.core.utils;

import javax.sql.DataSource;

import org.apache.commons.dbcp2.BasicDataSource;

/**
 * Utils for tests with datasource.
 */
public final class DataSourceUtils {

    /**
     * In memory database initialization.
     *
     * @return The DataSource
     */
    public static DataSource createInMemoryDatabase() {
        BasicDataSource dataSource = new BasicDataSource();
        dataSource.setDriverClassName("org.h2.Driver");
        dataSource.setUrl("jdbc:h2:mem:");
        dataSource.setUsername("sa");
        dataSource.setPassword("");
        dataSource.setMaxTotal(10);
        dataSource.setMaxIdle(5);
        dataSource.setInitialSize(5);
        dataSource.setValidationQuery("SELECT 1");
        return dataSource;
    }

    private DataSourceUtils() {
    }
}
