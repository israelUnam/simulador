package mx.lonsung.simulador.controller;

import mx.lonsung.simulador.entity.Examen;
import mx.lonsung.simulador.entity.ExamenPregunta;
import mx.lonsung.simulador.entity.UsuarioPermiso;
import mx.lonsung.simulador.repository.ExamenPreguntaRepository;
import mx.lonsung.simulador.repository.ExamenRepository;
import mx.lonsung.simulador.repository.UsuarioPermisoRepository;
import mx.lonsung.simulador.service.ExamenService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Controller
public class ExamenController {

    private final ExamenService examenService;
    private final ExamenRepository examenRepository;
    private final ExamenPreguntaRepository examenPreguntaRepository;
    private final UsuarioPermisoRepository usuarioPermisoRepository;

    public ExamenController(ExamenService examenService,
                            ExamenRepository examenRepository,
                            ExamenPreguntaRepository examenPreguntaRepository,
                            UsuarioPermisoRepository usuarioPermisoRepository) {
        this.examenService = examenService;
        this.examenRepository = examenRepository;
        this.examenPreguntaRepository = examenPreguntaRepository;
        this.usuarioPermisoRepository = usuarioPermisoRepository;
    }

    @PostMapping("/crear-examen")
    public String crearExamen(@RequestParam(value = "tipoExamen", required = false) Long idTipoExamen,
                              @RequestParam("numPreguntas") int numPreguntas,
                              @AuthenticationPrincipal OAuth2User principal,
                              RedirectAttributes redirectAttributes) {
        String email = obtenerEmail(principal);
        if (email == null) {
            redirectAttributes.addFlashAttribute("error", "No se pudo identificar al usuario.");
            return "redirect:/agregar-examen";
        }
        if (idTipoExamen == null) {
            redirectAttributes.addFlashAttribute("error", "Seleccione un tipo de examen.");
            return "redirect:/agregar-examen";
        }
        if (numPreguntas < 1) {
            redirectAttributes.addFlashAttribute("error", "El número de preguntas debe ser al menos 1.");
            return "redirect:/agregar-examen";
        }
        Examen examen = examenService.crearExamen(idTipoExamen, numPreguntas, email);
        return "redirect:/examen-en-progreso/indice?id=" + examen.getId();
    }

    @GetMapping("/examen-en-progreso/indice")
    public String indicePreguntas(@RequestParam("id") Long idExamen,
                                  @AuthenticationPrincipal OAuth2User principal,
                                  Model model) {
        Examen examen = examenRepository.findById(idExamen).orElse(null);
        if (examen == null) {
            return "redirect:/examen";
        }
        List<ExamenPregunta> items = examenPreguntaRepository.findByExamenIdWithPregunta(idExamen);
        model.addAttribute("examen", examen);
        model.addAttribute("examenPreguntas", items);
        model.addAttribute("totalPreguntas", items.size());

        if (principal != null) {
            model.addAttribute("userName", principal.getAttribute("name"));
            model.addAttribute("userEmail", principal.getAttribute("email"));
            Object picture = principal.getAttribute("picture");
            if (picture == null) picture = principal.getAttribute("image");
            model.addAttribute("userPicture", picture != null ? picture.toString() : null);
            String email = obtenerEmail(principal);
            boolean puedeCargarExcel = false;
            if (email != null) {
                UsuarioPermiso up = usuarioPermisoRepository.findByEmailIgnoreCaseWithRolAndPermisos(email).orElse(null);
                if (up != null && up.getRol() != null && up.getRol().getPermisos() != null) {
                    puedeCargarExcel = up.getRol().getPermisos().stream()
                            .anyMatch(p -> "CARGA_EXCEL".equalsIgnoreCase(p.getCodigo()));
                }
            }
            model.addAttribute("puedeCargarExcel", puedeCargarExcel);
        }
        return "examen-en-progreso-indice";
    }

    @PostMapping("/examen-en-progreso/finalizar")
    public String finalizarExamen(@RequestParam("id") Long idExamen, RedirectAttributes redirectAttributes) {
        Examen examen = examenRepository.findById(idExamen).orElse(null);
        if (examen != null) {
            LocalDateTime ahora = LocalDateTime.now();
            examen.setFechaHoraFin(ahora);
            if (examen.getFechaHoraInicio() != null) {
                examen.setMinutosTranscurrido((int) ChronoUnit.MINUTES.between(examen.getFechaHoraInicio(), ahora));
            }
            examenRepository.save(examen);
        }
        redirectAttributes.addFlashAttribute("mensaje", "Examen finalizado.");
        return "redirect:/examen";
    }

    @GetMapping("/examen-en-progreso")
    public String examenEnProgreso(@RequestParam("id") Long idExamen,
                                  @RequestParam(value = "num", required = false) Integer num,
                                  @AuthenticationPrincipal OAuth2User principal,
                                  Model model) {
        Examen examen = examenRepository.findById(idExamen).orElse(null);
        if (examen == null) {
            return "redirect:/examen";
        }
        List<ExamenPregunta> items = examenPreguntaRepository.findByExamenIdWithPregunta(idExamen);
        if (items.isEmpty()) {
            return "redirect:/examen";
        }
        if (num == null || num < 1) num = 1;
        if (num > items.size()) {
            return "redirect:/examen";
        }
        ExamenPregunta examenPregunta = items.get(num - 1);
        if (examenPregunta.getFechaAcceso() == null) {
            examenPregunta.setFechaAcceso(LocalDateTime.now());
            examenPreguntaRepository.save(examenPregunta);
        }
        model.addAttribute("examen", examen);
        model.addAttribute("examenPregunta", examenPregunta);
        model.addAttribute("numPregunta", num);
        model.addAttribute("totalPreguntas", items.size());
        model.addAttribute("siguiente", num < items.size() ? num + 1 : null);

        if (principal != null) {
            model.addAttribute("userName", principal.getAttribute("name"));
            model.addAttribute("userEmail", principal.getAttribute("email"));
            Object picture = principal.getAttribute("picture");
            if (picture == null) picture = principal.getAttribute("image");
            model.addAttribute("userPicture", picture != null ? picture.toString() : null);
            String email = obtenerEmail(principal);
            boolean puedeCargarExcel = false;
            if (email != null) {
                UsuarioPermiso up = usuarioPermisoRepository.findByEmailIgnoreCaseWithRolAndPermisos(email).orElse(null);
                if (up != null && up.getRol() != null && up.getRol().getPermisos() != null) {
                    puedeCargarExcel = up.getRol().getPermisos().stream()
                            .anyMatch(p -> "CARGA_EXCEL".equalsIgnoreCase(p.getCodigo()));
                }
            }
            model.addAttribute("puedeCargarExcel", puedeCargarExcel);
        }
        return "examen-en-progreso";
    }

    @PostMapping("/examen-en-progreso/responder")
    public String responderPregunta(@RequestParam("id") Long idExamen,
                                    @RequestParam("num") int num,
                                    @RequestParam(value = "respuesta", required = false) String respuesta,
                                    @RequestParam(value = "irA", required = false) String irA,
                                    RedirectAttributes redirectAttributes) {
        List<ExamenPregunta> items = examenPreguntaRepository.findByExamenIdWithPregunta(idExamen);
        if (num >= 1 && num <= items.size()) {
            ExamenPregunta ep = items.get(num - 1);
            if (respuesta != null && (respuesta.equals("1") || respuesta.equals("2") || respuesta.equals("3") || respuesta.equals("4"))) {
                ep.setRespuesta(respuesta);
                ep.setFechaGuardado(LocalDateTime.now());
                String correcta = ep.getPregunta() != null && ep.getPregunta().getRespuesta() != null
                        ? ep.getPregunta().getRespuesta().trim() : null;
                ep.setAcierto(correcta != null && correcta.equals(respuesta));
                examenPreguntaRepository.save(ep);
            }
        }
        if ("anterior".equals(irA) && num > 1) {
            return "redirect:/examen-en-progreso?id=" + idExamen + "&num=" + (num - 1);
        }
        if ("indice".equals(irA)) {
            return "redirect:/examen-en-progreso/indice?id=" + idExamen;
        }
        if (num < items.size()) {
            return "redirect:/examen-en-progreso?id=" + idExamen + "&num=" + (num + 1);
        }
        Examen examen = examenRepository.findById(idExamen).orElse(null);
        if (examen != null) {
            LocalDateTime ahora = LocalDateTime.now();
            examen.setFechaHoraFin(ahora);
            if (examen.getFechaHoraInicio() != null) {
                examen.setMinutosTranscurrido((int) ChronoUnit.MINUTES.between(examen.getFechaHoraInicio(), ahora));
            }
            examenRepository.save(examen);
        }
        redirectAttributes.addFlashAttribute("mensaje", "Examen finalizado.");
        return "redirect:/examen";
    }

    @PostMapping("/examen-en-progreso/marcar-pendiente")
    public String marcarPendiente(@RequestParam("id") Long idExamen,
                                 @RequestParam("num") int num,
                                 RedirectAttributes redirectAttributes) {
        List<ExamenPregunta> items = examenPreguntaRepository.findByExamenIdWithPregunta(idExamen);
        if (num >= 1 && num <= items.size()) {
            ExamenPregunta ep = items.get(num - 1);
            ep.setPreguntaPendiente(true);
            examenPreguntaRepository.save(ep);
        }
        return "redirect:/examen-en-progreso?id=" + idExamen + "&num=" + num;
    }

    private static String obtenerEmail(OAuth2User principal) {
        if (principal == null) return null;
        String email = principal.getAttribute("email");
        if (email == null) email = principal.getAttribute("name");
        if (email == null) email = principal.getName();
        return email;
    }
}
