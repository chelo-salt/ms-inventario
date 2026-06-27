package cl.municipalidad.inventario.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "items_inventario")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Representación física de un artículo de inventario municipal")
public class ItemInventario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "ID autogenerado del ítem", example = "1")
    private Long idItem;

    @Column(nullable = false)
    @Schema(description = "Nombre descriptivo del implemento", example = "Malla de Arquería Oficial Profesional")
    private String nombre;

    @Column(name = "id_recinto_foraneo", nullable = false)
    @Schema(description = "ID de la cancha o complejo deportivo al que pertenece el stock", example = "12")
    private Long idRecintoForaneo;

    @Column(name = "stock_total", nullable = false)
    @Schema(description = "Cantidad absoluta registrada en los almacenes municipales", example = "10")
    private Integer stockTotal;

    @Column(name = "stock_disponible", nullable = false)
    @Schema(description = "Cantidad operativa disponible inmediata para arriendos en tiempo real", example = "7")
    private Integer stockDisponible;

    @Column(name = "precio_arriendo", nullable = false)
    @Schema(description = "Tarifa base unitaria por el uso del accesorio", example = "5000.0")
    private Double precioArriendo; 
}