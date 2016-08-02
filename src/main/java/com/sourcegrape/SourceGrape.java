package com.sourcegrape;

import java.io.File;

public class SourceGrape {
    
    public static void grab(ClassLoader classLoader, String ... uris) {
        for (String uri : uris) {
            GitGrape grape = new GitGrape(uri);
            File directory = grape.grab();
            ClassLoaderUtils.addDirectoryToClassLoader(classLoader, directory);
        }
    }

    public static void grab(String ... uris) {
        grab(SourceGrape.class.getClassLoader(), uris);
    }
}
