package mx.lonsung.simulador.config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import mx.lonsung.simulador.repository.UsuarioPermisoRepository;
import mx.lonsung.simulador.service.BitacoraService;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class BitacoraAuthenticationSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {

    private final BitacoraService bitacoraService;
    private final UsuarioPermisoRepository usuarioPermisoRepository;

    public BitacoraAuthenticationSuccessHandler(BitacoraService bitacoraService,
                                                 UsuarioPermisoRepository usuarioPermisoRepository) {
        this.bitacoraService = bitacoraService;
        this.usuarioPermisoRepository = usuarioPermisoRepository;
        setDefaultTargetUrl("/examen");
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                          Authentication authentication) throws IOException, ServletException {
        String email = null;
        if (authentication.getPrincipal() instanceof OAuth2User oauth2User) {
            email = oauth2User.getAttribute("email");
            if (email == null) {
                email = oauth2User.getAttribute("name");
            }
            if (email == null) {
                email = oauth2User.getName();
            }
        }
        String usuario = email != null ? email : "anonimo";

        String ip = request.getRemoteAddr();
        if (request.getHeader("X-Forwarded-For") != null) {
            ip = request.getHeader("X-Forwarded-For").split(",")[0].trim();
        }
        String userAgent = request.getHeader("User-Agent");

        boolean tienePermiso = email != null && usuarioPermisoRepository.findByEmailIgnoreCase(email).isPresent();

        if (!tienePermiso) {
            bitacoraService.registrarAcceso(usuario, ip, "ACCESO_FALLIDO", userAgent);
            getRedirectStrategy().sendRedirect(request, response, "/acceso-denegado");
            return;
        }

        bitacoraService.registrarAcceso(usuario, ip, "LOGIN", userAgent);
        super.onAuthenticationSuccess(request, response, authentication);
    }
}
