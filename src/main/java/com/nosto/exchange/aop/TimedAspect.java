package com.nosto.exchange.aop;

import java.time.Duration;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class TimedAspect {

  @Around("@annotation(Timed)")
  public Object deProxy(ProceedingJoinPoint joinPoint) throws Throwable {
    var start = Instant.now();
    var result = joinPoint.proceed();
    Duration d = Duration.between(start, Instant.now());
    log.info(
      "Operation complete and took {} min {} sec {} ms",
      d.toMinutesPart(),
      d.toSecondsPart(),
      d.toMillisPart()
    );
    return result;
  }
}
