package fr.dz.chuse.core.data;

import java.io.Serializable;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * A checked file, stored in database to save the last workbench state.
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class CheckedFile implements Serializable {

    private static final long serialVersionUID = 1L;

    private String name;

    private LocalDateTime lastModifiedTime;
}
