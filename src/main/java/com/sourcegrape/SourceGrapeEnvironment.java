package com.sourcegrape;

import java.io.File;
import java.io.IOException;

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
}
