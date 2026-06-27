package com.ikea.oms.order_service.exception;
import java.util.HashMap;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String,String>>handleValidationExceptions(MethodArgumentNotValidException ex){
        Map<String,String> errors =new HashMap<>();
        ex.getBindingResult()
                .getFieldErrors()
                .forEach(error->errors.put(error.getField(),
                        error.getDefaultMessage()));
        return ResponseEntity.badRequest().body(errors);
    }

    @ExceptionHandler(InventoryNotFoundException.class)
    public ResponseEntity<Map<String,String>>
    handleInventoryNotFoundException(
            InventoryNotFoundException ex){

        return ResponseEntity.badRequest()
                .body(Map.of("error",
                        ex.getMessage()));
    }


    @ExceptionHandler(InsufficientInventoryException.class)
    public ResponseEntity<Map<String,String>>
    handleInsufficientInventoryException(
            InsufficientInventoryException ex){

        return ResponseEntity.badRequest()
                .body(Map.of("error",
                        ex.getMessage()));
    }
}
