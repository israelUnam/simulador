package mx.lonsung.simulador.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "examen_pregunta")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExamenPregunta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_examen", nullable = false)
    private Examen examen;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_pregunta", nullable = false)
    private Pregunta pregunta;

    @Column(name = "respuesta", length = 50)
    private String respuesta;

    @Column(name = "fecha_guardado")
    private LocalDateTime fechaGuardado;

    @Column(name = "fecha_acceso")
    private LocalDateTime fechaAcceso;

    @Column(name = "acierto")
    private Boolean acierto;

    @Column(name = "pregunta_pendiente")
    private Boolean preguntaPendiente;
}
