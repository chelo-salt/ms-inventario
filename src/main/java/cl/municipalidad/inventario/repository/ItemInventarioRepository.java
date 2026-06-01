package cl.municipalidad.inventario.repository;

import cl.municipalidad.inventario.entity.ItemInventario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;

@Repository
public interface ItemInventarioRepository extends JpaRepository<ItemInventario, Long> {
    
    List<ItemInventario> findByIdRecintoForaneo(Long idRecintoForaneo);

    // Bloqueo para evitar arriendos simultaneos
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT i FROM ItemInventario i WHERE i.idItem = :id")
    Optional<ItemInventario> findByIdWithLock(@Param("id") Long id);
}