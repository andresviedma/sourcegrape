package com.sourcegrape;

import java.io.File;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;

public class GitGrape {
    
    private final String uri;
    
    private final SourceGrapeEnvironment environment = new SourceGrapeEnvironment();
    
    public GitGrape(String uri) {
        this.uri = uri;
    }

    public String getRepositoryUri() {
        return uri;
    }
    
    public String getRepositoryId() {
        return getRepositoryUri().replace(":", "")
                .replace("/", "_")
                .replace("\\", "_");
    }
    
    public File getCacheDirectory() {
        File root = new File(environment.getGrapeCacheDir(), "git");
        File repoDir = new File(root, getRepositoryId());
        if (!repoDir.exists()) {
            root.mkdirs();
        }
        return repoDir;
    }
    
    public File grab() {
        File repoDir = getCacheDirectory();
        File gitDir = new File(repoDir, ".git");
        if (gitDir.exists()) {
            pullRepo(repoDir);
        } else {
            cloneRepo(repoDir);
        }
        return repoDir;
    }
    
    private void cloneRepo(File repoDir) {
        try {
            String repoUri = getRepositoryUri();
            Git.cloneRepository()
                    .setURI(repoUri)
                    .setDirectory(repoDir)
                    .call();
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void pullRepo(File repoDir) {
        Git git = null;
        try {
            FileRepositoryBuilder builder = new FileRepositoryBuilder();
            Repository repository = builder.setGitDir(new File(repoDir, ".git"))
                .readEnvironment() // scan environment GIT_* variables
                .findGitDir() // scan up the file system tree
                .build();
            
            git = new Git(repository);
            git.pull()
                .call();

        } catch (Exception e) {
            e.printStackTrace();
            
        } finally {
            git.close();
        }
    }
}
