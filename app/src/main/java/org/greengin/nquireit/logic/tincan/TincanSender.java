package org.greengin.nquireit.logic.tincan;

import gov.adlnet.xapi.client.StatementClient;
import gov.adlnet.xapi.model.Activity;
import gov.adlnet.xapi.model.Agent;
import gov.adlnet.xapi.model.Attachment;
import gov.adlnet.xapi.model.Statement;
import gov.adlnet.xapi.model.Verb;
import gov.adlnet.xapi.model.Verbs;
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
        StoreStatement(statement);
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
        StoreStatement(statement);
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
        StoreStatement(statement);
    }
    
    private static void StoreStatement(Statement statement) {
        try {
            System.out.println(AppDetails.LRS_URI);
            StatementClient client = new StatementClient(AppDetails.LRS_URI, AppDetails.LRS_USERNAME, AppDetails.LRS_PASSWORD);
            
            String id = client.postStatement(statement);
            // Should we do something with this id?
            System.out.println(id);
        } catch (Exception e) {
            LogManager.getLogger(TincanSender.class).error("An error occured while putting the following record:\nAgent: "
                    + statement.getActor()
                    + "\nVerb: " + statement.getVerb()
                    + "\nActivity: " + statement.getObject(), e);
        }
    }
}
