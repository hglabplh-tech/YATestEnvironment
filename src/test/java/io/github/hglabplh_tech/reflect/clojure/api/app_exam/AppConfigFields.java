package io.github.hglabplh_tech.reflect.clojure.api.app_exam;


import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;

public enum AppConfigFields {
    HOST("Host", FieldType.STRING_TYPE, "localhost"),
    PORT("Port", FieldType.INT_TYPE, 80),
    SSL_PORT("SSLPort", FieldType.INT_TYPE, 443),
    URL_SPEC("URL", FieldType.URL_TYPE, null),
    USER("User", FieldType.STRING_TYPE, ":-NONE-:"),
    IP("IP", FieldType.STRING_TYPE, "127.0.0.1"),;

    private final String displayName;

    private final FieldType fldType;

    private final Object defaultValue;

    AppConfigFields(String displayName, FieldType fldType,
                    Object defaultValue) {
        this.displayName = displayName;
        this.fldType = fldType;
        this.defaultValue = defaultValue;
    }

    public String displayName() {
        return displayName;
    }

    public FieldType fldType() {
        return fldType;
    }

    public Object defaultValue() {
        return defaultValue;
    }

    public enum FieldType {
        LONG_TYPE("long", Long.TYPE),
        INT_TYPE("integer", Integer.TYPE),
        URL_TYPE("URL", java.net.URL.class),
        STRING_TYPE("string", String.class),;

        private final String typeName;

        private final Class<?> typeClass;

        FieldType(String typeName, Class<?> typeClass) {
            this.typeClass = typeClass;
            this.typeName = typeName;
        }

        public String typeName() {
            return typeName;
        }

        public Class<?> typeClass() {
            return typeClass;
        }
    }

}
