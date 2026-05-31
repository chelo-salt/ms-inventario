package cl.municipalidad.inventario.service;

import cl.municipalidad.inventario.entity.ItemInventario;
import cl.municipalidad.inventario.dto.request.SolicitudArriendoDTO;
import cl.municipalidad.inventario.dto.response.ResultadoArriendoDTO;

import java.util.List;

public interface ItemInventarioService {
    List<ItemInventario> listarPorRecinto(Long idRecinto);
    ItemInventario guardarItem(ItemInventario item);
    ResultadoArriendoDTO alquilarItems(List<SolicitudArriendoDTO> solicitudes);
    void liberarItems(List<SolicitudArriendoDTO> devoluciones);
}