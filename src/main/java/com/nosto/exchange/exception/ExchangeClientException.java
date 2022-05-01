package com.nosto.exchange.exception;

import lombok.Getter;

@Getter
public class ExchangeClientException extends RuntimeException {

  private final int status;

  public ExchangeClientException(int statusCode, String response) {
    super("Feign client failed with " + statusCode + " statusCode, response: " + response);
    this.status = statusCode;
  }
}
