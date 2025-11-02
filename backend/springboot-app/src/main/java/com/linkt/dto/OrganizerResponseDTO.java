package com.linkt.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrganizerResponseDTO {
    private Long userId;
    private String firstName;
    private String lastName;
    private String email;
    private String organizationName;
    private String phoneNumber;
    private String approvalStatus;
}
