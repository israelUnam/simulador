package mx.lonsung.simulador.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "grupo_proceso")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GrupoProceso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_grupo_proceso")
    private Long idGrupoProceso;

    @Column(name = "descripcion", length = 255, nullable = false)
    private String descripcion;

    @OneToMany(mappedBy = "grupoProceso", fetch = FetchType.LAZY)
    private List<Pregunta> preguntas = new ArrayList<>();
}
