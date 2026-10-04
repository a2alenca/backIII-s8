package cl.banco.xyz.batch.policy;

import cl.banco.xyz.batch.exception.InvalidDataException;
import org.springframework.batch.core.step.skip.SkipPolicy;
import org.springframework.batch.item.file.FlatFileParseException;
import org.springframework.stereotype.Component;

@Component
public class BankSkipPolicy implements SkipPolicy {
    private static final long MAX_SKIPS = 100;

    @Override
    public boolean shouldSkip(Throwable t, long skipCount) {
        if (skipCount >= MAX_SKIPS) {
            return false;
        }
        return (t instanceof InvalidDataException) || (t instanceof FlatFileParseException);
    }
}