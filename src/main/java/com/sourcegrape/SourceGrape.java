package com.sourcegrape;

import java.io.File;
import java.net.MalformedURLException;

import org.codehaus.groovy.tools.RootLoader;

public class SourceGrape {
    public static void grab(ClassLoader classLoader, String ... uris) {
        try {
            RootLoader rootLoader = getRootLoader(classLoader);
            for (String uri : uris) {
                GitGrape grape = new GitGrape(uri);
                File directory = grape.grab();
                rootLoader.addURL(directory.toURI().toURL());
            }
            
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    public static void grab(String ... uris) {
        grab(SourceGrape.class.getClassLoader(), uris);
    }
    
    private static RootLoader getRootLoader(ClassLoader loader) {
        
        if (loader == null) {
            return null;
        } else if (loader.getClass().getName().equals("org.codehaus.groovy.tools.RootLoader")) {
            return (RootLoader) loader;
        } else {
            return getRootLoader(loader.getParent());
        }
    }
}
