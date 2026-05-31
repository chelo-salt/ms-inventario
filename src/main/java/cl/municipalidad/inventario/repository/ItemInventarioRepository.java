package cl.municipalidad.inventario.repository;

import cl.municipalidad.inventario.entity.ItemInventario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ItemInventarioRepository extends JpaRepository<ItemInventario, Long> {
    List<ItemInventario> findByIdRecintoForaneo(Long idRecintoForaneo);
}
