package praktikum.user;

import io.qameta.allure.*;
import io.restassured.response.Response;
import org.junit.After;
import org.junit.Test;
import praktikum.BaseTest;
import praktikum.constants.ApiConstants;
import praktikum.model.User;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

public class CreateUserTest extends BaseTest {
    private User user;
    private String accessToken;

    @Test
    @Description("Создание уникального пользователя")
    public void createUniqueUserTest() {
        user = new User(
            "test" + System.currentTimeMillis() + "@test.com",
            "password123",
            "TestUser"
        );

        Response response = createUser(user);

        response.then()
            .statusCode(200)
            .body("success", equalTo(true))
            .body("accessToken", notNullValue())
            .body("user", notNullValue());

        accessToken = response.path("accessToken");
    }

    @Test
    @Description("Создание пользователя с существующим email")
    public void createDuplicateUserTest() {
        user = new User(
            "duplicate" + System.currentTimeMillis() + "@test.com",
            "password123",
            "TestUser"
        );
        createUser(user);

        Response response = createUser(user);

        response.then()
            .statusCode(403)
            .body("success", equalTo(false))
            .body("message", equalTo("User already exists"));
    }

    @Test
    @Description("Создание пользователя без обязательного поля email")
    public void createUserWithoutRequiredFieldTest() {
        user = new User(
            null,
            "password123",
            "TestUser"
        );

        Response response = createUser(user);

        response.then()
            .statusCode(403)
            .body("success", equalTo(false))
            .body("message", equalTo("Email, password and name are required fields"));
    }

    @Step("Создание пользователя с email: {user.email}, именем: {user.name}")
    private Response createUser(User user) {
        return given()
            .spec(requestSpec)
            .body(user)
            .log().all()
            .when()
            .post(ApiConstants.CREATE_USER_PATH)
            .then()
            .log().all()
            .extract()
            .response();
    }

    @Step("Удаление тестовых данных после теста")
    @After
    public void tearDown() {
        if (accessToken != null) {
            given()
                .spec(requestSpec)
                .header("Authorization", accessToken)
                .when()
                .delete(ApiConstants.USER_PATH);
        }
    }
} 