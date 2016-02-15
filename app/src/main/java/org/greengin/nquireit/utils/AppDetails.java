package org.greengin.nquireit.utils;

/**
 * 
 * Holds the informations about the app.
 * For instance the appUrl.
 * Is populated at runtime.
 * @author Maxime Lasserre <maxlasserre@free.fr>
 */
public class AppDetails {
    public static String APP_URL = "";
    public static String LRS_URI = "";
    public static String LRS_USERNAME = "";
    public static String LRS_PASSWORD = "";
    public static String MAIL_SMTP_HOST = "";
    public static String MAIL_SENDER = "";
    public static String MAIL_NAME = "";
    
    public static void setAppDetails(String appUrl, String lrsUrl, String lrsUsername,
            String lrsPassword, String mailSmtpHost, String mailSender, String mailName) {
        AppDetails.APP_URL = appUrl;
        AppDetails.LRS_URI = lrsUrl;
        AppDetails.LRS_USERNAME = lrsUsername;
        AppDetails.LRS_PASSWORD = lrsPassword;
        AppDetails.MAIL_SMTP_HOST = mailSmtpHost;
        AppDetails.MAIL_SENDER = mailSender;
        AppDetails.MAIL_NAME = mailName;
    }
}
