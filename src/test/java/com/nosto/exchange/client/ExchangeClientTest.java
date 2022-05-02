package com.nosto.exchange.client;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;

import com.nosto.exchange.client.ExchangeClientTest.TestConvertersConfig;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.cloud.openfeign.FeignAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

@Slf4j
@SpringBootTest
@ContextConfiguration(classes = {
  FeignAutoConfiguration.class,
  TestConvertersConfig.class,
})
@EnableFeignClients(
  clients = ExchangeClient.class
)
@ActiveProfiles("test")
@AutoConfigureWireMock(port = 5050)
class ExchangeClientTest {

  @Autowired
  ExchangeClient client;

  @Test
  @SneakyThrows
  void successfullyReceiveConvertedValue() {
    //ARRANGE
    var amount = "0.07";
    var from = "EUR";
    var to = "GBP";

    stubFor(
      get(urlEqualTo("/convert?amount=" + amount + "&from=" + from + "&to=" + to))
        .withHeader("apiKey", equalTo("c29tZVVzZXI6c29tZVBhc3N3b3Jk"))
        .willReturn(
          aResponse()
            .withHeader(CONTENT_TYPE, "application/json")
            .withBodyFile("convert.json")
        )
    );

    //ACT
    var responseDto = client.convert(amount, from, to);

    //ASSERT
    assertThat(responseDto.getResult()).isEqualTo(0.058705);
  }

  @Test
  void successfullyObtainAvailableCurrencySymbols() {
    //ARRANGE
    stubFor(
      get(urlEqualTo("/symbols"))
        .withHeader("apiKey", equalTo("c29tZVVzZXI6c29tZVBhc3N3b3Jk"))
        .willReturn(
          aResponse()
            .withHeader(CONTENT_TYPE, "application/json")
            .withBodyFile("symbols.json")
        )
    );

    //ACT
    var responseDto = client.getCurrencies();

    //ASSERT
    assertThat(responseDto.getSymbols()).containsKey("EUR");
  }

  @TestConfiguration
  public static class TestConvertersConfig {

    @Bean
    public HttpMessageConverters httpMessageConverters() {
      return new HttpMessageConverters();
    }

  }
}
