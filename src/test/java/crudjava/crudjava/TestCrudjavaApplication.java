package crudjava.crudjava;

import org.springframework.boot.SpringApplication;

public class TestCrudjavaApplication {

    public static void main(String[] args) {
        SpringApplication.from(CrudjavaApplication::main).with(TestcontainersConfiguration.class).run(args);
    }

}
