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

public class UpdateUserTest extends BaseTest {
    private User user;
    private String accessToken;

    @Before
    @Step("Создание тестового пользователя")
    public void createTestUser() {
        user = new User(
            "update" + System.currentTimeMillis() + "@test.com",
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
    @Description("Изменение данных авторизованного пользователя")
    public void updateUserWithAuthTest() {
        User updatedUser = new User(
            "updated" + System.currentTimeMillis() + "@test.com",
            "newpassword123",
            "UpdatedUser"
        );

        Response response = updateUserWithAuth(updatedUser);

        response.then()
            .statusCode(200)
            .body("success", equalTo(true))
            .body("user.email", equalTo(updatedUser.getEmail()))
            .body("user.name", equalTo(updatedUser.getName()));
    }

    @Test
    @Description("Попытка изменения данных без авторизации")
    public void updateUserWithoutAuthTest() {
        User updatedUser = new User(
            "updated" + System.currentTimeMillis() + "@test.com",
            "newpassword123",
            "UpdatedUser"
        );

        Response response = updateUserWithoutAuth(updatedUser);

        response.then()
            .statusCode(401)
            .body("success", equalTo(false))
            .body("message", equalTo("You should be authorised"));
    }

    @Step("Изменение данных пользователя с авторизацией")
    private Response updateUserWithAuth(User user) {
        return given()
            .spec(requestSpec)
            .header("Authorization", accessToken)
            .body(user)
            .log().all()
            .when()
            .patch(ApiConstants.USER_PATH)
            .then()
            .log().all()
            .extract()
            .response();
    }

    @Step("Попытка изменения данных пользователя без авторизации")
    private Response updateUserWithoutAuth(User user) {
        return given()
            .spec(requestSpec)
            .body(user)
            .log().all()
            .when()
            .patch(ApiConstants.USER_PATH)
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