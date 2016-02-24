package org.greengin.nquireit.controllers.users;

import java.net.MalformedURLException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.greengin.nquireit.entities.users.UserProfile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import org.greengin.nquireit.logic.ContextBean;
import org.greengin.nquireit.logic.tincan.OAuth2Client;
import org.greengin.nquireit.logic.tincan.TokenResponse;
import org.greengin.nquireit.utils.AppDetails;

/**
 * This controller handles the redirects
 * after the authorization from a third party LRS using OAuth2.
 * @author Maxime Lasserre <maxlasserre@free.fr>
 */
@Controller
public class LrsAuthController {
    
    @Autowired
    ContextBean context;
 
    @RequestMapping(value = "/lrs/oauth/{id}", method = RequestMethod.GET)
    public String getAuthorizationCode(HttpServletRequest request, HttpServletResponse response, @PathVariable("id") Long id) {
        System.out.println(AppDetails.LRS_URI);
        UserProfile user =  context.getUsersManager().currentUser();
        if (user == null || !user.getId().equals(id)) {
            response.setStatus(403);
            return "403";
        }
        String code = request.getParameter("code");
        String lrsUrl = user.getMetadata().get(UserProfile.LRS_URL_KEY);
        String clientId = user.getMetadata().get(UserProfile.CLIENT_ID_KEY);
        String secret = user.getMetadata().get(UserProfile.CLIENT_SECRET_KEY);
        String accessTokenUrl = user.getMetadata().get(UserProfile.ACCESS_TOKEN_URL_KEY);
        String refreshToken = user.getMetadata().get(UserProfile.REFRESH_TOKEN_KEY);
        if (code == null || "".equals(code) 
                || lrsUrl == null || "".contains(lrsUrl)
                || clientId == null || "".equals(clientId)
                || secret == null || "".equals(secret)
                || accessTokenUrl == null || "".equals(accessTokenUrl)
                || refreshToken == null || "".equals(refreshToken)) {
            response.setStatus(403);
            return "403";
        }
        OAuth2Client client;
        try {
            client = new OAuth2Client(lrsUrl, clientId, secret, accessTokenUrl, code, refreshToken);
        } catch (MalformedURLException ex) {
            LogManager.getLogger(LrsAuthController.class.getName()).error("Malformed url before getting the auth code", ex);
            return "500";
        }
        TokenResponse token = client.requestToken();
        if (token == null) {
            response.setStatus(500);
            return "500";
        }
        user.getMetadata().put(UserProfile.TOKEN_KEY, token.getAccessToken());
        user.getMetadata().put(UserProfile.REFRESH_TOKEN_KEY, token.getRefreshToken());
        context.getUserProfileDao().updateUserInformation(user, user.getMetadata(), user.getVisibility());
        return "confirm-page";
    }
}
