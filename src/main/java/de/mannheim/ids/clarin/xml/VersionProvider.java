package de.mannheim.ids.clarin.xml;

import java.io.IOException;
import java.util.Objects;
import java.util.Properties;

import picocli.CommandLine.IVersionProvider;

/**
 * a version provider for PicoCLI
 * <p>
 * reads version from a file in the project,
 * which can be filled during the build
 */
public class VersionProvider implements IVersionProvider {

        /**
         * @return version number defined in
         * {@code src/main/resources/properties/project.properties}
         * @throws IOException when file broken/unavailable
         */
        @Override public String[] getVersion() throws IOException {
                final Properties properties = new Properties();
                properties.load(Objects.requireNonNull(
                        this.getClass().getClassLoader()
                                .getResourceAsStream("project.properties")));
                String version = properties.getProperty("version");
                return new String[] { version };
        }

}
