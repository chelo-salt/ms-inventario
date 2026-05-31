package cl.municipalidad.inventario.controller;

import cl.municipalidad.inventario.entity.ItemInventario;
import cl.municipalidad.inventario.dto.request.SolicitudArriendoDTO;
import cl.municipalidad.inventario.dto.response.ResultadoArriendoDTO;
import cl.municipalidad.inventario.exception.StockInsuficienteException;
import cl.municipalidad.inventario.service.ItemInventarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/inventario")
public class ItemInventarioController {

    @Autowired
    private ItemInventarioService service;

    // Obtener inventario disponible por recinto
    @GetMapping("/recinto/{idRecinto}")
    public ResponseEntity<List<ItemInventario>> listarPorRecinto(@PathVariable Long idRecinto) {
        return ResponseEntity.ok(service.listarPorRecinto(idRecinto));
    }

    // Agregar o actualizar implementos al inventario municipal
    @PostMapping
    public ResponseEntity<ItemInventario> crearItem(@RequestBody ItemInventario item) {
        return new ResponseEntity<>(service.guardarItem(item), HttpStatus.CREATED);
    }

    // Endpoint síncrono para que consuma ms-reservas
    @PostMapping("/asignar")
    public ResponseEntity<?> asignarInventario(@RequestBody List<SolicitudArriendoDTO> solicitudes) {
        try {
            ResultadoArriendoDTO resultado = service.alquilarItems(solicitudes);
            return new ResponseEntity<>(resultado, HttpStatus.OK);
        } catch (StockInsuficienteException e) {
            return new ResponseEntity<>(new ResultadoArriendoDTO(false, 0.0, e.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }

    // Liberar los artículos una vez finalizado el bloque horario
    @PostMapping("/liberar")
    public ResponseEntity<String> liberarInventario(@RequestBody List<SolicitudArriendoDTO> devoluciones) {
        service.liberarItems(devoluciones);
        return ResponseEntity.ok("Inventario liberado correctamente.");
    }
}