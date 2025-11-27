/**
 * This is a utility class for searching classes in packages and to set up the paths the classloader
 * has to search for matches
 *
 * @author: Harald Glab-Plhak
 */
package io.github.hglabplh_tech.reflect.clojure.api.utils;


import clojure.lang.IPersistentVector;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static io.github.hglabplh_tech.reflect.clojure.api.utils.ClojFunctionalUtils.getListAsLazyVector;


/**
 * The utility class  */
public class GetPackagesAndClasses {

    public static IPersistentVector findAllClassesUsingClassLoader(String packageName) {
        ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        assert classLoader != null;
        String path = packageName.replace('.', '/');
        List<Class> classes = new ArrayList<>();
        classLoader.resources(path)
                .forEach(url -> {
                    try {
                        classes.addAll(
                                findClasses(new File(url.getFile()),
                                        packageName));
                    } catch (ClassNotFoundException e) {
                        throw new RuntimeException(e);
                    }
                });
        return getListAsLazyVector(classes);

}


    /**
     * Recursive method used to find all classes in a given directory and subdirs.
     *
     * @param directory   The base directory
     * @param packageName The package name for classes found inside the base directory
     * @return The classes
     * @throws ClassNotFoundException
     */
    private static List<Class> findClasses(File directory, String packageName) throws ClassNotFoundException {
        List<Class> classes = new ArrayList<>();
        if (!directory.exists()) {
            return classes;
        }
        File[] files = directory.listFiles();
        for (File file : files) {
            if (file.isDirectory()) {
                assert !file.getName().contains(".");
                classes.addAll(findClasses(file, packageName + "." + file.getName()));
            } else if (file.getName().endsWith(".class")) {
                classes.add(Class.forName(packageName + '.' + file.getName().substring(0, file.getName().length() - 6)));
            }
        }
        return classes;
    }

    public static Class getClassByName(List<Class> classes,
                                                 String canonicalName) {
        return classes.stream().filter(clazz -> canonicalName
                .equals(clazz.getCanonicalName()))
                .findFirst().orElse(null);

    }
}
