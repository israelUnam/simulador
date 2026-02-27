package mx.lonsung.simulador.service;

import mx.lonsung.simulador.entity.Pregunta;
import mx.lonsung.simulador.repository.GrupoProcesoRepository;
import mx.lonsung.simulador.repository.PreguntaRepository;
import mx.lonsung.simulador.repository.TipoExamenRepository;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.Map;

/**
 * Importa preguntas desde un Excel (.xlsx).
 * Solo lee Sheet1. Formato: columna A = tipo de dato, columna B = valor.
 * - "Question" -> campo pregunta (y se inicia una nueva pregunta).
 * - "Answer1" -> answer1, "Answer2" -> answer2, "Answer3" -> answer3, "Answer4" -> answer4 (sobre la pregunta actual).
 *   Si la columna C tiene "x" o "X", el número del Answer (1,2,3,4) se guarda en respuesta.
 * - "Category" -> id_grupo_proceso: INICIO=1, PLANIFICACION=2, EJECUCION=3, MONITOREO Y CONTROL=4, CIERRE=5.
 * - "FeedbackTrue" -> feedback, "EnrichQuestion" -> enrichQuestion.
 * Según nombre del archivo (o celda A1 para el primero): "01Marcodereferencia" -> id_tipo_examen = 2;
 * "02ProcesosdelaDirecciondeProyectos3_ok" -> id_tipo_examen = 3; "03gestiondelaintegracion3_ok" -> id_tipo_examen = 4;
 * "04Gestiondelalcance3-ok" -> id_tipo_examen = 5; "05gestiondelcronograma3-ok" -> id_tipo_examen = 6;
 * "06GestiondelCosto3-ok" -> id_tipo_examen = 7; "07Gestiondelacalidad3-ok" -> id_tipo_examen = 8;
 * "08GestiondeRecursos_ok" -> id_tipo_examen = 9; "09GestiondelasComunicaciones_ok" -> id_tipo_examen = 10;
 * "10GestiondelRiesgo_ok" -> id_tipo_examen = 11; "11Gestiondelasadquisiciones_ok" -> id_tipo_examen = 12;
 * "12GestionInteresados_ok" -> id_tipo_examen = 13; "13ResponsabilidadProfesionalySocial_ok" -> id_tipo_examen = 14.
 * El resto de renglones se ignoran.
 */
@Service
public class PreguntaImportService {

    private static final String PREFIJO_MARCO_REFERENCIA = "01Marcodereferencia";
    private static final String PREFIJO_PROCESOS_DIRECCION = "02ProcesosdelaDirecciondeProyectos3_ok";
    private static final String PREFIJO_GESTION_INTEGRACION = "03gestiondelaintegracion3_ok";
    private static final String PREFIJO_GESTION_ALCANCE = "04Gestiondelalcance3-ok";
    private static final String PREFIJO_GESTION_CRONOGRAMA = "05gestiondelcronograma3-ok";
    private static final String PREFIJO_GESTION_COSTO = "06GestiondelCosto3-ok";
    private static final String PREFIJO_GESTION_CALIDAD = "07Gestiondelacalidad3-ok";
    private static final String PREFIJO_GESTION_RECURSOS = "08GestiondeRecursos_ok";
    private static final String PREFIJO_GESTION_COMUNICACIONES = "09GestiondelasComunicaciones_ok";
    private static final String PREFIJO_GESTION_RIESGO = "10GestiondelRiesgo_ok";
    private static final String PREFIJO_GESTION_ADQUISICIONES = "11Gestiondelasadquisiciones_ok";
    private static final String PREFIJO_GESTION_INTERESADOS = "12GestionInteresados_ok";
    private static final String PREFIJO_RESPONSABILIDAD_PROFESIONAL = "13ResponsabilidadProfesionalySocial_ok";
    private static final long ID_TIPO_EXAMEN_MARCO_REFERENCIA = 2L;
    private static final long ID_TIPO_EXAMEN_PROCESOS_DIRECCION = 3L;
    private static final long ID_TIPO_EXAMEN_GESTION_INTEGRACION = 4L;
    private static final long ID_TIPO_EXAMEN_GESTION_ALCANCE = 5L;
    private static final long ID_TIPO_EXAMEN_GESTION_CRONOGRAMA = 6L;
    private static final long ID_TIPO_EXAMEN_GESTION_COSTO = 7L;
    private static final long ID_TIPO_EXAMEN_GESTION_CALIDAD = 8L;
    private static final long ID_TIPO_EXAMEN_GESTION_RECURSOS = 9L;
    private static final long ID_TIPO_EXAMEN_GESTION_COMUNICACIONES = 10L;
    private static final long ID_TIPO_EXAMEN_GESTION_RIESGO = 11L;
    private static final long ID_TIPO_EXAMEN_GESTION_ADQUISICIONES = 12L;
    private static final long ID_TIPO_EXAMEN_GESTION_INTERESADOS = 13L;
    private static final long ID_TIPO_EXAMEN_RESPONSABILIDAD_PROFESIONAL = 14L;

    private static final String ETIQUETA_QUESTION = "Question";
    private static final String ETIQUETA_CATEGORY = "Category";
    private static final String ETIQUETA_FEEDBACK_TRUE = "FeedbackTrue";
    private static final String ETIQUETA_ENRICH_QUESTION = "EnrichQuestion";

    /** Category (columna B) -> id_grupo_proceso */
    private static final Map<String, Long> CATEGORY_A_GRUPO = Map.of(
            "INICIO", 1L,
            "PLANIFICACION", 2L,
            "EJECUCION", 3L,
            "EJECUCIÓN", 3L,
            "MONITOREO Y CONTROL", 4L,
            "SEGUIMIENTO Y CONTROL", 4L,
            "CIERRE", 5L
    );

    private final PreguntaRepository preguntaRepository;
    private final TipoExamenRepository tipoExamenRepository;
    private final GrupoProcesoRepository grupoProcesoRepository;

    public PreguntaImportService(PreguntaRepository preguntaRepository,
                                 TipoExamenRepository tipoExamenRepository,
                                 GrupoProcesoRepository grupoProcesoRepository) {
        this.preguntaRepository = preguntaRepository;
        this.tipoExamenRepository = tipoExamenRepository;
        this.grupoProcesoRepository = grupoProcesoRepository;
    }

    @Transactional
    public int importarDesdeExcel(MultipartFile file) throws Exception {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("El archivo está vacío");
        }
        String name = file.getOriginalFilename();
        if (name == null || (!name.endsWith(".xlsx") && !name.endsWith(".xls"))) {
            throw new IllegalArgumentException("Solo se aceptan archivos Excel (.xlsx o .xls)");
        }

        try (InputStream is = file.getInputStream();
             Workbook workbook = name.toLowerCase().endsWith(".xlsx")
                     ? new XSSFWorkbook(is)
                     : new org.apache.poi.hssf.usermodel.HSSFWorkbook(is)) {

            Sheet sheet = workbook.getSheetAt(0); // solo Sheet1
            String nombreArchivo = (name != null) ? name.trim() : "";
            String nombreLower = nombreArchivo.toLowerCase();
            Row firstRow = sheet.getRow(0);
            String a1 = firstRow != null ? getCellString(firstRow, 0) : null;
            Long idTipoExamen = null;
            if (nombreLower.startsWith(PREFIJO_PROCESOS_DIRECCION.toLowerCase())) {
                idTipoExamen = ID_TIPO_EXAMEN_PROCESOS_DIRECCION;
            } else if (nombreLower.startsWith(PREFIJO_GESTION_INTEGRACION.toLowerCase())) {
                idTipoExamen = ID_TIPO_EXAMEN_GESTION_INTEGRACION;
            } else if (nombreLower.startsWith(PREFIJO_GESTION_ALCANCE.toLowerCase())) {
                idTipoExamen = ID_TIPO_EXAMEN_GESTION_ALCANCE;
            } else if (nombreLower.startsWith(PREFIJO_GESTION_CRONOGRAMA.toLowerCase())) {
                idTipoExamen = ID_TIPO_EXAMEN_GESTION_CRONOGRAMA;
            } else if (nombreLower.startsWith(PREFIJO_GESTION_COSTO.toLowerCase())) {
                idTipoExamen = ID_TIPO_EXAMEN_GESTION_COSTO;
            } else if (nombreLower.startsWith(PREFIJO_GESTION_CALIDAD.toLowerCase())) {
                idTipoExamen = ID_TIPO_EXAMEN_GESTION_CALIDAD;
            } else if (nombreLower.startsWith(PREFIJO_GESTION_RECURSOS.toLowerCase())) {
                idTipoExamen = ID_TIPO_EXAMEN_GESTION_RECURSOS;
            } else if (nombreLower.startsWith(PREFIJO_GESTION_COMUNICACIONES.toLowerCase())) {
                idTipoExamen = ID_TIPO_EXAMEN_GESTION_COMUNICACIONES;
            } else if (nombreLower.startsWith(PREFIJO_GESTION_RIESGO.toLowerCase())) {
                idTipoExamen = ID_TIPO_EXAMEN_GESTION_RIESGO;
            } else if (nombreLower.startsWith(PREFIJO_GESTION_ADQUISICIONES.toLowerCase())) {
                idTipoExamen = ID_TIPO_EXAMEN_GESTION_ADQUISICIONES;
            } else if (nombreLower.startsWith(PREFIJO_GESTION_INTERESADOS.toLowerCase())) {
                idTipoExamen = ID_TIPO_EXAMEN_GESTION_INTERESADOS;
            } else if (nombreLower.startsWith(PREFIJO_RESPONSABILIDAD_PROFESIONAL.toLowerCase())) {
                idTipoExamen = ID_TIPO_EXAMEN_RESPONSABILIDAD_PROFESIONAL;
            } else if (nombreLower.startsWith(PREFIJO_MARCO_REFERENCIA.toLowerCase())
                    || (a1 != null && a1.trim().toLowerCase().startsWith(PREFIJO_MARCO_REFERENCIA.toLowerCase()))) {
                idTipoExamen = ID_TIPO_EXAMEN_MARCO_REFERENCIA;
            }

            int count = 0;
            Pregunta current = null;

            for (int r = 0; r <= sheet.getLastRowNum(); r++) {
                Row row = sheet.getRow(r);
                if (row == null) continue;

                String colA = getCellString(row, 0);
                if (colA == null || colA.isBlank()) continue;

                String colB = getCellString(row, 1);
                String colC = getCellString(row, 2);
                String valor = (colB != null && !colB.isBlank()) ? colB.trim() : null;
                String etiqueta = colA.trim();
                boolean colCEsX = colC != null && "x".equalsIgnoreCase(colC.trim());

                if (ETIQUETA_QUESTION.equalsIgnoreCase(etiqueta) && valor != null) {
                    if (current != null) {
                        preguntaRepository.save(current);
                        count++;
                    }
                    current = new Pregunta();
                    current.setPregunta(valor);
                    if (idTipoExamen != null) {
                        tipoExamenRepository.findById(idTipoExamen).ifPresent(current::setTipoExamen);
                    }
                } else if (current != null && valor != null) {
                    if (etiqueta.regionMatches(true, 0, "Answer", 0, 6)) {
                        String sufijo = etiqueta.length() >= 7 ? etiqueta.substring(etiqueta.length() - 1) : "";
                        if ("1".equals(sufijo)) {
                            current.setAnswer1(valor);
                            if (colCEsX) current.setRespuesta("1");
                        } else if ("2".equals(sufijo)) {
                            current.setAnswer2(valor);
                            if (colCEsX) current.setRespuesta("2");
                        } else if ("3".equals(sufijo)) {
                            current.setAnswer3(valor);
                            if (colCEsX) current.setRespuesta("3");
                        } else if ("4".equals(sufijo)) {
                            current.setAnswer4(valor);
                            if (colCEsX) current.setRespuesta("4");
                        }
                    } else if (ETIQUETA_CATEGORY.equalsIgnoreCase(etiqueta)) {
                        Long idGrupo = CATEGORY_A_GRUPO.entrySet().stream()
                                .filter(e -> e.getKey().equalsIgnoreCase(valor.trim()))
                                .findFirst()
                                .map(Map.Entry::getValue)
                                .orElse(null);
                        if (idGrupo != null) {
                            grupoProcesoRepository.findById(idGrupo).ifPresent(current::setGrupoProceso);
                        }
                    } else if (ETIQUETA_FEEDBACK_TRUE.equalsIgnoreCase(etiqueta)) {
                        current.setFeedback(valor);
                    } else if (ETIQUETA_ENRICH_QUESTION.equalsIgnoreCase(etiqueta)) {
                        current.setEnrichQuestion(valor);
                    }
                }
            }
            if (current != null) {
                preguntaRepository.save(current);
                count++;
            }
            return count;
        }
    }

    private static String cellToString(Cell cell) {
        if (cell == null) return "";
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue();
            case NUMERIC -> cell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)
                    ? "" : String.valueOf((long) cell.getNumericCellValue());
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            case FORMULA -> cell.getCellFormula();
            default -> "";
        };
    }

    private static String getCellString(Row row, int colIndex) {
        if (colIndex < 0) return null;
        String s = cellToString(row.getCell(colIndex));
        return s == null || s.isEmpty() ? null : s.trim();
    }
}
