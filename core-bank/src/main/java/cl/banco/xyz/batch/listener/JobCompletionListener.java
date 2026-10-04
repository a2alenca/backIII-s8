package cl.banco.xyz.batch.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.stereotype.Component;

@Component
public class JobCompletionListener implements JobExecutionListener {
    private static final Logger log = LoggerFactory.getLogger(JobCompletionListener.class);

    @Override
    public void beforeJob(JobExecution jobExecution) {
        log.info("========== INICIO JOB: {} (ID: {}) ==========",
                jobExecution.getJobInstance().getJobName(), jobExecution.getId());
    }

    @Override
    public void afterJob(JobExecution jobExecution) {
        if (jobExecution.getStatus() == BatchStatus.COMPLETED) {
            log.info("========== JOB {} FINALIZADO CON EXITO ==========",
                    jobExecution.getJobInstance().getJobName());
        } else {
            log.error("========== JOB {} FINALIZO CON ESTADO: {} ==========",
                    jobExecution.getJobInstance().getJobName(), jobExecution.getStatus());
        }
    }
}