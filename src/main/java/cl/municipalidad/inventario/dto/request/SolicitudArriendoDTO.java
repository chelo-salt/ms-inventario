package cl.municipalidad.inventario.dto.request;

import lombok.Data;

@Data
public class SolicitudArriendoDTO {
    private Long idItem;
    private Integer cantidad;
}