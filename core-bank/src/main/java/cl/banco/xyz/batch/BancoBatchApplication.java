package cl.banco.xyz.batch;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class BancoBatchApplication {

    public static void main(String[] args) {
        SpringApplication.run(BancoBatchApplication.class, args);
    }

    @Bean
    CommandLineRunner launchJobs(
            JobLauncher jobLauncher,
            @Qualifier("reporteTransaccionesJob") Job reporteTransaccionesJob,
            @Qualifier("calculoInteresesJob") Job calculoInteresesJob,
            @Qualifier("estadosCuentaJob") Job estadosCuentaJob) {

        return args -> {
            long timestamp = System.currentTimeMillis();

            System.out.println(">>> [1/3] Reporte de Transacciones Diarias");
            jobLauncher.run(reporteTransaccionesJob, new JobParametersBuilder()
                    .addLong("time", timestamp)
                    .addString("jobName", "reporteTransaccionesJob")
                    .toJobParameters());

            System.out.println(">>> [2/3] Calculo de Intereses Mensuales");
            jobLauncher.run(calculoInteresesJob, new JobParametersBuilder()
                    .addLong("time", timestamp + 1)
                    .addString("jobName", "calculoInteresesJob")
                    .toJobParameters());

            System.out.println(">>> [3/3] Generacion de Estados de Cuenta Anuales");
            jobLauncher.run(estadosCuentaJob, new JobParametersBuilder()
                    .addLong("time", timestamp + 2)
                    .addString("jobName", "estadosCuentaJob")
                    .toJobParameters());
        };
    }
}
