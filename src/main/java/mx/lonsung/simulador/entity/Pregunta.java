package mx.lonsung.simulador.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "pregunta")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Pregunta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_pregunta")
    private Integer idPregunta;

    @Column(name = "question", length = 1200, nullable = false)
    private String pregunta;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_tipo_examen")
    private TipoExamen tipoExamen;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_grupo_proceso")
    private GrupoProceso grupoProceso;

    @Column(name = "answer1", length = 800)
    private String answer1;

    @Column(name = "answer2", length = 800)
    private String answer2;

    @Column(name = "answer3", length = 800)
    private String answer3;

    @Column(name = "answer4", length = 800)
    private String answer4;

    @Column(name = "respuesta", length = 50)
    private String respuesta;

    @Column(name = "feedback", columnDefinition = "TEXT")
    private String feedback;

    @Column(name = "enrich_question", columnDefinition = "TEXT")
    private String enrichQuestion;


}
