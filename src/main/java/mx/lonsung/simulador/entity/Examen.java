package mx.lonsung.simulador.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "examen")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Examen {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario_permiso", nullable = false)
    private UsuarioPermiso usuarioPermiso;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_examen", nullable = false)
    private TipoExamen tipoExamen;

    @Column(name = "fecha_hora_inicio", nullable = false)
    private LocalDateTime fechaHoraInicio;

    @Column(name = "minutos_transcurrido")
    private Integer minutosTranscurrido;

    @Column(name = "numero_preguntas")
    private Integer numeroPreguntas;

    @Column(name = "estado", length = 20)
    private String estado;

    @Column(name = "fecha_hora_fin")
    private LocalDateTime fechaHoraFin;
}
