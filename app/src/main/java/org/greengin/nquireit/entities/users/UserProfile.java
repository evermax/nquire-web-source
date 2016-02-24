package org.greengin.nquireit.entities.users;

import lombok.Getter;
import lombok.Setter;
import org.greengin.nquireit.entities.AbstractEntity;
import org.greengin.nquireit.logic.ContextBean;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import javax.persistence.*;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

@Entity
public class UserProfile extends AbstractEntity implements UserDetails {
    
    /*
     * Series of keys for metadata for OAuth
     */
    public static final String LRS_URL_KEY = "lrs_url";
    public static final String CLIENT_ID_KEY = "client_id";
    public static final String CLIENT_SECRET_KEY = "client_secret";
    public static final String ACCESS_TOKEN_URL_KEY = "access_token_url";
    public static final String REFRESH_TOKEN_KEY = "refresh_token";
    public static final String TOKEN_KEY = "refresh_token";

    @Basic
    @Setter
    String username;

    @Basic
    @Setter
    String password;

    @Basic
    @Setter
    @Getter
    String email;

    @Basic
    @Setter
    @Getter
    String notify1 = "1";

    @Basic
    @Setter
    @Getter
    String notify2 = "1";

    @Basic
    @Setter
    @Getter
    String notify3 = "1";

    @Basic
    @Setter
    @Getter
    String notify4 = "1";

    @Basic
    @Setter
    @Getter
    String notify5 = "1";

    @Basic
    @Getter
    @Setter
    boolean emailConfirmed = false;

    @Basic
    @Setter
    @Getter
    boolean admin = false;

    @Basic
    @Setter
    @Getter
    String status = "";

    @Basic
    @Getter
    @Setter
    Date date;

    @Basic
    @Getter
    @Setter
    String image;

    @Lob
    @Getter
    @Setter
    HashMap<String, String> metadata = new HashMap<String, String>();

    @Lob
    @Setter
    HashMap<String, Boolean> visibility = new HashMap<String, Boolean>();

    public HashMap<String, Boolean> getVisibility() {
        if (visibility == null) {
            visibility = new HashMap<String, Boolean>();
        }

        for (String key : new String[] {"metadata", "projectsJoined", "projectsCreated"}) {
            if (!visibility.containsKey(key)) {
                visibility.put(key, true);
            }
        }
        
        for (String key : new String[] {LRS_URL_KEY, CLIENT_ID_KEY, CLIENT_SECRET_KEY,
                ACCESS_TOKEN_URL_KEY, REFRESH_TOKEN_KEY, TOKEN_KEY}) {
            visibility.put(key, false);
        }

        return visibility;
    }

    @Override
    public List<? extends GrantedAuthority> getAuthorities() {
        return null;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    public boolean isPasswordSet() {
        return password != null && password.length() > 0;
    }

    public boolean isLoggedIn() {
        return ContextBean.getContext().getUsersManager().isLoggedIn(this);
    }

    @Override
    public boolean equals(Object obj) {
        return obj != null && obj instanceof UserProfile && getId().equals(((UserProfile) obj).getId());
    }

}
