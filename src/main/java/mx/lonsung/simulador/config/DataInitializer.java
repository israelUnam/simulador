package mx.lonsung.simulador.config;

import mx.lonsung.simulador.entity.Permiso;
import mx.lonsung.simulador.entity.Rol;
import mx.lonsung.simulador.repository.PermisoRepository;
import mx.lonsung.simulador.repository.RolRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Crea los roles y permisos iniciales si no existen.
 */
@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initRolesAndPermisos(PermisoRepository permisoRepository, RolRepository rolRepository) {
        return args -> {
            if (permisoRepository.count() > 0) {
                return;
            }

            Permiso accesoExamen = permisoRepository.save(new Permiso(null, "ACCESO_EXAMEN", "Acceso a la página de examen"));
            Permiso verBitacora = permisoRepository.save(new Permiso(null, "VER_BITACORA", "Consulta de bitácora de accesos"));
            Permiso adminUsuarios = permisoRepository.save(new Permiso(null, "ADMIN_USUARIOS", "Administración de usuarios y permisos"));

            Rol usuario = new Rol();
            usuario.setNombre("USUARIO");
            usuario.setPermisos(List.of(accesoExamen));
            rolRepository.save(usuario);

            Rol administrador = new Rol();
            administrador.setNombre("ADMINISTRADOR");
            administrador.setPermisos(List.of(accesoExamen, verBitacora, adminUsuarios));
            rolRepository.save(administrador);
        };
    }
}
