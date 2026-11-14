package io.github.hglabplh_tech.reflect.java.api.utils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public final class LinterConfigLoader {

    public static final String ENV_CONFIG_PATH = "LINTER_CONFIG_PATH";
    public static final String SYS_PROP_CONFIG_PATH = "linter.config.path";
    public static final String DEFAULT_CLASSPATH_RESOURCE = "linter.properties";

    private LinterConfigLoader() {
        // Utility class
    }

    /**
     * Loads configuration using priority order:
     * 1. JVM System Property (-Dlinter.config.path)
     * 2. Environment Variable (LINTER_CONFIG_PATH)
     * 3. Classpath Resource (linter.properties)
     * 4. Built-in hardcoded defaults
     */
    public static LinterConfig load() {
        String externalPath = resolveExternalPath();
        if (externalPath != null && !externalPath.isBlank()) {
            return loadFromFile(Path.of(externalPath));
        }
        return loadFromClasspath(DEFAULT_CLASSPATH_RESOURCE);
    }

    public static LinterConfig loadFromFile(Path filePath) {
        if (!Files.exists(filePath) || !Files.isRegularFile(filePath)) {
            throw new IllegalArgumentException("External linter properties file not found: " + filePath);
        }
        try (InputStream in = Files.newInputStream(filePath)) {
            return parseProperties(in, filePath.toString());
        } catch (IOException e) {
            throw new RuntimeException("Failed to read external linter config from: " + filePath, e);
        }
    }

    public static LinterConfig loadFromClasspath(String resourceName) {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        if (cl == null) {
            cl = LinterConfigLoader.class.getClassLoader();
        }

        try (InputStream in = cl.getResourceAsStream(resourceName)) {
            if (in == null) {
                return LinterConfig.defaults();
            }
            return parseProperties(in, "classpath:" + resourceName);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read classpath linter config: " + resourceName, e);
        }
    }

    private static String resolveExternalPath() {
        String sysProp = System.getProperty(SYS_PROP_CONFIG_PATH);
        if (sysProp != null && !sysProp.isBlank()) {
            return sysProp;
        }
        return System.getenv(ENV_CONFIG_PATH);
    }

    private static LinterConfig parseProperties(InputStream in, String sourceLabel) throws IOException {
        Properties props = new Properties();
        props.load(in);

        LinterConfig defaults = LinterConfig.defaults();
        return new LinterConfig(
                parsePositiveInt(props, "linter.max.jump.depth", defaults.maxJumpDepth(), sourceLabel),
                parsePositiveInt(props, "linter.max.if.nesting.depth", defaults.maxIfNestingDepth(), sourceLabel),
                parsePositiveInt(props, "linter.max.else.if.chain.length", defaults.maxElseIfChainLength(), sourceLabel),
                parsePositiveInt(props, "linter.max.cyclomatic.complexity", defaults.maxCyclomaticComplexity(), sourceLabel)
        );
    }

    private static int parsePositiveInt(Properties props, String key, int defaultValue, String sourceLabel) {
        String raw = props.getProperty(key);
        if (raw == null || raw.isBlank()) {
            return defaultValue;
        }
        try {
            int value = Integer.parseInt(raw.trim());
            if (value < 1) {
                throw new IllegalArgumentException(
                        String.format("Property '%s' in %s must be >= 1, got: %d", key, sourceLabel, value));
            }
            return value;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                    String.format("Invalid integer for property '%s' in %s: '%s'", key, sourceLabel, raw), e);
        }
    }
}
