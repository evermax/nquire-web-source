package org.greengin.nquireit.logic.tincan;

import gov.adlnet.xapi.client.StatementClient;
import gov.adlnet.xapi.model.Activity;
import gov.adlnet.xapi.model.Agent;
import gov.adlnet.xapi.model.Attachment;
import gov.adlnet.xapi.model.Statement;
import gov.adlnet.xapi.model.Verb;
import gov.adlnet.xapi.model.Verbs;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;
import org.apache.logging.log4j.LogManager;
import org.greengin.nquireit.entities.users.UserProfile;
import org.greengin.nquireit.utils.AppDetails;
import org.springframework.scheduling.annotation.Async;

/**
 * This class holds the code to interact with a LRS
 * using the Tincan standard
 * @author Maxime Lasserre
 */
public class TincanSender {
    
    /*
     * Project related methods
     */
    @Async
    public static void StoreCreateProject(UserProfile user, String projectId) {
        StoreProjectActivity(user, Verbs.initialized(), projectId, null);
    }
    @Async
    public static void StoreOpenProject(UserProfile user, String projectId) {
        StoreProjectActivity(user, Verbs.launched(), projectId, null);
    }
    
    @Async
    public static void StoreCloseProject(UserProfile user, String projectId) {
        StoreProjectActivity(user, Verbs.terminated(), projectId, null);
    }
    
    @Async
    public static void StoreJoinProject(UserProfile user, String projectId) {
        StoreProjectActivity(user, Verbs.attended(), projectId, null);
    }
    
    @Async
    public static void StoreLeaveProject(UserProfile user, String projectId) {
        StoreProjectActivity(user, Verbs.exited(), projectId, null);
    }
    
    @Async
    public static void StoreCommentProject(UserProfile user, String projectId, String comment) {
        Verb verb = Verbs.commented();
        
        HashMap<String, String> commentMap = new HashMap<String, String>();
        commentMap.put("comment", comment);
        Attachment attachment = new Attachment();
        attachment.setDisplay(commentMap);
        ArrayList<Attachment> attachments = new ArrayList<Attachment>();
        attachments.add(attachment);
        
        StoreProjectActivity(user, verb, projectId, attachments);
    }
    
    @Async
    public static void StoreSubmitAnswerWinitProject(UserProfile user, String projectId, HashMap<String, String> answer) {
        Verb verb = Verbs.answered();
        
        Attachment attachment = new Attachment();
        attachment.setDisplay(answer);
        ArrayList<Attachment> attachments = new ArrayList<Attachment>();
        attachments.add(attachment);
        
        StoreProjectActivity(user, verb, projectId, attachments);
    }
    
    @Async
    public static void SubmittedAnswerSenseItProject(UserProfile user, String projectId, ArrayList<String> fileUrls) {
        Verb verb = Verbs.answered();
        
        Attachment attachment = new Attachment();
        HashMap<String, String> commentMap = new HashMap<String, String>();
        for (int i = 0; i < fileUrls.size(); i ++) {
            commentMap.put("fileUrl" + (i+1), fileUrls.get(i));
        }
        ArrayList<Attachment> attachments = new ArrayList<Attachment>();
        attachments.add(attachment);
        
        StoreProjectActivity(user, verb, projectId, attachments);
    }
    
    @Async
    public static void SubmittedAnswerSpotItProject(UserProfile user, String projectId, String imageUrl) {
        Verb verb = Verbs.answered();
        
        Attachment attachment = new Attachment();
        HashMap<String, String> commentMap = new HashMap<String, String>();
        commentMap.put("imageUrl", imageUrl);
        ArrayList<Attachment> attachments = new ArrayList<Attachment>();
        attachments.add(attachment);
        
        StoreProjectActivity(user, verb, projectId, attachments);
    }
    
    /*
     * Forum related methods
     */
    
    @Async
    public static void StoreCreateForumThread(UserProfile user, Long forumId, Long forumThreadId, String threadName) {
        String username = user.getUsername();
        if (user.getMetadata().containsKey("name") && !"".equals(user.getMetadata().get("name"))) {
            username = user.getMetadata().get("name");
        }
        
        Agent agent = new Agent(username, "mailto:" + user.getEmail());
        Activity activity = new Activity(AppDetails.APP_URL + "/#/thread/" + forumThreadId);
        
        HashMap<String, String> commentMap = new HashMap<String, String>();
        commentMap.put("threadName", threadName);
        commentMap.put("forumUrl", AppDetails.APP_URL + "/#/forum/" + forumId);
        Attachment attachment = new Attachment();
        attachment.setDisplay(commentMap);
        ArrayList<Attachment> attachments = new ArrayList<Attachment>();
        attachments.add(attachment);
        
        Statement statement = new Statement(agent, Verbs.initialized(), activity);
        statement.setId(UUID.randomUUID().toString());
        statement.setAttachments(attachments);
        StoreStatement(statement, user);
    }
    
    @Async
    public static void StoreCommentForumThread(UserProfile user, Long forumThreadId, String comment) {
        String username = user.getUsername();
        if (user.getMetadata().containsKey("name") && !"".equals(user.getMetadata().get("name"))) {
            username = user.getMetadata().get("name");
        }
        
        Agent agent = new Agent(username, "mailto:" + user.getEmail());
        Activity activity = new Activity(AppDetails.APP_URL + "/#/thread/" + forumThreadId);
        
        HashMap<String, String> commentMap = new HashMap<String, String>();
        commentMap.put("comment", comment);
        Attachment attachment = new Attachment();
        attachment.setDisplay(commentMap);
        ArrayList<Attachment> attachments = new ArrayList<Attachment>();
        attachments.add(attachment);
        
        Statement statement = new Statement(agent, Verbs.commented(), activity);
        statement.setId(UUID.randomUUID().toString());
        statement.setAttachments(attachments);
        StoreStatement(statement, user);
    }

    
    /*
     * Private methods
     */
    private static void StoreProjectActivity(UserProfile user, Verb verb, String projectId, ArrayList<Attachment> attachments) {
        String username = user.getUsername();
        if (user.getMetadata().containsKey("name") && !"".equals(user.getMetadata().get("name"))) {
            username = user.getMetadata().get("name");
        }
        
        Agent agent = new Agent(username, "mailto:" + user.getEmail());
        Activity activity = new Activity(AppDetails.APP_URL + "/#/project/" + projectId);
        Statement statement = new Statement(agent, verb, activity);
        statement.setId(UUID.randomUUID().toString());
        statement.setAttachments(attachments);
        StoreStatement(statement, null);
    }
    
    private static void StoreStatement(Statement statement, UserProfile user) {
        StatementClient client;
        try {
            client = new StatementClient(AppDetails.LRS_URI, AppDetails.LRS_USERNAME, AppDetails.LRS_PASSWORD);
        } catch (MalformedURLException ex) {
            LogManager.getLogger(TincanSender.class).error(
                    String.format("Error while building our own LRS's client for the statement %s", statement.toString()), ex);
            return;
        }
        try {
            // Should we do something with the returned id
            client.postStatement(statement);
        } catch (Exception e) {
            LogManager.getLogger(TincanSender.class).error(
                    String.format("An error occured when posting the statement %s to our LRS", statement.toString()), e);
        }
    
        String clientId = user.getMetadata().get(UserProfile.CLIENT_ID_KEY);
        String secret = user.getMetadata().get(UserProfile.CLIENT_SECRET_KEY);
        String accessTokenUrl = user.getMetadata().get(UserProfile.ACCESS_TOKEN_URL_KEY);
        String lrsUrl = user.getMetadata().get(UserProfile.LRS_URL_KEY);
        if (clientId != null && !"".equals(clientId) 
                && secret != null && !"".equals(secret) 
                && accessTokenUrl != null && !"".equals(accessTokenUrl)
                && lrsUrl != null && !"".equals(lrsUrl)) {
            // Send to user specific LRS as well.
            StatementClient userClient;
            try {
                userClient = new StatementClient(lrsUrl, "", "");
            } catch (MalformedURLException ex) {
                LogManager.getLogger(TincanSender.class).error(
                        String.format("Error while building the user's LRS client for the statement %s", statement), ex);
                return;
            }
            try {
                userClient.postStatement(statement);
            } catch (IOException ex) {
                LogManager.getLogger(TincanSender.class).error(
                    String.format("An error occured when posting the statement %s to the user's LRS", statement.toString()), ex);
            }
        }
    }
}
