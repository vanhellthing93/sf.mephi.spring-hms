package sf.mephi.booking;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
@ComponentScan(basePackages = {
        "sf.mephi.booking",
        "sf.mephi.common"
})
public class BookingServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(BookingServiceApplication.class, args);
    }
}
