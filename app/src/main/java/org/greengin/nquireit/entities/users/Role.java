package org.greengin.nquireit.entities.users;

import lombok.Getter;
import lombok.Setter;
import org.greengin.nquireit.entities.AbstractEntity;

import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;

@Entity
public class Role extends AbstractEntity {

	@ManyToOne
    @Getter
    @Setter
    UserProfile user;
	
	@ManyToOne
    @Getter
    @Setter
    AbstractEntity context;
	
	@Basic
    @Getter
    @Setter
    RoleType type;

}
