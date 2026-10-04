package cl.banco.xyz.batch.config;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BatchJobRunner {

    private final JobLauncher jobLauncher;
    private final Job reporteTransaccionesJob;
    private final Job calculoInteresesJob;
    private final Job estadosCuentaJob;

    public BatchJobRunner(JobLauncher jobLauncher,
                          Job reporteTransaccionesJob,
                          Job calculoInteresesJob,
                          Job estadosCuentaJob) {
        this.jobLauncher = jobLauncher;
        this.reporteTransaccionesJob = reporteTransaccionesJob;
        this.calculoInteresesJob = calculoInteresesJob;
        this.estadosCuentaJob = estadosCuentaJob;
    }

    @Bean
    public CommandLineRunner runBatchJobs() {
        return args -> {
            JobParameters params = new JobParametersBuilder()
                    .addLong("time", System.currentTimeMillis())
                    .toJobParameters();

            // Ejecuta los tres procesos secuencialmente al encender la app
            jobLauncher.run(reporteTransaccionesJob, params);
            jobLauncher.run(calculoInteresesJob, params);
            jobLauncher.run(estadosCuentaJob, params);
        };
    }
}