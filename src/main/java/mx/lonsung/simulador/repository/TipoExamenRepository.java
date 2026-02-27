package mx.lonsung.simulador.repository;

import mx.lonsung.simulador.entity.TipoExamen;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TipoExamenRepository extends JpaRepository<TipoExamen, Long> {

    java.util.Optional<TipoExamen> findByDescripcionIgnoreCase(String descripcion);
}
