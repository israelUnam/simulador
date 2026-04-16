package mx.lonsung.simulador.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import mx.lonsung.simulador.entity.Examen;
import mx.lonsung.simulador.entity.ExamenPregunta;
import mx.lonsung.simulador.entity.Pregunta;
import mx.lonsung.simulador.entity.TipoExamen;
import mx.lonsung.simulador.entity.UsuarioPermiso;
import mx.lonsung.simulador.repository.ExamenPreguntaRepository;
import mx.lonsung.simulador.repository.ExamenRepository;
import mx.lonsung.simulador.repository.PreguntaRepository;
import mx.lonsung.simulador.repository.TipoExamenRepository;
import mx.lonsung.simulador.repository.UsuarioPermisoRepository;
import mx.lonsung.simulador.config.DataInitializer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class HomeController {

    private final UsuarioPermisoRepository usuarioPermisoRepository;
    private final TipoExamenRepository tipoExamenRepository;
    private final PreguntaRepository preguntaRepository;
    private final ExamenRepository examenRepository;
    private final ExamenPreguntaRepository examenPreguntaRepository;

    public HomeController(UsuarioPermisoRepository usuarioPermisoRepository,
                         TipoExamenRepository tipoExamenRepository,
                         PreguntaRepository preguntaRepository,
                         ExamenRepository examenRepository,
                         ExamenPreguntaRepository examenPreguntaRepository) {
        this.usuarioPermisoRepository = usuarioPermisoRepository;
        this.tipoExamenRepository = tipoExamenRepository;
        this.preguntaRepository = preguntaRepository;
        this.examenRepository = examenRepository;
        this.examenPreguntaRepository = examenPreguntaRepository;
    }

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("mensaje", "Hola mundo");
        return "index";
    }

    @GetMapping("/examen")
    public String examen(Model model, @AuthenticationPrincipal OAuth2User principal) {
        boolean puedeCargarExcel = false;
        boolean puedeAdministrar = false;

        if (principal != null) {
            String email = principal.getAttribute("email");
            if (email == null) {
                email = principal.getAttribute("name");
            }
            if (email == null) {
                email = principal.getName();
            }

            model.addAttribute("userName", principal.getAttribute("name"));
            model.addAttribute("userEmail", principal.getAttribute("email"));
            Object picture = principal.getAttribute("picture");
            if (picture == null) {
                picture = principal.getAttribute("image");
            }
            model.addAttribute("userPicture", picture != null ? picture.toString() : null);

            if (email != null) {
                UsuarioPermiso usuarioPermiso = usuarioPermisoRepository
                        .findByEmailIgnoreCaseWithRolAndPermisos(email)
                        .orElse(null);

                if (usuarioPermiso != null
                        && usuarioPermiso.getRol() != null
                        && usuarioPermiso.getRol().getPermisos() != null) {
                    puedeCargarExcel = usuarioPermiso.getRol().getPermisos().stream()
                            .anyMatch(p -> "CARGA_EXCEL".equalsIgnoreCase(p.getCodigo()));
                    puedeAdministrar = usuarioPermiso.getRol().getPermisos().stream()
                            .anyMatch(p -> "ADMIN_USUARIOS".equalsIgnoreCase(p.getCodigo()));
                }
                UsuarioPermiso up = usuarioPermisoRepository.findByEmailIgnoreCase(email).orElse(null);
                if (up != null) {
                    List<Examen> examenes = examenRepository.findByUsuarioPermisoWithTipoExamen(up);
                    Map<Long, Long> aciertosPorExamen = new HashMap<>();
                    for (Examen e : examenes) {
                        aciertosPorExamen.put(e.getId(), examenPreguntaRepository.countByExamen_IdAndAciertoTrue(e.getId()));
                    }
                    model.addAttribute("examenes", examenes);
                    model.addAttribute("aciertosPorExamen", aciertosPorExamen);
                } else {
                    model.addAttribute("examenes", List.<Examen>of());
                    model.addAttribute("aciertosPorExamen", Map.<Long, Long>of());
                }
            }
        }
        if (!model.containsAttribute("examenes")) {
            model.addAttribute("examenes", List.<Examen>of());
            model.addAttribute("aciertosPorExamen", Map.<Long, Long>of());
        }

        model.addAttribute("puedeCargarExcel", puedeCargarExcel);
        model.addAttribute("puedeAdministrar", puedeAdministrar);
        return "examen";
    }

    @Transactional
    @PostMapping("/examen/borrar/{id}")
    public String borrarExamen(@PathVariable("id") Long id, @AuthenticationPrincipal OAuth2User principal,
                               RedirectAttributes redirectAttributes) {
        if (principal == null) {
            return "redirect:/examen";
        }
        String email = principal.getAttribute("email");
        if (email == null) {
            email = principal.getAttribute("name");
        }
        if (email == null) {
            email = principal.getName();
        }
        UsuarioPermiso up = email != null ? usuarioPermisoRepository.findByEmailIgnoreCase(email).orElse(null) : null;
        Examen examen = id != null ? examenRepository.findById(id).orElse(null) : null;
        if (up == null || examen == null || examen.getUsuarioPermiso() == null
                || !examen.getUsuarioPermiso().getIdUsuarioPermiso().equals(up.getIdUsuarioPermiso())) {
            return "redirect:/examen";
        }
        examenPreguntaRepository.deleteByExamen_Id(examen.getId());
        examenRepository.delete(examen);
        redirectAttributes.addFlashAttribute("mensaje", "Examen borrado correctamente.");
        return "redirect:/examen";
    }

    @GetMapping("/examen/resultados/{id}")
    public String resultadosExamen(@PathVariable("id") Long id, Model model, @AuthenticationPrincipal OAuth2User principal) {
        if (principal == null) {
            return "redirect:/examen";
        }
        String email = principal.getAttribute("email");
        if (email == null) email = principal.getAttribute("name");
        if (email == null) email = principal.getName();
        UsuarioPermiso up = email != null ? usuarioPermisoRepository.findByEmailIgnoreCase(email).orElse(null) : null;
        Examen examen = id != null ? examenRepository.findById(id).orElse(null) : null;
        if (up == null || examen == null || examen.getUsuarioPermiso() == null
                || !examen.getUsuarioPermiso().getIdUsuarioPermiso().equals(up.getIdUsuarioPermiso())) {
            return "redirect:/examen";
        }
        List<ExamenPregunta> examenPreguntas = examenPreguntaRepository.findByExamenIdWithPregunta(examen.getId());
        model.addAttribute("userName", principal.getAttribute("name"));
        model.addAttribute("userEmail", principal.getAttribute("email"));
        Object picture = principal.getAttribute("picture");
        if (picture == null) picture = principal.getAttribute("image");
        model.addAttribute("userPicture", picture != null ? picture.toString() : null);
        UsuarioPermiso usuarioPermiso = usuarioPermisoRepository.findByEmailIgnoreCaseWithRolAndPermisos(email).orElse(null);
        boolean puedeCargarExcel = usuarioPermiso != null && usuarioPermiso.getRol() != null && usuarioPermiso.getRol().getPermisos() != null
                && usuarioPermiso.getRol().getPermisos().stream().anyMatch(p -> "CARGA_EXCEL".equalsIgnoreCase(p.getCodigo()));
        model.addAttribute("puedeCargarExcel", puedeCargarExcel);
        model.addAttribute("examen", examen);
        model.addAttribute("examenPreguntas", examenPreguntas);
        return "examen-resultados";
    }

    @GetMapping("/agregar-examen")
    public String agregarExamen(Model model, @AuthenticationPrincipal OAuth2User principal) {
        boolean puedeCargarExcel = false;

        if (principal != null) {
            String email = principal.getAttribute("email");
            if (email == null) {
                email = principal.getAttribute("name");
            }
            if (email == null) {
                email = principal.getName();
            }

            model.addAttribute("userName", principal.getAttribute("name"));
            model.addAttribute("userEmail", principal.getAttribute("email"));
            Object picture = principal.getAttribute("picture");
            if (picture == null) {
                picture = principal.getAttribute("image");
            }
            model.addAttribute("userPicture", picture != null ? picture.toString() : null);

            if (email != null) {
                UsuarioPermiso usuarioPermiso = usuarioPermisoRepository
                        .findByEmailIgnoreCaseWithRolAndPermisos(email)
                        .orElse(null);

                if (usuarioPermiso != null
                        && usuarioPermiso.getRol() != null
                        && usuarioPermiso.getRol().getPermisos() != null) {
                    puedeCargarExcel = usuarioPermiso.getRol().getPermisos().stream()
                            .anyMatch(p -> "CARGA_EXCEL".equalsIgnoreCase(p.getCodigo()));
                }
            }
        }

        model.addAttribute("puedeCargarExcel", puedeCargarExcel);

        List<TipoExamen> tiposExamen = tipoExamenRepository.findAll();
        Map<Long, Long> cantidadPreguntasPorTipo = new HashMap<>();
        Long idTipoTodasLasAreas = tipoExamenRepository
                .findByDescripcionIgnoreCase(DataInitializer.DESCRIPCION_TODAS_LAS_AREAS)
                .map(TipoExamen::getIdExamen)
                .orElse(null);
        for (TipoExamen t : tiposExamen) {
            long cantidad = idTipoTodasLasAreas != null && idTipoTodasLasAreas.equals(t.getIdExamen())
                    ? preguntaRepository.count()
                    : preguntaRepository.countByTipoExamen_IdExamen(t.getIdExamen());
            cantidadPreguntasPorTipo.put(t.getIdExamen(), cantidad);
        }
        model.addAttribute("tiposExamen", tiposExamen);
        model.addAttribute("cantidadPreguntasPorTipo", cantidadPreguntasPorTipo);
        model.addAttribute("idTipoTodasLasAreas", idTipoTodasLasAreas);

        return "agregar-examen";
    }

    @GetMapping("/admin")
    public String admin(Model model, @AuthenticationPrincipal OAuth2User principal) {
        String email = obtenerEmail(principal);
        if (email == null) {
            return "redirect:/acceso-denegado";
        }
        UsuarioPermiso usuarioPermiso = usuarioPermisoRepository.findByEmailIgnoreCaseWithRolAndPermisos(email).orElse(null);
        boolean puedeAdministrar = usuarioPermiso != null
                && usuarioPermiso.getRol() != null
                && usuarioPermiso.getRol().getPermisos() != null
                && usuarioPermiso.getRol().getPermisos().stream().anyMatch(p -> "ADMIN_USUARIOS".equalsIgnoreCase(p.getCodigo()));
        if (!puedeAdministrar) {
            return "redirect:/acceso-denegado";
        }
        boolean puedeCargarExcel = false;
        if (usuarioPermiso != null && usuarioPermiso.getRol() != null && usuarioPermiso.getRol().getPermisos() != null) {
            puedeCargarExcel = usuarioPermiso.getRol().getPermisos().stream()
                    .anyMatch(p -> "CARGA_EXCEL".equalsIgnoreCase(p.getCodigo()));
        }
        model.addAttribute("userName", principal.getAttribute("name"));
        model.addAttribute("userEmail", principal.getAttribute("email"));
        Object picture = principal.getAttribute("picture");
        if (picture == null) {
            picture = principal.getAttribute("image");
        }
        model.addAttribute("userPicture", picture != null ? picture.toString() : null);
        model.addAttribute("puedeCargarExcel", puedeCargarExcel);
        return "admin";
    }

    @GetMapping("/admin/preguntas")
    public String adminPreguntas(Model model, @AuthenticationPrincipal OAuth2User principal) {
        String email = obtenerEmail(principal);
        if (email == null) {
            return "redirect:/acceso-denegado";
        }
        UsuarioPermiso usuarioPermiso = usuarioPermisoRepository.findByEmailIgnoreCaseWithRolAndPermisos(email).orElse(null);
        boolean puedeAdministrar = usuarioPermiso != null
                && usuarioPermiso.getRol() != null
                && usuarioPermiso.getRol().getPermisos() != null
                && usuarioPermiso.getRol().getPermisos().stream().anyMatch(p -> "ADMIN_USUARIOS".equalsIgnoreCase(p.getCodigo()));
        if (!puedeAdministrar) {
            return "redirect:/acceso-denegado";
        }
        model.addAttribute("userName", principal.getAttribute("name"));
        model.addAttribute("userEmail", principal.getAttribute("email"));
        Object picture = principal.getAttribute("picture");
        if (picture == null) {
            picture = principal.getAttribute("image");
        }
        model.addAttribute("userPicture", picture != null ? picture.toString() : null);
        List<TipoExamen> tiposExamen = tipoExamenRepository.findAll().stream()
                .filter(tipo -> tipo.getDescripcion() == null
                        || !DataInitializer.DESCRIPCION_TODAS_LAS_AREAS.equalsIgnoreCase(tipo.getDescripcion()))
                .toList();
        model.addAttribute("tiposExamen", tiposExamen);
        return "admin-preguntas";
    }

    @GetMapping("/admin/preguntas/lista")
    @ResponseBody
    public List<Map<String, Object>> listarPreguntasPorTipo(@RequestParam("tipoExamenId") Long tipoExamenId,
                                                             @AuthenticationPrincipal OAuth2User principal) {
        String email = obtenerEmail(principal);
        if (email == null) {
            return List.of();
        }
        UsuarioPermiso usuarioPermiso = usuarioPermisoRepository.findByEmailIgnoreCaseWithRolAndPermisos(email).orElse(null);
        boolean puedeAdministrar = usuarioPermiso != null
                && usuarioPermiso.getRol() != null
                && usuarioPermiso.getRol().getPermisos() != null
                && usuarioPermiso.getRol().getPermisos().stream().anyMatch(p -> "ADMIN_USUARIOS".equalsIgnoreCase(p.getCodigo()));
        if (!puedeAdministrar || tipoExamenId == null) {
            return List.of();
        }
        return preguntaRepository.findByTipoExamen_IdExamen(tipoExamenId).stream()
                .map(p -> {
                    Map<String, Object> row = new HashMap<>();
                    row.put("idPregunta", p.getIdPregunta());
                    row.put("pregunta", p.getPregunta());
                    row.put("answer1", p.getAnswer1());
                    row.put("answer2", p.getAnswer2());
                    row.put("answer3", p.getAnswer3());
                    row.put("answer4", p.getAnswer4());
                    row.put("respuesta", p.getRespuesta());
                    row.put("enrichQuestion", p.getEnrichQuestion());
                    row.put("feedback", p.getFeedback());
                    return row;
                })
                .toList();
    }

    @PostMapping("/admin/preguntas")
    @ResponseBody
    public Map<String, Object> crearPregunta(@RequestBody Map<String, Object> body,
                                             @AuthenticationPrincipal OAuth2User principal) {
        if (!puedeAdministrar(principal)) {
            return Map.of("ok", false, "error", "No autorizado.");
        }
        Long tipoExamenId = toLong(body.get("tipoExamenId"));
        if (tipoExamenId == null) {
            return Map.of("ok", false, "error", "Seleccione un tipo de examen.");
        }
        TipoExamen tipoExamen = tipoExamenRepository.findById(tipoExamenId).orElse(null);
        if (tipoExamen == null) {
            return Map.of("ok", false, "error", "Tipo de examen no encontrado.");
        }
        String preguntaTexto = toString(body.get("pregunta"));
        if (preguntaTexto == null || preguntaTexto.isBlank()) {
            return Map.of("ok", false, "error", "La pregunta es obligatoria.");
        }
        Pregunta p = new Pregunta();
        p.setTipoExamen(tipoExamen);
        p.setPregunta(preguntaTexto);
        p.setAnswer1(toString(body.get("answer1")));
        p.setAnswer2(toString(body.get("answer2")));
        p.setAnswer3(toString(body.get("answer3")));
        p.setAnswer4(toString(body.get("answer4")));
        p.setRespuesta(toString(body.get("respuesta")));
        p.setEnrichQuestion(toString(body.get("enrichQuestion")));
        p.setFeedback(toString(body.get("feedback")));
        p = preguntaRepository.save(p);
        return Map.of("ok", true, "idPregunta", p.getIdPregunta());
    }

    @PutMapping("/admin/preguntas/{idPregunta}")
    @ResponseBody
    public Map<String, Object> editarPregunta(@PathVariable("idPregunta") Integer idPregunta,
                                              @RequestBody Map<String, Object> body,
                                              @AuthenticationPrincipal OAuth2User principal) {
        if (!puedeAdministrar(principal)) {
            return Map.of("ok", false, "error", "No autorizado.");
        }
        Pregunta p = preguntaRepository.findById(idPregunta).orElse(null);
        if (p == null) {
            return Map.of("ok", false, "error", "Pregunta no encontrada.");
        }
        Long tipoExamenId = toLong(body.get("tipoExamenId"));
        if (tipoExamenId != null) {
            TipoExamen tipoExamen = tipoExamenRepository.findById(tipoExamenId).orElse(null);
            if (tipoExamen != null) {
                p.setTipoExamen(tipoExamen);
            }
        }
        String preguntaTexto = toString(body.get("pregunta"));
        if (preguntaTexto == null || preguntaTexto.isBlank()) {
            return Map.of("ok", false, "error", "La pregunta es obligatoria.");
        }
        p.setPregunta(preguntaTexto);
        p.setAnswer1(toString(body.get("answer1")));
        p.setAnswer2(toString(body.get("answer2")));
        p.setAnswer3(toString(body.get("answer3")));
        p.setAnswer4(toString(body.get("answer4")));
        p.setRespuesta(toString(body.get("respuesta")));
        p.setEnrichQuestion(toString(body.get("enrichQuestion")));
        p.setFeedback(toString(body.get("feedback")));
        preguntaRepository.save(p);
        return Map.of("ok", true, "idPregunta", p.getIdPregunta());
    }

    @GetMapping("/acceso-denegado")
    public String accesoDenegado() {
        return "acceso-denegado";
    }

    @GetMapping("/logout")
    public String logout(HttpServletRequest request, HttpServletResponse response) {
        new SecurityContextLogoutHandler().logout(request, response,
                SecurityContextHolder.getContext().getAuthentication());
        return "redirect:/";
    }

    private static String obtenerEmail(OAuth2User principal) {
        if (principal == null) return null;
        String email = principal.getAttribute("email");
        if (email == null) {
            email = principal.getAttribute("name");
        }
        if (email == null) {
            email = principal.getName();
        }
        return email;
    }

    private boolean puedeAdministrar(OAuth2User principal) {
        String email = obtenerEmail(principal);
        if (email == null) {
            return false;
        }
        UsuarioPermiso usuarioPermiso = usuarioPermisoRepository.findByEmailIgnoreCaseWithRolAndPermisos(email).orElse(null);
        return usuarioPermiso != null
                && usuarioPermiso.getRol() != null
                && usuarioPermiso.getRol().getPermisos() != null
                && usuarioPermiso.getRol().getPermisos().stream().anyMatch(p -> "ADMIN_USUARIOS".equalsIgnoreCase(p.getCodigo()));
    }

    private static Long toLong(Object value) {
        if (value == null) return null;
        if (value instanceof Number number) return number.longValue();
        try {
            return Long.parseLong(String.valueOf(value));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static String toString(Object value) {
        return value == null ? null : String.valueOf(value);
    }
}
