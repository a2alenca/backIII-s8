package cl.duoc.bff.web.client;

import cl.duoc.bff.web.dto.CuentaBancariaDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.web.client.RestTemplate;

@Component
public class CoreBankClient {

    private final RestTemplate restTemplate;

    @Value("${core.bank.url}")
    private String coreUrl;

    public CoreBankClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @CircuitBreaker(name = "coreBank", fallbackMethod = "fallbackCuentas")
    public Object[] obtenerCuentasWeb() {
        // Apunta a la ruta real del Core del Banco
        String url = coreUrl + "/internal/api/accounts";
        return restTemplate.getForObject(url, Object[].class);
    }

    @CircuitBreaker(name = "coreBank", fallbackMethod = "fallbackCuenta")
    public Object obtenerCuentaPorId(Long id) {
        // Apunta a la ruta real de detalle por ID del Core del Banco
        String url = coreUrl + "/internal/api/accounts/" + id;
        return restTemplate.getForObject(url, Object.class);
    }

    private Object[] fallbackCuentas(Exception ex) {
        return new Object[0];
    }

    private Object fallbackCuenta(Long id, Exception ex) {
        return java.util.Map.of(
                "error", "CORE_BANK_NO_DISPONIBLE",
                "mensaje", "El servicio bancario no está disponible temporalmente",
                "cuentaId", id
        );
    }
}