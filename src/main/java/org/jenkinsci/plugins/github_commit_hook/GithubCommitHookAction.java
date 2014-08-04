package org.jenkinsci.plugins.github_commit_hook;

import hudson.EnvVars;
import hudson.model.EnvironmentContributingAction;
import hudson.model.InvisibleAction;
import hudson.model.AbstractBuild;

import org.kohsuke.stapler.export.ExportedBean;

@ExportedBean
public class GithubCommitHookAction extends InvisibleAction implements EnvironmentContributingAction {

    public static final String GITHUB_HEAD_REF = "GITHUB_HEAD_REF";
    public static final String GITHUB_HEAD_COMMIT = "GITHUB_HEAD_COMMIT";

    public final String ref;
    public final String commit;

    public GithubCommitHookAction(String ref, String commit) {
        this.ref = ref;
        this.commit = commit;
    }

    public void buildEnvVars(AbstractBuild<?, ?> build, EnvVars env) {
        env.put(GITHUB_HEAD_REF, ref);
        env.put(GITHUB_HEAD_COMMIT, commit);
    }

}