package com.pdd.pddai.dto;

import com.pdd.pddai.enums.TicketStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TicketStatusDto {
    private int ticketNumber;
    private TicketStatus status;
}
