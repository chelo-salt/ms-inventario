package cl.municipalidad.inventario.exception;

import cl.municipalidad.inventario.dto.response.ResultadoArriendoDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(StockInsuficienteException.class)
    public ResponseEntity<ResultadoArriendoDTO> handleStockInsuficiente(StockInsuficienteException ex) {
        ResultadoArriendoDTO errorResponse = new ResultadoArriendoDTO(false, 0.0, ex.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }
}