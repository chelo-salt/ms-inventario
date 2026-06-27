package cl.municipalidad.inventario.service;

import cl.municipalidad.inventario.entity.ItemInventario;
import cl.municipalidad.inventario.dto.request.SolicitudArriendoDTO;
import cl.municipalidad.inventario.dto.response.ResultadoArriendoDTO;
import cl.municipalidad.inventario.exception.StockInsuficienteException;
import cl.municipalidad.inventario.repository.ItemInventarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ItemInventarioServiceImpl implements ItemInventarioService {

    private final ItemInventarioRepository repository;

    @Override
    public List<ItemInventario> listarPorRecinto(Long idRecinto) {
        log.info("Consultando inventario asociado al recinto deportivo ID: {}", idRecinto);
        return repository.findByIdRecintoForaneo(idRecinto);
    }

    @Override
    public ItemInventario guardarItem(ItemInventario item) {
        log.info("Registrando nuevo implemento municipal en los catálogos: {}", item.getNombre());
        return repository.save(item);
    }

    @Override
    @Transactional
    public ResultadoArriendoDTO alquilarItems(List<SolicitudArriendoDTO> solicitudes) {
        log.info("Iniciando solicitud transaccional de arriendo en lote. Cantidad de artículos a evaluar: {}", solicitudes.size());
        Double costoTotalExtra = 0.0;

        for (SolicitudArriendoDTO solicitud : solicitudes) {
            log.debug("Aplicando Pessimistic Lock (WRITE) para asegurar consistencia en Item ID: {}", solicitud.getIdItem());
            
            ItemInventario item = repository.findByIdWithLock(solicitud.getIdItem())
                    .orElseThrow(() -> {
                        log.error("Fallo crítico de arriendo: Artículo no encontrado con ID: {}", solicitud.getIdItem());
                        return new StockInsuficienteException("Artículo no encontrado con ID: " + solicitud.getIdItem());
                    });

            if (item.getStockDisponible() < solicitud.getCantidad()) {
                log.warn("Quiebre de stock detectado para '{}'. Disponibles: {}, Solicitados: {}", 
                        item.getNombre(), item.getStockDisponible(), solicitud.getCantidad());
                throw new StockInsuficienteException("Stock insuficiente para: " + item.getNombre() + 
                        ". Disponible: " + item.getStockDisponible() + ", Solicitado: " + solicitud.getCantidad());
            }

            int stockAnterior = item.getStockDisponible();
            item.setStockDisponible(stockAnterior - solicitud.getCantidad());
            costoTotalExtra += item.getPrecioArriendo() * solicitud.getCantidad();
            
            repository.save(item);
            log.info("Descuento de stock exitoso para '{}' (ID {}). Stock anterior: {} -> Nuevo Stock: {}", 
                    item.getNombre(), item.getIdItem(), stockAnterior, item.getStockDisponible());
        }

        log.info("Transacción de arriendo concluida con éxito. Costo total agregado: ${}", costoTotalExtra);
        return new ResultadoArriendoDTO(true, costoTotalExtra, "Reserva de inventario exitosa.");
    }

    @Override
    @Transactional
    public void liberarItems(List<SolicitudArriendoDTO> devoluciones) {
        log.info("Procesando lote de liberación/devolución de stock. Cantidad de ítems: {}", devoluciones.size());
        
        for (SolicitudArriendoDTO devolucion : devoluciones) {
            repository.findByIdWithLock(devolucion.getIdItem()).ifPresentOrElse(item -> {
                int nuevoStock = item.getStockDisponible() + devolucion.getCantidad();
                
                if (nuevoStock <= item.getStockTotal()) {
                    int stockAnterior = item.getStockDisponible();
                    item.setStockDisponible(nuevoStock);
                    repository.save(item);
                    log.info("Stock incrementado por devolución para '{}' (ID {}). {} -> {}", 
                            item.getNombre(), item.getIdItem(), stockAnterior, item.getStockDisponible());
                } else {
                    log.warn("Intento anómalo de devolución para ID {}: El stock final calculado ({}) superaría el stock total permitido ({})", 
                            item.getIdItem(), nuevoStock, item.getStockTotal());
                }
            }, () -> log.error("No se pudo restituir stock. El artículo con ID {} no existe en los registros.", devolucion.getIdItem()));
        }
    }
}