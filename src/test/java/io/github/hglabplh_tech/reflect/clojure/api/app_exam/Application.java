package io.github.hglabplh_tech.reflect.clojure.api.app_exam;

import javax.annotation.Nonnull;
import java.net.URL;
import java.util.Objects;

public class Application {

    public static String BLUBB = "Hallo";
    private final @Nonnull  String host;

    private final @Nonnull Integer port;

    private final @Nonnull  URL url;

    @Deprecated(since = "v1.78", forRemoval = true)
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

    private @Nonnull Integer sendRequest(@Nonnull  URL url, @Nonnull String host, @Nonnull Integer port) {
        System.out.println("I am called");
        return 0;
    }

    @Override
    public boolean equals(@Nonnull  Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
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
}
