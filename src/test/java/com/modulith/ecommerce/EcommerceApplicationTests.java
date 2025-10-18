package com.modulith.ecommerce;

import com.modulith.ecommerce.user.UserModuleAPI;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.modulith.core.ApplicationModules;
import org.springframework.modulith.docs.Documenter;

@SpringBootTest
class EcommerceApplicationTests {
    private UserModuleAPI userModuleAPI;

	@Test
	void contextLoads() {
	}

    @Test
    void shouldGenerateModularityDocumentation() {
        ApplicationModules modules = ApplicationModules.of(EcommerceApplication.class).verify();

        new Documenter(modules)
                .writeDocumentation()
                .writeIndividualModulesAsPlantUml()
                .writeModulesAsPlantUml();
    }

}
