package com.nosto.exchange.validator;

import com.nosto.exchange.annotations.Currency;
import com.nosto.exchange.client.ExchangeClient;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

public class CurrencyValidator implements ConstraintValidator<Currency, String> {

  @Autowired
  ExchangeClient exchangeClient;

  @Override
  public boolean isValid(String currency, ConstraintValidatorContext context) {

    if (StringUtils.isBlank(currency)) {
      setViolation(context, "Currency is required!");
      return false;
    }

    if (!exchangeClient.getCurrencies().getSymbols().containsKey(currency)) {
      setViolation(context, "Specified value is not supported!");
      return false;
    }

    return true;
  }

  private void setViolation(ConstraintValidatorContext context, String message) {
    context.disableDefaultConstraintViolation();
    context.buildConstraintViolationWithTemplate(message)
      .addConstraintViolation();
  }

}
