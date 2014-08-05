package org.jenkinsci.plugins.github_commit_hook;

import java.io.IOException;

import hudson.Extension;
import hudson.model.AbstractProject;
import hudson.model.TaskListener;
import hudson.model.AbstractBuild;
import hudson.plugins.git.Branch;
import hudson.plugins.git.Revision;
import hudson.plugins.git.util.BuildData;

import org.jenkinsci.plugins.tokenmacro.DataBoundTokenMacro;
import org.jenkinsci.plugins.tokenmacro.MacroEvaluationException;

@Extension(optional=true)
public class GithubCommitHookRefTokenMacro extends DataBoundTokenMacro {

    @Override
    public boolean acceptsMacroName(String macroName) {
        return macroName.equals("GIT_COMMIT_HOOK_REF");
    }

    @Override
    public String evaluate(AbstractBuild<?, ?> build, TaskListener listener, String macroName)
            throws MacroEvaluationException, IOException, InterruptedException  {

        AbstractProject<?, ?> project = build.getProject();
        GithubCommitHookJobProperty jobProperty = project.getProperty(GithubCommitHookJobProperty.class);

        // return our property by default because build was triggered by our
        // hoook.
        if (jobProperty != null) {
            return format(jobProperty.getRef());
        }

        BuildData data = build.getAction(BuildData.class);
        if (data == null) {
            return "";
        }

        Revision lb = data.getLastBuiltRevision();
        if (lb==null || lb.getBranches().isEmpty()) {
            return "";
        }

        // return same value as GitBranchTokenMacro otherwise
        Branch branch = lb.getBranches().iterator().next();
        return format(branch.getName());
    }

    private String format(String branchName) {
        return branchName.substring(branchName.lastIndexOf('/')+1);
    }
}
