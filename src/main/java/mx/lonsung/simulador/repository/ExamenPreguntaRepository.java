package mx.lonsung.simulador.repository;

import mx.lonsung.simulador.entity.ExamenPregunta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExamenPreguntaRepository extends JpaRepository<ExamenPregunta, Long> {

    @Query("SELECT ep FROM ExamenPregunta ep JOIN FETCH ep.pregunta WHERE ep.examen.id = :idExamen ORDER BY ep.id")
    List<ExamenPregunta> findByExamenIdWithPregunta(@Param("idExamen") Long idExamen);

    long countByExamen_IdAndAciertoTrue(Long examenId);

    void deleteByExamen_Id(Long examenId);
}
