package mx.lonsung.simulador.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "bitacora")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Bitacora {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_bitacora")
    private Long idBitacora;

    @Column(name = "fecha_hora", nullable = false)
    private LocalDateTime fechaHora;

    @Column(name = "usuario", length = 255, nullable = false)
    private String usuario;

    @Column(name = "ip", length = 45)
    private String ip;

    @Column(name = "accion", length = 50, nullable = false)
    private String accion;

    @Column(name = "user_agent", length = 500)
    private String userAgent;
}
