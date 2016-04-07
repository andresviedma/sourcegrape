package com.sourcegrape;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import  org.codehaus.groovy.reflection.ReflectionUtils;

public class SourceGrapeEnvironment {
    public File getGroovyRoot() {
        String root = System.getProperty("groovy.root");
        File groovyRoot;
        if (root == null) {
            groovyRoot = new File(System.getProperty("user.home"), ".groovy");
        } else {
            groovyRoot = new File(root);
        }
        try {
            groovyRoot = groovyRoot.getCanonicalFile();
        } catch (IOException e) {
            // skip canonicalization then, it may not exist yet
        }
        return groovyRoot;
    }

    public File getLocalGrapeConfig() {
        String grapeConfig = System.getProperty("sourcegrape.config");
        if (grapeConfig != null) {
            return new File(grapeConfig);
        }
        return new File(getGrapeDir(), "sourceGrapeConfig.yaml");
    }
    
    public File getGrapeDir() {
        String root = System.getProperty("sourcegrape.root");
        if(root == null) {
            return getGroovyRoot();
        }
        File grapeRoot = new File(root);
        try {
            grapeRoot = grapeRoot.getCanonicalFile();
        } catch (IOException e) {
            // skip canonicalization then, it may not exist yet
        }
        return grapeRoot;
    }
    
    public File getGrapeCacheDir() {
        File cache =  new File(getGrapeDir(), "sourcegrapes");
        if (!cache.exists()) {
            cache.mkdirs();
        } else if (!cache.isDirectory()) {
            throw new RuntimeException("The grape cache dir " + cache + " is not a directory");
        }
        return cache;
    }
    
    @SuppressWarnings("rawtypes")
    public ClassLoader chooseClassLoader(Map args) {
        ClassLoader loader = (ClassLoader) args.get("classLoader");
        if (!isValidTargetClassLoader(loader)) {
            Object refObject = args.get("refObject");
            if (refObject != null) {
                loader = refObject.getClass().getClassLoader();
            } else {
                Object calleeDepthArg = args.get("calleeDepth");
                int calleeDepth = 1;
                if (calleeDepthArg != null) {
                    Integer.parseInt(calleeDepthArg.toString());
                }
                loader = ReflectionUtils.getCallingClass(calleeDepth).getClassLoader();
            }
            while ((loader != null) && !isValidTargetClassLoader(loader)) {
                loader = loader.getParent();
            }
            //if (!isValidTargetClassLoader(loader)) {
            //    loader = Thread.currentThread().contextClassLoader
            //}
            //if (!isValidTargetClassLoader(loader)) {
            //    loader = GrapeIvy.class.classLoader
            //}
            if (!isValidTargetClassLoader(loader)) {
                throw new RuntimeException("No suitable ClassLoader found for grab");
            }
        }
        return loader;
    }
    
    private boolean isValidTargetClassLoader(ClassLoader loader) {
        return (loader != null) && isValidTargetClassLoaderClass(loader.getClass());
    }
    
    private boolean isValidTargetClassLoaderClass(Class<?> loaderClass) {
        return (loaderClass != null) &&
            (
             (loaderClass.getName() == "groovy.lang.GroovyClassLoader") ||
             (loaderClass.getName() == "org.codehaus.groovy.tools.RootLoader") ||
             isValidTargetClassLoaderClass(loaderClass.getSuperclass())
            );
    }
}
