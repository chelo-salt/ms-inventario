package cl.municipalidad.inventario.controller;

import cl.municipalidad.inventario.entity.ItemInventario;
import cl.municipalidad.inventario.dto.request.SolicitudArriendoDTO;
import cl.municipalidad.inventario.dto.response.ResultadoArriendoDTO;
import cl.municipalidad.inventario.service.ItemInventarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;

@RestController
@RequestMapping("/api/v1/inventario")
@RequiredArgsConstructor
public class ItemInventarioController {

    private final ItemInventarioService service;

    @GetMapping("/recinto/{idRecinto}")
    public ResponseEntity<List<ItemInventario>> listarPorRecinto(@PathVariable Long idRecinto) {
        return ResponseEntity.ok(service.listarPorRecinto(idRecinto));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ItemInventario> crearItem(@RequestBody ItemInventario item) {
        return new ResponseEntity<>(service.guardarItem(item), HttpStatus.CREATED);
    }

    @PostMapping("/asignar")
    public ResponseEntity<ResultadoArriendoDTO> asignarInventario(@RequestBody List<SolicitudArriendoDTO> solicitudes) {
        ResultadoArriendoDTO resultado = service.alquilarItems(solicitudes);
        return ResponseEntity.ok(resultado);
    }

    @PostMapping("/liberar")
    public ResponseEntity<String> liberarInventario(@RequestBody List<SolicitudArriendoDTO> devoluciones) {
        service.liberarItems(devoluciones);
        return ResponseEntity.ok("Inventario liberado correctamente.");
    }
}