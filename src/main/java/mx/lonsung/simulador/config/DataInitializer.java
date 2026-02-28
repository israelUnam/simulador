package mx.lonsung.simulador.config;

import mx.lonsung.simulador.entity.Permiso;
import mx.lonsung.simulador.entity.Rol;
import mx.lonsung.simulador.entity.TipoExamen;
import mx.lonsung.simulador.repository.PermisoRepository;
import mx.lonsung.simulador.repository.RolRepository;
import mx.lonsung.simulador.repository.TipoExamenRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Crea los roles, permisos y tipos de examen iniciales si no existen.
 */
@Configuration
public class DataInitializer {

    public static final String DESCRIPCION_TODAS_LAS_AREAS = "200 preguntas todas las áreas";

    @Bean
    CommandLineRunner initTipoExamenTodasAreas(TipoExamenRepository tipoExamenRepository) {
        return args -> {
            if (tipoExamenRepository.findByDescripcionIgnoreCase(DESCRIPCION_TODAS_LAS_AREAS).isEmpty()) {
                TipoExamen tipo = new TipoExamen();
                tipo.setDescripcion(DESCRIPCION_TODAS_LAS_AREAS);
                tipoExamenRepository.save(tipo);
            }
        };
    }

    @Bean
    CommandLineRunner initRolesAndPermisos(PermisoRepository permisoRepository, RolRepository rolRepository) {
        return args -> {
            if (permisoRepository.count() > 0) {
                return;
            }

            Permiso accesoExamen = permisoRepository.save(new Permiso(null, "ACCESO_EXAMEN", "Acceso a la página de examen"));
            Permiso verBitacora = permisoRepository.save(new Permiso(null, "VER_BITACORA", "Consulta de bitácora de accesos"));
            Permiso adminUsuarios = permisoRepository.save(new Permiso(null, "ADMIN_USUARIOS", "Administración de usuarios y permisos"));
            Permiso cargaExcel = permisoRepository.save(new Permiso(null, "CARGA_EXCEL", "Importación de preguntas desde Excel"));

            Rol usuario = new Rol();
            usuario.setNombre("USUARIO");
            usuario.setPermisos(List.of(accesoExamen));
            rolRepository.save(usuario);

            Rol administrador = new Rol();
            administrador.setNombre("ADMINISTRADOR");
            administrador.setPermisos(List.of(accesoExamen, verBitacora, adminUsuarios, cargaExcel));
            rolRepository.save(administrador);
        };
    }
}
