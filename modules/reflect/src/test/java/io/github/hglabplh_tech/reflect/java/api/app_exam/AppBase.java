package io.github.hglabplh_tech.reflect.java.api.app_exam;

import java.net.*;

public abstract class AppBase {

    public void testInsert() {

    }

    public static void main(String[] args) throws URISyntaxException, MalformedURLException {
        Application app= new Application("blubb", 8080, new URL("http://www.ibm.com"));

    }
}
