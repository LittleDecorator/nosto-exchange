package com.nosto.exchange.controller;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nosto.exchange.client.ExchangeClient;
import com.nosto.exchange.model.ConvertRequestDto;
import com.nosto.exchange.model.ConvertResponseDto;
import com.nosto.exchange.model.ConvertResponseDto.ConversionInfo;
import com.nosto.exchange.model.ConvertResponseDto.OriginalQuery;
import com.nosto.exchange.model.CurrencyResponseDto;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Map;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;


@ExtendWith(SpringExtension.class)
@WebMvcTest({CurrencyController.class})
class CurrencyControllerTest {

  @Autowired
  MockMvc mockMvc;

  @MockBean
  ExchangeClient exchangeClient;

  final ObjectMapper mapper = new ObjectMapper();

  @SneakyThrows
  @ParameterizedTest
  @CsvSource({
    "1, EURR, GBP, source",
    "1, EUR, GBPP, target"
  })
  void whenWrongCurrencySendThen400Error(Double value, String from, String to, String field) {
    //GIVEN
    var payload = new ConvertRequestDto()
      .setValue(value)
      .setSource(from)
      .setTarget(to);

    //WHEN
    when(exchangeClient.getCurrencies()).thenReturn(new CurrencyResponseDto().setSymbols(
      Map.of(
        "EUR", "Euro",
        "GBP", "British Pound Sterling"
      )
    ));

    // ACT & THEN
    mockMvc
      .perform(
        post("/api/v1/currencies/exchange")
          .contentType(MediaType.APPLICATION_JSON_VALUE)
          .content(mapper.writeValueAsString(payload))
      )
      .andExpect(status().is4xxClientError())
      .andExpect(jsonPath("$.status", is("BAD_REQUEST")))
      .andExpect(jsonPath("$.message", is("Validation error")))
      .andExpect(jsonPath("$.errors[0].field", is(field)))
      .andExpect(jsonPath("$.errors[0].rejectedValue", is(field.equals("source") ? from : to)))
      .andExpect(jsonPath("$.errors[0].message", is("Specified value is not supported!")))
    ;
  }

  @SneakyThrows
  @ParameterizedTest
  @MethodSource("testData")
  void whenAnyParamIsMissingThen400Error(Double value, String from, String to, String expectedMessage) {
    //GIVEN
    var payload = new ConvertRequestDto()
      .setValue(value)
      .setSource(from)
      .setTarget(to);

    //WHEN
    when(exchangeClient.getCurrencies()).thenReturn(new CurrencyResponseDto().setSymbols(
      Map.of(
        "EUR", "Euro",
        "GBP", "British Pound Sterling"
      )
    ));

    // ACT & THEN
    mockMvc
      .perform(
        post("/api/v1/currencies/exchange")
          .contentType(MediaType.APPLICATION_JSON_VALUE)
          .content(mapper.writeValueAsString(payload))
      )
      .andExpect(status().is4xxClientError())
      .andExpect(jsonPath("$.status", is("BAD_REQUEST")))
      .andExpect(jsonPath("$.message", is("Validation error")))
      .andExpect(jsonPath("$.errors[0].message", is(expectedMessage)))
    ;
  }

  @SneakyThrows
  @ParameterizedTest
  @CsvSource({"-1", "0"})
  void whenNonPositiveValueSendThen400Error(Double value) {

    //GIVEN
    var payload = new ConvertRequestDto()
      .setValue(value)
      .setSource("EUR")
      .setTarget("GBP");

    //WHEN
    when(exchangeClient.getCurrencies()).thenReturn(new CurrencyResponseDto().setSymbols(
      Map.of(
        "EUR", "Euro",
        "GBP", "British Pound Sterling"
      )
    ));

    // ACT & THEN
    mockMvc
      .perform(
        post("/api/v1/currencies/exchange")
          .contentType(MediaType.APPLICATION_JSON_VALUE)
          .content(mapper.writeValueAsString(payload))
      )
      .andExpect(status().is4xxClientError())
      .andExpect(jsonPath("$.status", is("BAD_REQUEST")))
      .andExpect(jsonPath("$.message", is("Validation error")))
      .andExpect(jsonPath("$.errors[0].message", is("must be greater than 0")));
  }

  @Test
  @SneakyThrows
  void whenWrongValueTypeSendThen400Error() {
    mockMvc
      .perform(
        post("/api/v1/currencies/exchange")
          .contentType(MediaType.APPLICATION_JSON_VALUE)
          .content("{\"value\": \"badValue\",\"source\":\"EUR\",\"target\":\"GBP\"}"))
      .andExpect(status().is4xxClientError())
      .andExpect(jsonPath("$.status", is("BAD_REQUEST")))
      .andExpect(jsonPath("$.message", containsString("\"badValue\": not a valid `Double` value")));
  }

  @Test
  @SneakyThrows
  void whenCorrectRequestSendThen200Ok() {
    //GIVEN
    var request = new ConvertRequestDto()
      .setValue(0.07)
      .setSource("EUR")
      .setTarget("GBP");

    var response = new ConvertResponseDto()
      .setResult(0.058705)
      .setDate(LocalDate.parse("2022-04-30"))
      .setInfo(
        new ConversionInfo()
          .setRate(0.838639)
          .setTimestamp(Instant.ofEpochMilli(1651352103))
      )
      .setQuery(
        new OriginalQuery()
          .setAmount(request.getValue())
          .setFrom(request.getSource())
          .setTo(request.getTarget())
      );

    //WHEN
    when(exchangeClient.convert(anyString(), anyString(), anyString())).thenReturn(response);
    when(exchangeClient.getCurrencies()).thenReturn(new CurrencyResponseDto().setSymbols(
      Map.of(
        "EUR", "Euro",
        "GBP", "British Pound Sterling"
      )
    ));

    // ACT
    var result = mockMvc
      .perform(
        post("/api/v1/currencies/exchange")
          .contentType(MediaType.APPLICATION_JSON_VALUE)
          .content(mapper.writeValueAsString(request))
      );

    //THEN
    verify(exchangeClient, times(2)).getCurrencies();
    verify(exchangeClient).convert(String.valueOf(request.getValue()), request.getSource(), request.getTarget());

    result
      .andDo(print())
      .andExpect(status().is2xxSuccessful())
      .andExpect(content().string(String.valueOf(response.getResult())));
  }

  static Object[][] testData() {
    return new Object[][]{
      {
        null, "EUR", "GBP", "must not be null"
      },
      {
        1.0, null, "GBP", "Currency is required!"
      },
      {
        1.0, "EUR", null, "Currency is required!"
      }
    };
  }

}
