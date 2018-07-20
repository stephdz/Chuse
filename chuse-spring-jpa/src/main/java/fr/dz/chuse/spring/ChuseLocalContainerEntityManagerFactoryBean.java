package fr.dz.chuse.spring;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.persistence.spi.PersistenceUnitInfo;

import org.springframework.lang.Nullable;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.persistenceunit.PersistenceUnitManager;

import fr.dz.chuse.core.check.service.CheckService;
import fr.dz.chuse.core.check.service.support.CheckServiceImpl;
import fr.dz.chuse.core.data.CheckedFile;
import fr.dz.chuse.core.repository.support.JdbcCheckedFileRepository;
import lombok.Setter;

/**
 * LocalContainerEntityManagerFactoryBean using Chuse to check the need of re-building the database.
 */
public class ChuseLocalContainerEntityManagerFactoryBean
        extends LocalContainerEntityManagerFactoryBean {

    // The Chuse service
    private transient CheckService checkService;

    // Additional folders for searching resources
    @Setter
    @Nullable
    private List<String> additionalResourcesFolders;

    // Additional folders for searching java classes
    @Setter
    @Nullable
    private List<String> additionalJavaClassesFolders;

    // Determined using the PersistenceUnitInfo wheter the repository needs to be updated or not
    private boolean updateRepository;

    // The actual files to be updated in the repository (if updateRepository is true)
    private transient List<CheckedFile> actualFiles;

    @Override
    public void afterPropertiesSet() {

        // Initialization of CheckService
        this.checkService = new CheckServiceImpl(
                new JdbcCheckedFileRepository(this.getDataSource()));

        // Check for null parameters
        this.additionalResourcesFolders = this.additionalResourcesFolders == null
                ? Collections.emptyList()
                : this.additionalResourcesFolders;
        this.additionalJavaClassesFolders = this.additionalJavaClassesFolders == null
                ? Collections.emptyList()
                : this.additionalJavaClassesFolders;

        // Do the FactoryBean job (whatever, it doesn't matter)
        super.afterPropertiesSet();

        // Update repository and release a bit of memory
        if (this.updateRepository) {
            this.checkService.updateCheckedFiles(this.actualFiles);
            this.actualFiles = null;
        }
    }

    @Override
    protected PersistenceUnitInfo determinePersistenceUnitInfo(
            final PersistenceUnitManager persistenceUnitManager) {

        /*
         *  Get the PersistenceUnitInfo in order to extract needed informations
         */
        PersistenceUnitInfo pui = super.determinePersistenceUnitInfo(persistenceUnitManager);

        /*
         * List all files to check
         */
        List<CheckedFile> toBeChecked = new ArrayList<>();

        // Mapping files
        toBeChecked.addAll(this.checkService.extractResources(pui.getMappingFileNames(),
                this.additionalResourcesFolders));

        // Initialization scripts
        toBeChecked.addAll(this.checkService.extractResources(this.extractInitScripts(),
                this.additionalResourcesFolders));

        // JPA Classes
        toBeChecked.addAll(this.checkService.extractJavaClasses(pui.getManagedClassNames(),
                this.additionalJavaClassesFolders));

        /*
         *  Check if there are changes
         */
        if (this.checkService.hasChanged(toBeChecked)) {

            // There are some changes
            // Let the user defined hbm2ddl.auto strategy update the database
            // And prepare for update checked file repository (empty it until it has finished)
            this.checkService.updateCheckedFiles(Collections.emptyList());
            this.updateRepository = true;
            this.actualFiles = toBeChecked;
        } else {

            // No changes, skip database build using hbm2ddl.auto=none
            this.getJpaPropertyMap().put("hibernate.hbm2ddl.auto", "none");
        }

        return pui;
    }

    /**
     * Extract initialization scripts from "hibernate.hbm2ddl.import_files" property.
     * 
     * @return
     */
    private List<String> extractInitScripts() {
        String importFiles = (String) this.getJpaPropertyMap()
                .getOrDefault("hibernate.hbm2ddl.import_files", "");
        return Stream.of(importFiles.split(",")).map(String::trim).collect(Collectors.toList());
    }
}
