package org.jenkinsci.plugins.github_commit_hook;

import java.io.IOException;
import java.util.logging.Logger;

import hudson.Extension;
import hudson.model.AbstractProject;
import hudson.model.TaskListener;
import hudson.model.AbstractBuild;
import hudson.model.listeners.RunListener;
import hudson.plugins.git.RevisionParameterAction;

@SuppressWarnings("rawtypes")
@Extension
public class GithubCommitHookRunListner extends RunListener<AbstractBuild> {

    public GithubCommitHookRunListner() {
        super(AbstractBuild.class);
    }

    @Override
    public void onStarted(AbstractBuild build, TaskListener listener) {

        AbstractProject<?, ?> project = build.getProject();
        GithubCommitHookJobProperty jobProperty = project.getProperty(GithubCommitHookJobProperty.class);

        if (jobProperty == null) return;

        // restrict build to current commit
        build.addAction(new RevisionParameterAction(jobProperty.getCommit()));
        build.addAction(new GithubCommitHookAction(jobProperty.getRef(), jobProperty.getCommit()));

        try {
            project.removeProperty(GithubCommitHookJobProperty.class);
        } catch (IOException ioe) {
            LOGGER.info("No GithubCommitHookJobProperty found !!!");
        }
    }

    private static final Logger LOGGER = Logger.getLogger(GithubCommitHookRunListner.class.getName());
}
