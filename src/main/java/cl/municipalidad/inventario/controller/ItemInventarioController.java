package cl.municipalidad.inventario.controller;

import cl.municipalidad.inventario.entity.ItemInventario;
import cl.municipalidad.inventario.dto.request.SolicitudArriendoDTO;
import cl.municipalidad.inventario.dto.response.ResultadoArriendoDTO;
import cl.municipalidad.inventario.service.ItemInventarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;

@RestController
@RequestMapping("/api/v1/inventario")
@RequiredArgsConstructor
@Tag(name = "Gestión de Inventario", description = "Endpoints corporativos para la administración, asignación y liberación de implementos deportivos")
public class ItemInventarioController {

    private final ItemInventarioService service;

    @Operation(summary = "Listar stock por recinto", description = "Obtiene todos los artículos disponibles asignados a una cancha específica.")
    @GetMapping("/recinto/{idRecinto}")
    public ResponseEntity<List<ItemInventario>> listarPorRecinto(@PathVariable Long idRecinto) {
        return ResponseEntity.ok(service.listarPorRecinto(idRecinto));
    }

    @Operation(summary = "Crear nuevo artículo de stock", description = "Permite dar de alta un accesorio en el sistema municipal (Exclusivo Administradores).")
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ItemInventario> crearItem(@RequestBody ItemInventario item) {
        return new ResponseEntity<>(service.guardarItem(item), HttpStatus.CREATED);
    }

    @Operation(summary = "Asignar inventario (Arriendos)", description = "Reserva el stock en bloque con mitigación de bloqueos simultáneos y calcula cobros adicionales.")
    @ApiResponse(responseCode = "200", description = "Reserva de inventario procesada")
    @ApiResponse(responseCode = "400", description = "Falta de stock crítico o artículo no encontrado")
    @PostMapping("/asignar")
    public ResponseEntity<ResultadoArriendoDTO> asignarInventario(@RequestBody List<SolicitudArriendoDTO> solicitudes) {
        ResultadoArriendoDTO resultado = service.alquilarItems(solicitudes);
        return ResponseEntity.ok(resultado);
    }

    @Operation(summary = "Liberar inventario (Devoluciones)", description = "Devuelve las cantidades al pool de stock una vez finalizado el bloque horario asignado.")
    @PostMapping("/liberar")
    public ResponseEntity<String> liberarInventario(@RequestBody List<SolicitudArriendoDTO> devoluciones) {
        service.liberarItems(devoluciones);
        return ResponseEntity.ok("Inventario liberado correctamente.");
    }
}