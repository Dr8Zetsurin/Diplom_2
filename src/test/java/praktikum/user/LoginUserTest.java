package praktikum.user;

import io.qameta.allure.*;
import io.restassured.response.Response;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import praktikum.BaseTest;
import praktikum.constants.ApiConstants;
import praktikum.model.User;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

public class LoginUserTest extends BaseTest {
    private User user;
    private String accessToken;

    @Before
    @Step("Создание тестового пользователя")
    public void createTestUser() {
        user = new User(
            "login" + System.currentTimeMillis() + "@test.com",
            "password123",
            "TestUser"
        );
        Response response = given()
            .spec(requestSpec)
            .body(user)
            .when()
            .post(ApiConstants.CREATE_USER_PATH);
        
        accessToken = response.path("accessToken");
    }

    @Test
    @Description("Логин с правильными учетными данными")
    public void loginWithValidCredentialsTest() {
        Response response = loginUser(user);

        response.then()
            .statusCode(200)
            .body("success", equalTo(true))
            .body("accessToken", notNullValue())
            .body("user.email", equalTo(user.getEmail()))
            .body("user.name", equalTo(user.getName()));
    }

    @Test
    @Description("Логин с неправильным паролем")
    public void loginWithInvalidCredentialsTest() {
        User invalidUser = new User(user.getEmail(), "wrongpassword", user.getName());
        
        Response response = loginUser(invalidUser);

        response.then()
            .statusCode(401)
            .body("success", equalTo(false))
            .body("message", equalTo("email or password are incorrect"));
    }

    @Step("Логин пользователя с email: {user.email}")
    private Response loginUser(User user) {
        return given()
            .spec(requestSpec)
            .body(user)
            .log().all()
            .when()
            .post(ApiConstants.LOGIN_USER_PATH)
            .then()
            .log().all()
            .extract()
            .response();
    }

    @After
    @Step("Удаление тестового пользователя")
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