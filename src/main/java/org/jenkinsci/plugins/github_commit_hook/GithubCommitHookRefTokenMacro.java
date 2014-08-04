package org.jenkinsci.plugins.github_commit_hook;

import java.io.IOException;

import hudson.Extension;
import hudson.model.AbstractProject;
import hudson.model.TaskListener;
import hudson.model.AbstractBuild;

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

        if (jobProperty == null) {
            return "";
        }

        return jobProperty.getRef();
    }
}
