package org.jenkinsci.plugins.github_commit_hook;

import hudson.Extension;
import hudson.model.AbstractProject;
import hudson.model.JobProperty;
import hudson.model.JobPropertyDescriptor;

import net.sf.json.JSONObject;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;

import com.coravy.hudson.plugins.github.GithubProjectProperty;

public class GithubCommitHookJobProperty extends JobProperty<AbstractProject<?, ?>> {

    private String ref;
    private String commit;

    @DataBoundConstructor
    public GithubCommitHookJobProperty(String ref, String commit) {
        this.ref = ref;
        this.commit = commit;
    }

    public String getRef() {
        return ref;
    }

    public void setRef(String ref) {
        this.ref = ref;
    }

    public String getCommit() {
        return commit;
    }

    public void setCommit(String commit) {
        this.commit = commit;
    }

    @Extension
    public static final class DescriptorImpl extends JobPropertyDescriptor {

        public DescriptorImpl() {
            super(GithubCommitHookJobProperty.class);
            load();
        }

        public String getDisplayName() {
            return "GitHub commit";
        }

        @Override
        public JobProperty<?> newInstance(StaplerRequest req, JSONObject formData) throws FormException {
            GithubProjectProperty json = req.bindJSON(GithubProjectProperty.class, formData);
            if (json == null) {
                return null;
            }
            return json;
        }
    }
}
