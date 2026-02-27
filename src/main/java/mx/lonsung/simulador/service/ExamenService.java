package mx.lonsung.simulador.service;

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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class ExamenService {

    private static final String ESTADO_EN_CURSO = "EN_CURSO";

    private final ExamenRepository examenRepository;
    private final ExamenPreguntaRepository examenPreguntaRepository;
    private final PreguntaRepository preguntaRepository;
    private final TipoExamenRepository tipoExamenRepository;
    private final UsuarioPermisoRepository usuarioPermisoRepository;

    public ExamenService(ExamenRepository examenRepository,
                         ExamenPreguntaRepository examenPreguntaRepository,
                         PreguntaRepository preguntaRepository,
                         TipoExamenRepository tipoExamenRepository,
                         UsuarioPermisoRepository usuarioPermisoRepository) {
        this.examenRepository = examenRepository;
        this.examenPreguntaRepository = examenPreguntaRepository;
        this.preguntaRepository = preguntaRepository;
        this.tipoExamenRepository = tipoExamenRepository;
        this.usuarioPermisoRepository = usuarioPermisoRepository;
    }

    /**
     * Crea un registro en examen, elige aleatoriamente preguntas del tipo indicado
     * e inserta filas en examen_pregunta. Devuelve el examen creado.
     */
    @Transactional
    public Examen crearExamen(Long idTipoExamen, int numeroPreguntas, String emailUsuario) {
        UsuarioPermiso usuarioPermiso = usuarioPermisoRepository.findByEmailIgnoreCase(emailUsuario)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado: " + emailUsuario));
        TipoExamen tipoExamen = tipoExamenRepository.findById(idTipoExamen)
                .orElseThrow(() -> new IllegalArgumentException("Tipo de examen no encontrado: " + idTipoExamen));

        Examen examen = new Examen();
        examen.setUsuarioPermiso(usuarioPermiso);
        examen.setTipoExamen(tipoExamen);
        examen.setFechaHoraInicio(LocalDateTime.now());
        examen.setNumeroPreguntas(numeroPreguntas);
        examen.setEstado(ESTADO_EN_CURSO);
        examen = examenRepository.save(examen);

        List<Pregunta> todas = preguntaRepository.findByTipoExamen_IdExamen(idTipoExamen);
        if (todas.isEmpty()) {
            return examen;
        }
        Collections.shuffle(todas, ThreadLocalRandom.current());
        int limit = Math.min(numeroPreguntas, todas.size());
        List<Pregunta> elegidas = todas.subList(0, limit);

        for (Pregunta p : elegidas) {
            ExamenPregunta ep = new ExamenPregunta();
            ep.setExamen(examen);
            ep.setPregunta(p);
            ep.setRespuesta(null);
            ep.setFechaGuardado(null);
            examenPreguntaRepository.save(ep);
        }

        return examen;
    }
}
