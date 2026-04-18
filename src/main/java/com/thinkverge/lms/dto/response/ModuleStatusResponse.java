package com.thinkverge.lms.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ModuleStatusResponse {
    private Long moduleId;
    private String moduleTitle;
    private Integer orderIndex;
    private boolean lessonsCompleted;   // all lessons in module done
    private boolean quizExists;
    private boolean quizPassed;
    private boolean unlocked;           // can student access this module?
    private Integer quizBestScore;      // null if never attempted
    private Integer passingScore;
}
