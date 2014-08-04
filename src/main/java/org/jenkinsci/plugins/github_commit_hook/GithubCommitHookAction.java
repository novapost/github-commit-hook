package org.jenkinsci.plugins.github_commit_hook;

import hudson.EnvVars;
import hudson.model.EnvironmentContributingAction;
import hudson.model.InvisibleAction;
import hudson.model.AbstractBuild;

import org.kohsuke.stapler.export.ExportedBean;

@ExportedBean
public class GithubCommitHookAction extends InvisibleAction implements EnvironmentContributingAction {

    public static final String GIT_COMMIT_HOOK_REF = "GIT_COMMIT_HOOK_REF";
    public static final String GIT_COMMIT_HOOK_COMMIT = "GIT_COMMIT_HOOK_COMMIT";

    public final String ref;
    public final String commit;

    public GithubCommitHookAction(String ref, String commit) {
        this.ref = ref;
        this.commit = commit;
    }

    public void buildEnvVars(AbstractBuild<?, ?> build, EnvVars env) {
        env.put(GIT_COMMIT_HOOK_REF, ref);
        env.put(GIT_COMMIT_HOOK_COMMIT, commit);
    }

}