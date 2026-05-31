package cl.municipalidad.inventario.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ResultadoArriendoDTO {
    private boolean exito;
    private Double costoTotalExtra;
    private String mensaje;
}