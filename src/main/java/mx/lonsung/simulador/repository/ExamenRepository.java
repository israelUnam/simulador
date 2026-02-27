package mx.lonsung.simulador.repository;

import mx.lonsung.simulador.entity.Examen;
import mx.lonsung.simulador.entity.UsuarioPermiso;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExamenRepository extends JpaRepository<Examen, Long> {

    @Query("SELECT e FROM Examen e JOIN FETCH e.tipoExamen WHERE e.usuarioPermiso = :up ORDER BY e.fechaHoraInicio DESC")
    List<Examen> findByUsuarioPermisoWithTipoExamen(@Param("up") UsuarioPermiso up);
}
