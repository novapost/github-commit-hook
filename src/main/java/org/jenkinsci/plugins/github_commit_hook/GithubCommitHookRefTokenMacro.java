package org.jenkinsci.plugins.github_commit_hook;

import java.io.IOException;

import hudson.Extension;
import hudson.model.TaskListener;
import hudson.model.AbstractBuild;
import hudson.plugins.git.Branch;
import hudson.plugins.git.Revision;
import hudson.plugins.git.util.BuildData;

import org.jenkinsci.plugins.tokenmacro.DataBoundTokenMacro;
import org.jenkinsci.plugins.tokenmacro.MacroEvaluationException;

@Extension(optional=true)
public class GithubCommitHookRefTokenMacro extends DataBoundTokenMacro {

    @Parameter
    public int length = 20;

    @Override
    public boolean acceptsMacroName(String macroName) {
        return macroName.equals("GIT_COMMIT_HOOK_REF");
    }

    @Override
    public String evaluate(AbstractBuild<?, ?> build, TaskListener listener, String macroName)
            throws MacroEvaluationException, IOException, InterruptedException  {

        GithubCommitHookAction action = build.getAction(GithubCommitHookAction.class);
        if (action != null) {
            return format(action.ref);
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

    private String format(String name) {
        String branchName = name.substring(name.lastIndexOf('/')+1);
        return branchName.substring(0, Math.min(length, branchName.length()));
    }
}
