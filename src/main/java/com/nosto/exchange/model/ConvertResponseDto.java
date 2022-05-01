package com.nosto.exchange.model;

import java.time.Instant;
import java.time.LocalDate;
import lombok.Data;


@Data
public class ConvertResponseDto {

  LocalDate date;
  ConversionInfo info;
  OriginalQuery query;
  Double result;

  @Data
  public static class ConversionInfo {
    Double rate;
    Instant timestamp;
  }

  @Data
  public static class OriginalQuery {
    Double amount;
    String from;
    String to;
  }

}
