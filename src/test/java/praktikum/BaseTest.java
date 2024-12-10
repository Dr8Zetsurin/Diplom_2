package praktikum;

import io.qameta.allure.restassured.AllureRestAssured;
import io.qameta.allure.Step;
import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.specification.RequestSpecification;
import org.junit.Before;
import praktikum.constants.ApiConstants;

public class BaseTest {
    protected RequestSpecification requestSpec;

    @Step("Инициализация базовой конфигурации")
    @Before
    public void setUp() {
        RestAssured.baseURI = ApiConstants.BASE_URL;
        
        requestSpec = new RequestSpecBuilder()
                .addFilter(new AllureRestAssured())
                .setContentType("application/json")
                .setRelaxedHTTPSValidation()
                .build();
    }
} 