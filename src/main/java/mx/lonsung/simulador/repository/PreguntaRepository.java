package mx.lonsung.simulador.repository;

import mx.lonsung.simulador.entity.Pregunta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PreguntaRepository extends JpaRepository<Pregunta, Integer> {

    long countByTipoExamen_IdExamen(Long idExamen);

    java.util.List<Pregunta> findByTipoExamen_IdExamen(Long idExamen);
}

