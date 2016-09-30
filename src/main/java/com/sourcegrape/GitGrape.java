package com.sourcegrape;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.TransportCommand;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;

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
            authenticated(Git.cloneRepository()
                    .setURI(repoUri)
                    .setDirectory(repoDir))
                    .call();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private TransportCommand authenticated(TransportCommand command) {
        try {
            URI uri = new URI(getRepositoryUri());
            String userInfo = uri.getUserInfo();
            if (userInfo != null && userInfo.length() > 0) {
                String usr = "";
                String pwd = "";
                int separatorIndex = userInfo.indexOf(':');
                if (separatorIndex >= 0 && separatorIndex < userInfo.length() - 1) {
                    usr = userInfo.substring(0, separatorIndex);
                    pwd = userInfo.substring(separatorIndex + 1);
                } else {
                    usr = userInfo;
                }
                command.setCredentialsProvider(new UsernamePasswordCredentialsProvider(usr, pwd));
            }
            return command;

        } catch (URISyntaxException exception) {
            return command;
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
            authenticated(git.pull())
                .call();

        } catch (Exception e) {
            e.printStackTrace();

        } finally {
            git.close();
        }
    }
}
