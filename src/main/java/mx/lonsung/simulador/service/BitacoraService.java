package mx.lonsung.simulador.service;

import mx.lonsung.simulador.entity.Bitacora;
import mx.lonsung.simulador.repository.BitacoraRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BitacoraService {

    private final BitacoraRepository bitacoraRepository;

    public BitacoraService(BitacoraRepository bitacoraRepository) {
        this.bitacoraRepository = bitacoraRepository;
    }

    @Transactional
    public void registrarAcceso(String usuario, String ip, String accion, String userAgent) {
        Bitacora bitacora = new Bitacora();
        bitacora.setFechaHora(java.time.LocalDateTime.now());
        bitacora.setUsuario(usuario != null ? usuario : "anonimo");
        bitacora.setIp(ip);
        bitacora.setAccion(accion != null ? accion : "ACCESO");
        bitacora.setUserAgent(userAgent != null && userAgent.length() > 500 ? userAgent.substring(0, 500) : userAgent);
        bitacoraRepository.save(bitacora);
    }
}
