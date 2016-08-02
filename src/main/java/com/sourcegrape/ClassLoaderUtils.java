package com.sourcegrape;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;

public class ClassLoaderUtils {

    public static boolean addDirectoryToClassLoader(ClassLoader loader, File directory) {
        try {
            ClassLoader rootLoader = getRootLoader(loader);
            if (rootLoader == null) {
                return false;
            } else {
                URL url = directory.toURI().toURL();
                Method method = rootLoader.getClass().getMethod("addURL", URL.class);
                method.invoke(rootLoader, url);
                return true;
            }
            
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
    
    private static ClassLoader getRootLoader(ClassLoader loader) {
        
        if (loader == null) {
            return null;
        } else if (isRootClassLoader(loader)) {
            return loader;
        } else {
            return getRootLoader(loader.getParent());
        }
    }

    private static boolean isRootClassLoader(ClassLoader loader) {
        return (loader != null) && isRootLoaderClass(loader.getClass());
    }
    
    private static boolean isRootLoaderClass(Class<?> loaderClass) {
        return (loaderClass != null) &&
            ((loaderClass.getName() == "groovy.lang.GroovyClassLoader") ||
             (loaderClass.getName() == "org.codehaus.groovy.tools.RootLoader") ||
             isRootLoaderClass(loaderClass.getSuperclass()));
    }
}
