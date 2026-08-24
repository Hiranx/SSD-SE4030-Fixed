package com.devd.spring.bookstoreapigatewayservice.exception;

import java.util.List;
import java.util.UUID;

public class ErrorResponse {

  private UUID uuid;
  private List<Error> errors;

  public ErrorResponse() {
  }

  public ErrorResponse(UUID uuid, List<Error> errors) {
    this.uuid = uuid;
    this.errors = errors;
  }

  public UUID getUuid() {
    return uuid;
  }

  public void setUuid(UUID uuid) {
    this.uuid = uuid;
  }

  public List<Error> getErrors() {
    return errors;
  }

  public void setErrors(List<Error> errors) {
    this.errors = errors;
  }
}