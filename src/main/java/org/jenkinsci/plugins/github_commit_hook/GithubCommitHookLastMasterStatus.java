package org.jenkinsci.plugins.github_commit_hook;

import java.util.logging.Logger;

import org.kohsuke.stapler.DataBoundConstructor;

import hudson.Extension;
import hudson.model.BallColor;
import hudson.model.Job;
import hudson.model.Run;
import hudson.views.ListViewColumn;
import hudson.views.ListViewColumnDescriptor;

public class GithubCommitHookLastMasterStatus extends ListViewColumn {

    @DataBoundConstructor
    public GithubCommitHookLastMasterStatus() {
    }

    public BallColor getIconColor(Job<?, ?> job) {

        GithubCommitHookAction action = null;

        for (Run<?, ?> r : job.getBuilds()) {

            action = r.getAction(GithubCommitHookAction.class);

            if (action == null) {
                continue;
            }

            if (action.ref.equals("refs/heads/master")) {
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
