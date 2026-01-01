package com.example.shopfood.Exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
public class ControllerExceptionHandler {
    @ExceptionHandler({AppException.class})
    public ResponseEntity<AppException> catchExceptionGlobal(AppException exception, HttpServletRequest request) {
        exception.setPath(request.getRequestURI());
        return ResponseEntity.status(exception.getCode()).body(exception);
    }

    @ExceptionHandler({Exception.class})
    public ResponseEntity<AppException> catchExceptionGlobal2(Exception exception, HttpServletRequest request) {
        AppException appException = new AppException(exception);
        appException.setPath(request.getRequestURI());
        return ResponseEntity.status(appException.getCode()).body(appException);
    }

    @ExceptionHandler({BindException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<AppException> handleBindException(BindException e, HttpServletRequest request) {
        String errorMessage = "";
        if (e.getBindingResult().hasErrors()) {
            for(int i = 0; i < e.getBindingResult().getAllErrors().size(); ++i) {
                errorMessage = errorMessage + ((ObjectError)e.getBindingResult().getAllErrors().get(i)).getDefaultMessage();
                errorMessage = errorMessage + (i == e.getBindingResult().getAllErrors().size() - 1 ? "." : ", ");
            }
        }

        AppException appException = new AppException(errorMessage, 400, request.getRequestURI());
        return new ResponseEntity(appException, HttpStatus.valueOf(appException.getCode()));
    }
}
