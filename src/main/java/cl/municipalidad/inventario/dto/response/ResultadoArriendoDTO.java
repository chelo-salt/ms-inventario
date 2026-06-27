package cl.municipalidad.inventario.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@Schema(description = "Respuesta unificada sobre los costos agregados del arriendo de implementos")
public class ResultadoArriendoDTO {

    @Schema(description = "Indica si se pudo reservar exitosamente el stock de todos los artículos", example = "true")
    private boolean exito;

    @Schema(description = "Costo total financiero extra calculado a partir de la suma de arriendos", example = "15000.00")
    private Double costoTotalExtra;

    @Schema(description = "Mensaje explicativo del estado del inventario", example = "Reserva de inventario exitosa.")
    private String mensaje;
}