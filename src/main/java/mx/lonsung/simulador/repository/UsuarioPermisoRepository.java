package mx.lonsung.simulador.repository;

import mx.lonsung.simulador.entity.UsuarioPermiso;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UsuarioPermisoRepository extends JpaRepository<UsuarioPermiso, Long> {

    Optional<UsuarioPermiso> findByEmailIgnoreCase(String email);

    /** Carga usuario con rol y permisos del rol para evitar LazyInitializationException. */
    @Query("SELECT DISTINCT u FROM UsuarioPermiso u JOIN FETCH u.rol r LEFT JOIN FETCH r.permisos WHERE LOWER(u.email) = LOWER(:email)")
    Optional<UsuarioPermiso> findByEmailIgnoreCaseWithRolAndPermisos(@Param("email") String email);
}
