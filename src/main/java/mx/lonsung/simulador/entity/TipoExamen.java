package mx.lonsung.simulador.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tipo_examen")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TipoExamen {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_examen")
    private Long idExamen;

    @Column(name = "descripcion", length = 255, nullable = false)
    private String descripcion;

    @OneToMany(mappedBy = "tipoExamen", fetch = FetchType.LAZY)
    private List<Pregunta> preguntas = new ArrayList<>();
}
