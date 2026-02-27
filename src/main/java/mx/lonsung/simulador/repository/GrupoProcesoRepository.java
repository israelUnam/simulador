package mx.lonsung.simulador.repository;

import mx.lonsung.simulador.entity.GrupoProceso;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GrupoProcesoRepository extends JpaRepository<GrupoProceso, Long> {

    java.util.Optional<GrupoProceso> findByDescripcionIgnoreCase(String descripcion);
}
