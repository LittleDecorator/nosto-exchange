package com.nosto.exchange.model;

import com.nosto.exchange.annotations.Currency;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import lombok.Data;

@Data
public class ConvertRequestDto {

  @Positive
  @NotNull
  Double value;
  @Currency
  String source;
  @Currency
  String target;

}
