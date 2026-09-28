package com.devd.spring.bookstorecommons.web;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author: Devaraj Reddy,
 * Date : 2019-06-17
 *
 * Security fix (IT22189776): Removed password field to prevent sensitive data exposure
 * over inter-service Feign calls. OWASP A02:2021 - Cryptographic Failures / CWE-312.
 * Hashed passwords must not be included in inter-service API responses.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GetUserResponse {

    private String userId;
    private String userName;
    private String firstName;
    private String lastName;
    private String email;

}
