package org.jenkinsci.plugins.github_commit_hook;

import org.jenkinsci.plugins.tokenmacro.DataBoundTokenMacro.Parameter;
import org.kohsuke.stapler.DataBoundConstructor;

import hudson.Extension;
import hudson.model.AbstractBuild;
import hudson.model.BallColor;
import hudson.model.Job;
import hudson.model.Run;
import hudson.plugins.git.Branch;
import hudson.plugins.git.Revision;
import hudson.plugins.git.util.BuildData;
import hudson.views.ListViewColumn;
import hudson.views.ListViewColumnDescriptor;

public class GithubCommitHookLastMasterStatus extends ListViewColumn {

    @Parameter
    public int length = 20;

    @DataBoundConstructor
    public GithubCommitHookLastMasterStatus() {
    }

    private String getBuildBranchName(AbstractBuild<?, ?> build) {

        GithubCommitHookAction action = build.getAction(GithubCommitHookAction.class);
        if (action != null) {
            return format(action.ref);
        }

        BuildData data = build.getAction(BuildData.class);
        if (data == null) {
            return "";
        }

        Revision revision = data.getLastBuiltRevision();
        if (revision == null || revision.getBranches().isEmpty()) {
            return "";
        }

        // return same value as GitBranchTokenMacro otherwise
        Branch branch = revision.getBranches().iterator().next();
        return format(branch.getName());
    }

    private String format(String name) {
        String branchName = name.substring(name.lastIndexOf('/')+1);
        return branchName.substring(0, Math.min(length, branchName.length()));
    }

    public BallColor getIconColor(Job<?, ?> job) {

        for (Run<?, ?> r : job.getBuilds()) {

            String branchName = getBuildBranchName((AbstractBuild<?, ?>) r);

            if (branchName.equals("master")) {
                return r.getIconColor();
            }
        }

        return BallColor.DISABLED;
    }

    @Extension
    public static class DescriptorImpl extends ListViewColumnDescriptor {
        @Override
        public String getDisplayName() {
            return "Last Master Status";
        }

        @Override
        public boolean shownByDefault() {
            return false;
        }
    }
}
