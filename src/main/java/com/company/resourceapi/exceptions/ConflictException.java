package com.company.resourceapi.exceptions;

public class ConflictException extends RuntimeException {
  private static final long serialVersionUID = 1L;

  private final String message;

  public ConflictException(String message) {
      this.message = message;
  }


  @Override
  public String getMessage() {
      return message;
  }
}