package com.hp.imagebackend.model.dto.space.analyze;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class SpaceUserAnalyzeRequest extends SpaceAnalyzeRequest {

    
    private Long userId;

    
    private String timeDimension;
}
