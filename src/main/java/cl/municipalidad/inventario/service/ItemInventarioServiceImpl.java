package cl.municipalidad.inventario.service;

import cl.municipalidad.inventario.entity.ItemInventario;
import cl.municipalidad.inventario.dto.request.SolicitudArriendoDTO;
import cl.municipalidad.inventario.dto.response.ResultadoArriendoDTO;
import cl.municipalidad.inventario.exception.StockInsuficienteException;
import cl.municipalidad.inventario.repository.ItemInventarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ItemInventarioServiceImpl implements ItemInventarioService {

    private final ItemInventarioRepository repository;

    @Override
    public List<ItemInventario> listarPorRecinto(Long idRecinto) {
        return repository.findByIdRecintoForaneo(idRecinto);
    }

    @Override
    public ItemInventario guardarItem(ItemInventario item) {
        return repository.save(item);
    }

    @Override
    @Transactional // En caso de fallo de carrito, se hace Rollback de todo automáticamente
    public ResultadoArriendoDTO alquilarItems(List<SolicitudArriendoDTO> solicitudes) {
        Double costoTotalExtra = 0.0;

        for (SolicitudArriendoDTO solicitud : solicitudes) {
            ItemInventario item = repository.findByIdWithLock(solicitud.getIdItem())
                    .orElseThrow(() -> new StockInsuficienteException("Artículo no encontrado con ID: " + solicitud.getIdItem()));

            if (item.getStockDisponible() < solicitud.getCantidad()) {
                throw new StockInsuficienteException("Stock insuficiente para: " + item.getNombre() + 
                        ". Disponible: " + item.getStockDisponible() + ", Solicitado: " + solicitud.getCantidad());
            }

            item.setStockDisponible(item.getStockDisponible() - solicitud.getCantidad());
            costoTotalExtra += item.getPrecioArriendo() * solicitud.getCantidad();
            
            repository.save(item);
        }

        return new ResultadoArriendoDTO(true, costoTotalExtra, "Reserva de inventario exitosa.");
    }

    @Override
    @Transactional
    public void liberarItems(List<SolicitudArriendoDTO> devoluciones) {
        for (SolicitudArriendoDTO devolucion : devoluciones) {
            repository.findByIdWithLock(devolucion.getIdItem()).ifPresent(item -> {
                int nuevoStock = item.getStockDisponible() + devolucion.getCantidad();
                if (nuevoStock <= item.getStockTotal()) {
                    item.setStockDisponible(nuevoStock);
                    repository.save(item);
                }
            });
        }
    }
}