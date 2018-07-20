package fr.dz.chuse.core.config;

import javax.sql.DataSource;

import lombok.Getter;
import lombok.Setter;

/**
 * Chuse Configuration.
 */
@Getter
@Setter
public class Config {

    // The datasource
    private DataSource datasource;
}
