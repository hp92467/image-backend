package com.hp.imagebackend.model.dto.user;

import com.hp.imagebackend.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

@EqualsAndHashCode(callSuper = true)
@Data
public class UserQueryRequest extends PageRequest implements Serializable {

    
    private Long id;

    
    private String userName;

    
    private String userAccount;

    
    private String userProfile;

    
    private String userRole;

    private static final long serialVersionUID = 1L;
}
