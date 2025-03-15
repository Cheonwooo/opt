package com.opt.ssafy.optback.domain.license.api;

import com.opt.ssafy.optback.domain.license.exception.BusinessLicenseMessagingException;
import com.opt.ssafy.optback.global.dto.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(basePackageClasses = BusinessLicenseController.class)
public class BusinessLicenseExceptionHandler {

    @ExceptionHandler(BusinessLicenseMessagingException.class)
    public ResponseEntity<ErrorResponse> handleBusinessLicenseMessagingExceptionHandler(
            BusinessLicenseMessagingException e) {
        return new ResponseEntity<>(ErrorResponse.builder().message(e.getMessage()).build(), HttpStatus.BAD_REQUEST);
    }

}
