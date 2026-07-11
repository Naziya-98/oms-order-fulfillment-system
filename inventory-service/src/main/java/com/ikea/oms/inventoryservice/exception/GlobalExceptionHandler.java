package com.ikea.oms.inventoryservice.exception;
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
    public ResponseEntity<String> handleInventoryNotFound(
            InventoryNotFoundException ex) {

        return ResponseEntity
                .status(404)
                .body(ex.getMessage());
    }

    @ExceptionHandler(com.ikea.oms.inventoryservice.exception.ReservationNotFoundException.class)
    public ResponseEntity<String> handleReservationNotFound(
            com.ikea.oms.inventoryservice.exception.ReservationNotFoundException ex) {

        return ResponseEntity
                .status(404)
                .body(ex.getMessage());
    }

    @ExceptionHandler(com.ikea.oms.inventoryservice.exception.InsufficientInventoryException.class)
    public ResponseEntity<String> handleInsufficientInventory(
            com.ikea.oms.inventoryservice.exception.InsufficientInventoryException ex) {

        return ResponseEntity
                .status(409)
                .body(ex.getMessage());
    }
}
