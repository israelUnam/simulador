package mx.lonsung.simulador.repository;

import mx.lonsung.simulador.entity.Bitacora;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BitacoraRepository extends JpaRepository<Bitacora, Long> {

    List<Bitacora> findAllByOrderByFechaHoraDesc();
}
