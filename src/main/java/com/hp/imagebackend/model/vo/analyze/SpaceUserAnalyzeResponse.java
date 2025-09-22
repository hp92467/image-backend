package com.hp.imagebackend.model.vo.analyze;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SpaceUserAnalyzeResponse implements Serializable {
@Data
public class SpaceUserAddRequest implements Serializable {
    private Long spaceId;
    private Long userId;
    private String spaceRole;
    private static final long serialVersionUID = 1L;
}@Data
public class SpaceUserEditRequest implements Serializable {
    private Long id; // space_user 表的主键 id
    private String spaceRole;
    private static final long serialVersionUID = 1L;
}
    
    private String period;

    
    private Long count;

    private static final long serialVersionUID = 1L;
}