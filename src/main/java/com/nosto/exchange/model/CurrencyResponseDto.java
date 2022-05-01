package com.nosto.exchange.model;

import java.util.Map;
import lombok.Data;


@Data
public class CurrencyResponseDto {

  Map<String, String> symbols;

}
