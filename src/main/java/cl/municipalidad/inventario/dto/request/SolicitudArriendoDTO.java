package cl.municipalidad.inventario.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Estructura de petición para solicitar o devolver stock de un artículo")
public class SolicitudArriendoDTO {

    @Schema(description = "ID único del ítem en la base de datos de inventario", example = "5")
    private Long idItem;

    @Schema(description = "Cantidad de artículos a reservar o liberar", example = "3")
    private Integer cantidad;
}