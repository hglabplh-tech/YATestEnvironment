/*
 * Copyright (c) 2026 Harald Glab-Plhak
 */

package io.github.hglabplh_tech.reflect.java.api.app_exam;

import io.github.hglabplh_tech.reflect.java.api.example.annot_rec.EmployeeType;


import javax.annotation.Nonnull;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;


public class Application extends AppBase implements AppListenerIfc {

    public static String BLUBB = "Hallo";

    private final List<String> infos = new ArrayList<>();

    private final Map<Integer, String> orders = new HashMap<>();

    private final @Nonnull  String host;

    private final @Nonnull Integer port;

    private final @Nonnull  URL url;

    public Application() {
        host = "local";
        port = 8080;
        try {
            url = new URL("http://localhost:8080");
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    public Application(@Nonnull  String host, @Nonnull Integer port, @Nonnull  URL url) {
        this.host = host;
        this.port = port;
        this.url = url;
    }

    public @Nonnull String host() {
        return host;
    }

    public @Nonnull Integer port() {
        return port;
    }

    public @Nonnull URL url() {
        return url;
    }

    public <T> T testParametrized(T term) throws ClassNotFoundException {
        String typeName = term.getClass().getCanonicalName();
        ClassLoader loader = Application.class.getClassLoader();
        return (T) loader.loadClass(typeName);
    }

    public @Nonnull Integer sendRequest(@Nonnull  final URL url, @Nonnull String host, @Nonnull Integer port, String... messages) {
        System.out.println("I am called");
        return 0;
    }

    public @Nonnull Integer addEmployee(@Nonnull final PersonRecord newPerson, @Nonnull EmployeeType type) {
        PersonRecord local = new PersonRecord(newPerson.clientNo(), newPerson.firstName(),
                newPerson.lastName(), newPerson.street(), newPerson.houseNumber(),
                newPerson.zipCode(), newPerson.city(), newPerson.country());
        storePerson(local);
        System.out.println("I am called");
        return 0;
    }

    private static Boolean storePerson(PersonRecord local) {
        StringBuilder builder = new StringBuilder(local.clientNo().toString())
                .append(local.firstName()).append(local.lastName()).append(local.street())
                .append(local.houseNumber()).append(local.zipCode()).append(local.city())
                .append(local.country());
        System.out.println(builder.toString());
        StringReader reader = new StringReader(builder.toString());
        return Boolean.TRUE;
    }


    @Override
    public boolean equals(@Nonnull  Object o) {
        if (this == o) return true;
        if (getClass() != o.getClass()) return false;
        Application that = (Application) o;
        return Objects.equals(host, that.host) && Objects.equals(port, that.port) && Objects.equals(url, that.url);
    }

    @Override
    public int hashCode() {
        return Objects.hash(host, port, url);
    }

    @Override
    public String toString() {
        return "Application{" +
                "host='" + host + '\'' +
                ", port=" + port +
                ", url=" + url +
                '}';
    }

    @Override
    public Integer countEmployees(String lastName) {
        return 0;
    }

    @Override
    public Boolean employeeAdded(String lastname) {
        return null;
    }

    public enum CATEGORY {
        NONE,
        ADMINISTRATION,
        PRODUCTION,
        COORDINATION
    }

    public record PersonRecord (UUID clientNo,
                                String firstName,
                                String lastName,
                                String street,
                                String houseNumber,
                                Integer zipCode,
                                String city,
                                String country

    ){};

    public static class Tuple<K extends Integer, E> {
        private final K key;

        private final E value;

        public Tuple(K key, E value) {
            this.key = key;
            this.value = value;
        }

        public K key() {
            return key;
        }

        public E value() {
            return value;
        }
    }

}
