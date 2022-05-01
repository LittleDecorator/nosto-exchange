package com.nosto.exchange.configuration;

import java.time.Duration;
import javax.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Data
@Validated
@ConfigurationProperties(prefix = "app.cache")
public class CacheProperties {

  @NotNull
  private Duration ttl;

  @NotNull
  private Integer initialSize;

  @NotNull
  private Long maxSize;

}
