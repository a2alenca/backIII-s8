package cl.banco.xyz.batch.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.SkipListener;
import org.springframework.stereotype.Component;

@Component
public class CustomSkipListener implements SkipListener<Object, Object> {
    private static final Logger log = LoggerFactory.getLogger(CustomSkipListener.class);

    @Override
    public void onSkipInRead(Throwable t) {
        log.warn("[SKIP LECTURA] Registro omitido: {}", t.getMessage());
    }

    @Override
    public void onSkipInProcess(Object item, Throwable t) {
        log.warn("[SKIP PROCESO] Registro omitido: {} | Causa: {}", item, t.getMessage());
    }

    @Override
    public void onSkipInWrite(Object item, Throwable t) {
        log.error("[SKIP EN ESCRITURA] Error al persistir registro: {} | Causa: {}", item, t.getMessage());
    }
}