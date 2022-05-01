package com.nosto.exchange.decoder;

import static feign.FeignException.errorStatus;

import com.nosto.exchange.exception.ExchangeClientException;
import feign.Response;
import feign.codec.ErrorDecoder;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ExchangeErrorDecoder implements ErrorDecoder {

  @Override
  public Exception decode(String methodKey, Response response) {
    var errorMessage = errorStatus(methodKey, response).contentUTF8();
    log.error("Decoding error response body: \n{}", errorMessage);
    return new ExchangeClientException(response.status(), errorMessage);
  }

}
