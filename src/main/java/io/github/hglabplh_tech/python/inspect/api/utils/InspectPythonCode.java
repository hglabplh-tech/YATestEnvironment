package io.github.hglabplh_tech.python.inspect.api.utils;

import clojure.lang.*;
import jep.Interpreter;
import jep.SharedInterpreter;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * Loads a Python inspection program from the class path and uses JEP to inspect
 * compiled CPython modules. Results are returned as immutable Clojure values.
 *
 * @author Harald Glab-Plhak
 * @since 1.4
 * (C) Copyright 2026 (Harald Glab-Plhak)
 */

public class InspectPythonCode implements AutoCloseable {

    /** Constant for the inspector code path*/
    private static final String INSPECTOR_RESOURCE = "/python/compiled_inspector.py";

    /** Constant for the properties path */
    private static final String INSPECTOR_PROPS = "/pycinspect.properties";

    /** Constant for the environment name of setting  the python venv*/
    private static final String PYTHON_VN = "PYTHON_VN";

    /** python major version*/
    private static final int REQUIRED_PYTHON_MAJOR = 3;
    private static final int REQUIRED_PYTHON_MINOR = 14;


    private final Interpreter python;
    private final Path virtualEnvironment;

    public InspectPythonCode(Path virtualEnvironment) {
        this.virtualEnvironment = validateVirtualEnvironment(virtualEnvironment);
        validateVirtualEnvironmentInterpreter(this.virtualEnvironment);

        this.python = new SharedInterpreter();
        configureVirtualEnvironment();
        validateEmbeddedPython();
        this.python.exec(readResource(INSPECTOR_RESOURCE));
    }

    /**
     * Return the path of the python environment
     * @return the absolute path to env
     * @throws IOException I/O error reading properties
     * @since 1.4
     */
    private static Path resolveVirtualEnvironment() throws IOException {

        String value;

        Properties props = new Properties();
        props.load(resolveResource(INSPECTOR_PROPS));
        if (props.getProperty(PYTHON_VN) != null) {
            value = props.getProperty(PYTHON_VN);
        } else if (System.getProperty(PYTHON_VN) != null) {
            value = System.getProperty(PYTHON_VN);
        } else {
            value = System.getenv(PYTHON_VN);
        }

        if (value == null || value.isBlank()) {
            throw new IllegalStateException(
                    "Environment variable " + PYTHON_VN
                            + " must point to a Python 3.14 virtual environment"
            );
        }
        return Paths.get(value).toAbsolutePath().normalize();
    }

    private static Path validateVirtualEnvironment(Path path) {
        Objects.requireNonNull(path, "virtualEnvironment");
        Path normalized = path.toAbsolutePath().normalize();
        if (!Files.isDirectory(normalized)) {
            throw new IllegalStateException("PYTHON_VN is not a directory: " + normalized);
        }
        if (!Files.isRegularFile(normalized.resolve("pyvenv.cfg"))) {
            throw new IllegalStateException(
                    "PYTHON_VN is not a Python virtual environment; pyvenv.cfg is missing: "
                            + normalized
            );
        }
        return normalized;
    }

    public IPersistentMap runtimeInfo() {
        Object raw = python.invoke("_jep_runtime_info", virtualEnvironment.toString());
        return requirePersistentMap(convert(raw), "runtime information");
    }

    public static Path virtualEnvironmentPython(Path virtualEnvironment) {
        boolean windows = System.getProperty("os.name", "").toLowerCase().contains("win");
        return windows
                ? virtualEnvironment.resolve("Scripts").resolve("python.exe")
                : virtualEnvironment.resolve("bin").resolve("python");
    }

    private static String[] virtualEnvironmentSitePackages(Path virtualEnvironment) {
        Set<String> paths = new LinkedHashSet<>();
        paths.add(virtualEnvironment.resolve("Lib").resolve("site-packages").toString());
        paths.add(virtualEnvironment.resolve("lib").resolve("python3.14")
                .resolve("site-packages").toString());
        paths.add(virtualEnvironment.resolve("lib64").resolve("python3.14")
                .resolve("site-packages").toString());
        return paths.toArray(String[]::new);
    }


    private void configureVirtualEnvironment() {
        python.set("_java_virtual_env", virtualEnvironment.toString());
        python.set("_java_site_packages", virtualEnvironmentSitePackages(virtualEnvironment));
        python.exec("""
                import os
                import site
                import sys
                
                _vn = os.path.abspath(_java_virtual_env)
                for _path in reversed(list(_java_site_packages)):
                    if os.path.isdir(_path):
                        site.addsitedir(_path)
                
                # JEP embeds a fixed CPython runtime. We intentionally do not replace
                # sys.prefix; the virtualenv is used as the dependency/package source.
                os.environ["VIRTUAL_ENV"] = _vn
                """);
    }

    private void validateEmbeddedPython() {
        python.exec("""
                import sys
                if sys.version_info[:2] != (3, 14):
                    raise RuntimeError(
                        "JEP must embed CPython 3.14, but embeds "
                        + f"{sys.version_info.major}.{sys.version_info.minor}.{sys.version_info.micro}"
                    )
                """);
    }


    @Override
    public void close() {
        python.close();
    }

    public IPersistentMap inspectModule(String moduleName, Path compiledModule) {
        return inspectModule(moduleName, compiledModule, false, false);
    }

    public IPersistentMap inspectModule(
            String moduleName,
            Path compiledModule,
            boolean includeInherited,
            boolean includeImported
    ) {
        validate(moduleName, compiledModule);
        Object raw = python.invoke(
                "inspect_compiled_module",
                moduleName,
                compiledModule.toAbsolutePath().toString(),
                includeInherited,
                includeImported
        );
        return requirePersistentMap(convert(raw), "module inspection result");
    }

    private static void validateVirtualEnvironmentInterpreter(Path virtualEnvironment) {
        Path executable = virtualEnvironmentPython(virtualEnvironment);
        if (!Files.isRegularFile(executable)) {
            throw new IllegalStateException("Python executable not found in PYTHON_VN: " + executable);
        }
        try {
            Process process = new ProcessBuilder(
                    executable.toString(),
                    "-c",
                    "import sys; print(f'{sys.version_info.major}.{sys.version_info.minor}')"
            ).redirectErrorStream(true).start();
            String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8).trim();
            int exit = process.waitFor();
            if (exit != 0) {
                throw new IllegalStateException(
                        "Could not execute PYTHON_VN interpreter (exit " + exit + "): " + output
                );
            }
            String required = REQUIRED_PYTHON_MAJOR + "." + REQUIRED_PYTHON_MINOR;
            if (!required.equals(output)) {
                throw new IllegalStateException(
                        "PYTHON_VN must use Python " + required + ", but uses " + output
                );
            }
        } catch (IOException exception) {
            throw new IllegalStateException("Could not start PYTHON_VN interpreter", exception);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while validating PYTHON_VN", exception);
        }
    }

    /**
     * Inspect a python class from a pyc
     *
     * @param moduleName     the module name
     * @param compiledModule the compiled module path
     * @param className      the name of the class
     * @return the persistent
     * Map for clojure describing the pyc class
     * @since 1.4
     */
    public IPersistentMap inspectClass(
            String moduleName,
            Path compiledModule,
            String className
    ) {
        return inspectClass(moduleName, compiledModule, className, false);
    }

    public IPersistentMap inspectClass(
            String moduleName,
            Path compiledModule,
            String className,
            boolean includeInherited
    ) {
        validate(moduleName, compiledModule);
        Objects.requireNonNull(className, "className");

        Object raw = python.invoke(
                "inspect_compiled_class",
                moduleName,
                compiledModule.toAbsolutePath().toString(),
                className,
                includeInherited
        );
        return requirePersistentMap(convert(raw), "class inspection result");
    }

    private static void validate(String moduleName, Path compiledModule) {
        Objects.requireNonNull(moduleName, "moduleName");
        Objects.requireNonNull(compiledModule, "compiledModule");
    }

    /**
     * Recursively converts JEP Java containers to persistent Clojure values.
     */
    private static Object convert(Object value) {
        if (value == null || value instanceof String || value instanceof Number || value instanceof Boolean) {
            return value;
        }

        if (value instanceof Map<?, ?> map) {
            IPersistentMap result = PersistentArrayMap.EMPTY;
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                Object key = entry.getKey() instanceof String text
                        ? Keyword.intern(null, text)
                        : convert(entry.getKey());
                result = result.assoc(key, convert(entry.getValue()));
            }
            return result;
        }

        if (value instanceof List<?> list) {
            IPersistentVector result = PersistentVector.EMPTY;
            for (Object item : list) {
                result = result.cons(convert(item));
            }
            return result;
        }

        if (value.getClass().isArray()) {
            IPersistentVector result = PersistentVector.EMPTY;
            int length = java.lang.reflect.Array.getLength(value);
            for (int i = 0; i < length; i++) {
                result = result.cons(convert(java.lang.reflect.Array.get(value, i)));
            }
            return result;
        }

        return String.valueOf(value);
    }

    private static IPersistentMap requirePersistentMap(Object value, String description) {
        if (value instanceof IPersistentMap map) {
            return map;
        }
        throw new IllegalStateException(
                description + " must be a map, got " +
                        (value == null ? "null" : value.getClass().getName())
        );
    }

    private static InputStream resolveResource(String resourceName) {
        try (InputStream input = InspectPythonCode.class.getResourceAsStream(resourceName)) {
            if (input == null) {
                throw new IllegalStateException("Resource not found: " + resourceName);
            }
            return input;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static String readResource(String resourceName) {

        try (InputStream input = resolveResource(resourceName)) {
            return new String(input.readAllBytes(), StandardCharsets.UTF_8);

        } catch (IOException exception) {
            throw new IllegalStateException("Could not read resource: " + resourceName, exception);
        }
    }
}
