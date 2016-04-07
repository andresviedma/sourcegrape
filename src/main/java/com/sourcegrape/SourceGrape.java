package com.sourcegrape;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;

public class SourceGrape {
    public static void grab(ClassLoader classLoader, String ... uris) {
        Object rootLoader = getRootLoader(classLoader);
        for (String uri : uris) {
            GitGrape grape = new GitGrape(uri);
            File directory = grape.grab();
            addDirectoryToClassLoader(rootLoader, directory);
        }
    }
    
    private static void addDirectoryToClassLoader(Object rootLoader, File directory) {
        try {
            URL url = directory.toURI().toURL();
            Method method = rootLoader.getClass().getMethod("addURL", URL.class);
            method.invoke(rootLoader, url);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static void grab(String ... uris) {
        grab(SourceGrape.class.getClassLoader(), uris);
    }
    
    private static Object getRootLoader(ClassLoader loader) {
        
        if (loader == null) {
            return null;
        } else if (loader.getClass().getName().equals("org.codehaus.groovy.tools.RootLoader")) {
            return loader;
        } else {
            return getRootLoader(loader.getParent());
        }
    }
}
