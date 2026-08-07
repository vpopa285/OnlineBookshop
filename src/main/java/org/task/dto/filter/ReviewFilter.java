package org.task.dto.filter;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReviewFilter {
    private Integer minRate;
    private Integer maxRate;
    private Integer exactRate;
}
