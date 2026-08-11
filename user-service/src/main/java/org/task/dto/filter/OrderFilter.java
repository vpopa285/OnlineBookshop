package org.task.dto.filter;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class OrderFilter {
    private String username;
    private LocalDateTime minDate;
    private LocalDateTime maxDate;
    private LocalDateTime exactDate;
}
