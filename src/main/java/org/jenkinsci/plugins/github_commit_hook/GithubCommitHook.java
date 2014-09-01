package org.jenkinsci.plugins.github_commit_hook;

import java.io.IOException;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jenkins.model.Jenkins;

import net.sf.json.JSONException;
import net.sf.json.JSONObject;

import org.acegisecurity.Authentication;
import org.acegisecurity.context.SecurityContextHolder;

import hudson.Extension;
import hudson.model.AbstractProject;
import hudson.model.Hudson;
import hudson.security.ACL;
import hudson.triggers.Trigger;

import com.cloudbees.jenkins.GitHubRepositoryName;
import com.cloudbees.jenkins.GitHubRepositoryNameContributor;
import com.cloudbees.jenkins.GitHubTrigger;
import com.cloudbees.jenkins.GitHubWebHook;

import org.jenkinsci.plugins.github_commit_hook.GithubCommitHookJobProperty;

/**
 * GithubCommitWebHook {@link Builder}.
 */
@Extension
public class GithubCommitHook extends GitHubWebHook {

    private static final Pattern REPOSITORY_NAME_PATTERN = Pattern.compile("https?://([^/]+)/([^/]+)/([^/]+)");

    public static final String URLNAME = "github-commit-hook";

    @Override
    public String getUrlName() {
        return URLNAME;
    }

    public void setCommitJobProperty(AbstractProject<?, ?> project, String ref, String commit) {

        GithubCommitHookJobProperty jobProperty = project.getProperty(GithubCommitHookJobProperty.class);
        
        if (jobProperty == null) {
            jobProperty = new GithubCommitHookJobProperty(ref, commit);
            try {
                project.addProperty(jobProperty);
            } catch (IOException ioe) {
                LOGGER.info("Cant add jobProperty " + ref + " (" + commit + ")");
            }
        } else {
            jobProperty.setRef(ref);
            jobProperty.setCommit(commit);
        }
    }

    @Override
    public void processGitHubPayload(String payload, Class<? extends Trigger<?>> triggerClass) {

        String repoUrl;
        String pusherName;
        String ref;
        String commit;

        JSONObject o = JSONObject.fromObject(payload);

        // no commit set when create PR > we quit!
        if (o.getJSONObject("head_commit").isNullObject()) {
            LOGGER.fine("Empty head_commit ... skip.");
            return;
        }

        try {
            repoUrl = o.getJSONObject("repository").getString("url");
            pusherName = o.getJSONObject("pusher").getString("name");
            ref = o.getString("ref");
            commit = o.getJSONObject("head_commit").getString("id");
        } catch (JSONException jsone) {
            LOGGER.warning("Invalid payload: " + payload);
            return;
        }

        LOGGER.info("Received POST for " + repoUrl);
        LOGGER.fine("Full details of the POST was " + o.toString());

        Matcher matcher = REPOSITORY_NAME_PATTERN.matcher(repoUrl);

        if (matcher.matches()) {

            GitHubRepositoryName changedRepository = GitHubRepositoryName.create(repoUrl);

            if (changedRepository == null) {
                LOGGER.warning("Malformed repo url " + repoUrl);
                return;
            }

            // run in high privilege to see all the projects anonymous users
            // don't see.
            // this is safe because when we actually schedule a build, it's a
            // build that can
            // happen at some random time anyway.
            Authentication old = SecurityContextHolder.getContext().getAuthentication();
            SecurityContextHolder.getContext().setAuthentication(ACL.SYSTEM);

            try {
                for (AbstractProject<?, ?> job : Hudson.getInstance().getAllItems(AbstractProject.class)) {
                    GitHubTrigger trigger = (GitHubTrigger) job.getTrigger(triggerClass);
                    if (trigger != null) {
                        LOGGER.fine("Considering to poke " + job.getFullDisplayName());
                        if (GitHubRepositoryNameContributor.parseAssociatedNames(job).contains(changedRepository)) {
                            // set additional properties
                            setCommitJobProperty(job, ref, commit);
                            trigger.onPost(pusherName);
                        } else
                            LOGGER.fine("Skipped " + job.getFullDisplayName() + " because it doesn't have a matching repository.");
                    }
                }
            } finally {

                SecurityContextHolder.getContext().setAuthentication(old);

            }

            for (Listener listener : Jenkins.getInstance().getExtensionList(Listener.class)) {
                listener.onPushRepositoryChanged(pusherName, changedRepository);
            }

        } else {

            LOGGER.warning("Malformed repo url " + repoUrl);

        }
    }

    private static final Logger LOGGER = Logger.getLogger(GithubCommitHook.class.getName());
}