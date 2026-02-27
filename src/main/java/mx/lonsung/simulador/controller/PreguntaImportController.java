package mx.lonsung.simulador.controller;

import jakarta.servlet.http.HttpServletRequest;
import mx.lonsung.simulador.service.PreguntaImportService;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/importar-preguntas")
public class PreguntaImportController {

    private final PreguntaImportService preguntaImportService;

    public PreguntaImportController(PreguntaImportService preguntaImportService) {
        this.preguntaImportService = preguntaImportService;
    }

    @GetMapping
    public String formulario(Model model, HttpServletRequest request) {
        CsrfToken csrf = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
        if (csrf != null) {
            model.addAttribute("_csrf", csrf);
        }
        return "importar-preguntas";
    }

    @PostMapping
    public String importar(@RequestParam("archivo") MultipartFile archivo,
                           RedirectAttributes redirectAttributes) {
        if (archivo.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Seleccione un archivo Excel.");
            return "redirect:/importar-preguntas";
        }
        try {
            int count = preguntaImportService.importarDesdeExcel(archivo);
            redirectAttributes.addFlashAttribute("mensaje", "Se importaron " + count + " pregunta(s) correctamente.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al importar: " + e.getMessage());
        }
        return "redirect:/importar-preguntas";
    }
}
