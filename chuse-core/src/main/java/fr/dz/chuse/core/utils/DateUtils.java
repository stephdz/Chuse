package fr.dz.chuse.core.utils;

import java.sql.Timestamp;
import java.time.LocalDateTime;

/**
 * Utils for date manipulations.
 */
public final class DateUtils {

    /**
     * Converts a SQL Timestamp to a LocalDateTime.
     *
     * @param timestamp
     *     The SQL Timestamp
     * @return The LocalDateTime
     */
    public static LocalDateTime toLocalDateTime(final Timestamp timestamp) {
        return timestamp.toLocalDateTime();
    }

    /**
     * Converts a LocalDateTime to a SQL Timestamp.
     *
     * @param localDateTime
     *     The LocalDateTime
     * @return The SQL Timestamp
     */
    public static Timestamp toSqlTimestamp(final LocalDateTime localDateTime) {
        return Timestamp.valueOf(localDateTime);
    }

    private DateUtils() {
    }
}
