package com.devd.spring.bookstoreaccountservice.web;

import javax.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class GoogleLoginRequest {

  @NotBlank
  private String idToken;
}