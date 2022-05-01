package com.nosto.exchange.controller;

import com.nosto.exchange.client.ExchangeClient;
import com.nosto.exchange.model.ConvertRequestDto;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1/currencies")
@RequiredArgsConstructor()
public class CurrencyController {

  private final ExchangeClient client;

  @PostMapping("/exchange")
  public Double convert(@Validated @RequestBody ConvertRequestDto dto) {
    log.info("Convert monetary value={} from {} to {}", dto.getValue(), dto.getSource(), dto.getTarget());
    return client.convert(
      String.valueOf(dto.getValue()),
      dto.getSource(),
      dto.getTarget()
    ).getResult();
  }

  @GetMapping
  public Map<String, String> getCurrencies() {
    log.info("Get available currencies");
    return client.getCurrencies().getSymbols();
  }

}
