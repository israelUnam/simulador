package mx.lonsung.simulador.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import mx.lonsung.simulador.repository.UsuarioPermisoRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;

/**
 * Filtro que asegura que solo usuarios presentes en usuario_permiso puedan acceder a /examen y /importar-preguntas.
 */
public class UsuarioPermisoFilter extends OncePerRequestFilter {

    private static final java.util.Set<String> RUTAS_PROTEGIDAS = java.util.Set.of("/examen", "/importar-preguntas", "/agregar-examen", "/crear-examen", "/examen-en-progreso", "/examen-en-progreso/indice", "/examen-en-progreso/responder", "/examen-en-progreso/marcar-pendiente", "/examen-en-progreso/finalizar");

    private final UsuarioPermisoRepository usuarioPermisoRepository;

    public UsuarioPermisoFilter(UsuarioPermisoRepository usuarioPermisoRepository) {
        this.usuarioPermisoRepository = usuarioPermisoRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String path = request.getServletPath();
        boolean rutaProtegida = RUTAS_PROTEGIDAS.contains(path) || (path != null && path.startsWith("/examen/resultados/"));
        if (!rutaProtegida) {
            filterChain.doFilter(request, response);
            return;
        }

        Authentication auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal() == null) {
            filterChain.doFilter(request, response);
            return;
        }

        String email = null;
        if (auth.getPrincipal() instanceof OAuth2User oauth2User) {
            email = oauth2User.getAttribute("email");
            if (email == null) {
                email = oauth2User.getAttribute("name");
            }
            if (email == null) {
                email = oauth2User.getName();
            }
        }

        boolean tienePermiso = email != null && usuarioPermisoRepository.findByEmailIgnoreCase(email).isPresent();
        if (!tienePermiso) {
            response.sendRedirect(request.getContextPath() + "/acceso-denegado");
            return;
        }

        filterChain.doFilter(request, response);
    }
}
