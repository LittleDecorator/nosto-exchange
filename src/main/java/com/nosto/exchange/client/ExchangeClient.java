package com.nosto.exchange.client;

import com.nosto.exchange.aop.Timed;
import com.nosto.exchange.model.ConvertResponseDto;
import com.nosto.exchange.model.CurrencyResponseDto;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(value = "api-exchange-client", url = "${app.exchange.url}")
public interface ExchangeClient {

  @Timed
  @GetMapping(path = "/convert")
  ConvertResponseDto convert(@RequestParam String amount, @RequestParam String from, @RequestParam String to);

  @Cacheable("symbols-cache")
  @GetMapping(path = "/symbols")
  CurrencyResponseDto getCurrencies();

}
