package cl.municipalidad.inventario.service;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import cl.municipalidad.inventario.dto.request.SolicitudArriendoDTO;
import cl.municipalidad.inventario.dto.response.ResultadoArriendoDTO;
import cl.municipalidad.inventario.entity.ItemInventario;
import cl.municipalidad.inventario.exception.StockInsuficienteException;
import cl.municipalidad.inventario.repository.ItemInventarioRepository;

@ExtendWith(MockitoExtension.class)
class ItemInventarioServiceImplTest {

    @Mock
    private ItemInventarioRepository repository;

    @InjectMocks
    private ItemInventarioServiceImpl service;

    @Captor
    private ArgumentCaptor<ItemInventario> itemCaptor;

    private ItemInventario itemPelotas;
    private ItemInventario itemChalecos;
    private SolicitudArriendoDTO solicitudPelotas;
    private SolicitudArriendoDTO solicitudChalecos;

    @BeforeEach
    void setUp() {
        itemPelotas = new ItemInventario(1L, "Pelota de Fútbol FIFA", 10L, 20, 15, 3000.0);
        itemChalecos = new ItemInventario(2L, "Chalecos Distintivos", 10L, 50, 5, 1000.0);

        solicitudPelotas = new SolicitudArriendoDTO();
        solicitudPelotas.setIdItem(1L);
        solicitudPelotas.setCantidad(3);

        solicitudChalecos = new SolicitudArriendoDTO();
        solicitudChalecos.setIdItem(2L);
        solicitudChalecos.setCantidad(10);
    }

    // TEST 1: Consulta básica por Recinto Foráneo
    @Test
    void listarPorRecinto_DebeRetornarListaDeItems() {
        when(repository.findByIdRecintoForaneo(10L)).thenReturn(Arrays.asList(itemPelotas, itemChalecos));

        List<ItemInventario> resultado = service.listarPorRecinto(10L);

        assertThat(resultado).hasSize(2);
        assertThat(resultado.get(0).getNombre()).isEqualTo("Pelota de Fútbol FIFA");
        verify(repository, times(1)).findByIdRecintoForaneo(10L);
    }

    // TEST 2: Flujo Exitoso de Arriendo (Verifica Descuento de Stock y Cálculo Financiero)
    @Test
    void alquilarItems_CuandoHayStockSuficiente_DebeDescontarYCalcularPrecioTotal() {

        when(repository.findByIdWithLock(1L)).thenReturn(Optional.of(itemPelotas));
        when(repository.save(any(ItemInventario.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ResultadoArriendoDTO resultado = service.alquilarItems(Collections.singletonList(solicitudPelotas));

        assertThat(resultado.isExito()).isTrue();
        assertThat(resultado.getMensaje()).isEqualTo("Reserva de inventario exitosa.");
        assertThat(resultado.getCostoTotalExtra()).isEqualTo(9000.0); // 3 pelotas * $3000

        verify(repository).save(itemCaptor.capture());
        ItemInventario itemGuardado = itemCaptor.getValue();
        assertThat(itemGuardado.getStockDisponible()).isEqualTo(12); 
    }

    // TEST 3: Manejo de Excepciones por Quiebre de Stock Crítico (Transaccional)
    @Test
    void alquilarItems_CuandoStockEsInsuficiente_DebeLanzarStockInsuficienteException() {
        when(repository.findByIdWithLock(2L)).thenReturn(Optional.of(itemChalecos));

        assertThatThrownBy(() -> service.alquilarItems(Collections.singletonList(solicitudChalecos)))
                .isInstanceOf(StockInsuficienteException.class)
                .hasMessageContaining("Stock insuficiente para: Chalecos Distintivos. Disponible: 5, Solicitado: 10");

        
        verify(repository, never()).save(any());
    }

    // TEST 4: Manejo de Excepciones por Artículo Inexistente en Catálogo
    @Test
    void alquilarItems_CuandoItemNoExiste_DebeLanzarException() {
        when(repository.findByIdWithLock(99L)).thenReturn(Optional.empty());

        SolicitudArriendoDTO solicitudInvalida = new SolicitudArriendoDTO();
        solicitudInvalida.setIdItem(99L);
        solicitudInvalida.setCantidad(1);

        assertThatThrownBy(() -> service.alquilarItems(Collections.singletonList(solicitudInvalida)))
                .isInstanceOf(StockInsuficienteException.class)
                .hasMessageContaining("Artículo no encontrado con ID: 99");

        verify(repository, never()).save(any());
    }

    // TEST 5: Devolución/Liberación Exitosa de Implementos Deportivos
    @Test
    void liberarItems_CuandoDevolucionEsValida_DebeRestituirStockDisponible() {
        when(repository.findByIdWithLock(1L)).thenReturn(Optional.of(itemPelotas));

        SolicitudArriendoDTO devolucion = new SolicitudArriendoDTO();
        devolucion.setIdItem(1L);
        devolucion.setCantidad(4);

        service.liberarItems(Collections.singletonList(devolucion));

        verify(repository).save(itemCaptor.capture());
        ItemInventario itemRestituido = itemCaptor.getValue();
        
        assertThat(itemRestituido.getStockDisponible()).isEqualTo(19); 
    }
}