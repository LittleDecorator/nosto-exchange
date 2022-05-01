package com.nosto.exchange.handler;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.UNSUPPORTED_MEDIA_TYPE;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.nosto.exchange.exception.ExchangeClientException;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.validation.ConstraintViolationException;
import javax.validation.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.helpers.MessageFormatter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.util.MimeType;
import org.springframework.validation.BindException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@Slf4j
@ControllerAdvice
@SuppressWarnings("NullableProblems")
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

  @Override
  protected ResponseEntity<Object> handleMissingServletRequestPart(
    MissingServletRequestPartException ex,
    HttpHeaders headers,
    HttpStatus status,
    WebRequest request
  ) {
    ApiError apiError = new ApiError(BAD_REQUEST);
    apiError.setMessage(ex.getRequestPartName() + " parameter is missing");
    apiError.setDetail(ex.getMessage());
    return buildResponseEntity(apiError);
  }

  @Override
  protected ResponseEntity<Object> handleHttpMessageNotReadable(
    HttpMessageNotReadableException ex,
    HttpHeaders headers,
    HttpStatus status,
    WebRequest request) {
    ApiError apiError = new ApiError(BAD_REQUEST);
    if (ex.getCause() instanceof JsonMappingException) {
      apiError.setMessage(ex.getMostSpecificCause().getMessage());
    } else {
      ServletWebRequest servletWebRequest = (ServletWebRequest) request;
      apiError.setMessage("Malformed JSON request");
      apiError.setDetail(
        MessageFormatter.format(
          "httpMethod={} to request={}",
          servletWebRequest.getHttpMethod(),
          servletWebRequest.getRequest().getServletPath()
        ).getMessage());
    }
    return buildResponseEntity(apiError);
  }

  @Override
  protected ResponseEntity<Object> handleHttpMessageNotWritable(
    HttpMessageNotWritableException ex,
    HttpHeaders headers,
    HttpStatus status,
    WebRequest request) {
    ApiError apiError = new ApiError(INTERNAL_SERVER_ERROR);
    apiError.setMessage("Error writing JSON output");
    apiError.setDetail(ex.getMessage());
    return buildResponseEntity(apiError);
  }

  @Override
  protected ResponseEntity<Object> handleNoHandlerFoundException(
    NoHandlerFoundException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
    ApiError apiError = new ApiError(NOT_FOUND);
    apiError.setMessage(
      MessageFormatter.format(
        "Could not find the {} method for URL {}",
        ex.getHttpMethod(),
        ex.getRequestURL()
      ).getMessage());
    apiError.setDetail(ex.getMessage());
    return buildResponseEntity(apiError);
  }

  @Override
  protected ResponseEntity<Object> handleMissingServletRequestParameter(
    MissingServletRequestParameterException ex,
    HttpHeaders headers,
    HttpStatus status,
    WebRequest request) {
    ApiError apiError = new ApiError(BAD_REQUEST);
    apiError.setMessage(ex.getParameterName() + " parameter is missing");
    apiError.setDetail(ex.getMessage());
    return buildResponseEntity(apiError);
  }

  @Override
  protected ResponseEntity<Object> handleHttpMediaTypeNotSupported(
    HttpMediaTypeNotSupportedException ex,
    HttpHeaders headers,
    HttpStatus status,
    WebRequest request) {

    ApiError apiError = new ApiError(UNSUPPORTED_MEDIA_TYPE);
    apiError.setMessage(
      ex.getContentType() + " media type is not supported. Supported media types are: " + ex
        .getSupportedMediaTypes()
        .stream()
        .map(MimeType::toString)
        .collect(
          Collectors.joining(",")
        ));
    apiError.setDetail(ex.getMessage());
    return buildResponseEntity(apiError);
  }

  @Override
  protected ResponseEntity<Object> handleMethodArgumentNotValid(
    MethodArgumentNotValidException ex,
    HttpHeaders headers,
    HttpStatus status,
    WebRequest request) {
    ApiError apiError = new ApiError(BAD_REQUEST);
    apiError.setMessage("Validation error");
    apiError.addValidationErrors(ex.getBindingResult().getFieldErrors());
    apiError.addValidationError(ex.getBindingResult().getGlobalErrors());
    return buildResponseEntity(apiError);
  }

  @ExceptionHandler(ConstraintViolationException.class)
  protected ResponseEntity<Object> handleConstraintViolation(ConstraintViolationException ex) {
    ApiError apiError = new ApiError(BAD_REQUEST);
    apiError.setMessage("Validation error");
    apiError.addValidationErrors(ex.getConstraintViolations());
    return buildResponseEntity(apiError);
  }

  @ExceptionHandler(MethodArgumentTypeMismatchException.class)
  protected ResponseEntity<Object> handleMethodArgumentTypeMismatch(
    MethodArgumentTypeMismatchException ex) {
    ApiError apiError = new ApiError(BAD_REQUEST);
    apiError.setMessage(
      MessageFormatter.arrayFormat(
        "Invalid parameter value is passed to the request: {}. "
          + "The parameter {} of value {} could not be converted to {}",
        new Object[]{
          ex.getValue(),
          ex.getName(),
          ex.getValue(),
          Optional.ofNullable(ex.getRequiredType()).map(Class::getSimpleName).orElse("desired type")
        }
      ).getMessage());
    apiError.setDetail(ex.getMessage());
    return buildResponseEntity(apiError);
  }

  @Override
  protected ResponseEntity<Object> handleBindException(BindException ex, HttpHeaders headers,
    HttpStatus status,
    WebRequest request) {
    ApiError apiError = new ApiError(BAD_REQUEST);
    apiError.setMessage("Binding exception");
    apiError.addValidationErrors(ex.getBindingResult().getFieldErrors());
    apiError.addValidationError(ex.getBindingResult().getGlobalErrors());
    return buildResponseEntity(apiError);
  }

  @ExceptionHandler({ValidationException.class})
  public ResponseEntity<Object> handleValidationException(ValidationException ex) {
    return buildResponseEntity(BAD_REQUEST, ex);
  }

  @ExceptionHandler({ExchangeClientException.class})
  public ResponseEntity<Object> handleCommonFeignException(ExchangeClientException ex) {
    var status = HttpStatus.resolve(ex.getStatus());
    return buildResponseEntity(status, ex);
  }

  @ExceptionHandler({Exception.class})
  public ResponseEntity<Object> handleOtherExceptions(Exception ex) {
    log.error("Handle exception", ex);
    return buildResponseEntity(INTERNAL_SERVER_ERROR, ex);
  }

  private ResponseEntity<Object> buildResponseEntity(HttpStatus status, Exception ex) {
    var cause = Optional.ofNullable(ex.getCause()).map(Throwable::getMessage).orElse(null);
    var apiError = new ApiError(status)
      .setMessage(ex.getMessage())
      .setDetail(cause);
    return buildResponseEntity(apiError);
  }

  private ResponseEntity<Object> buildResponseEntity(ApiError apiError) {
    return new ResponseEntity<>(apiError, apiError.getStatus());
  }
}
