package praktikum.order;

import io.qameta.allure.*;
import io.restassured.response.Response;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import praktikum.BaseTest;
import praktikum.constants.ApiConstants;
import praktikum.model.Order;
import praktikum.model.User;
import java.util.Arrays;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

public class GetUserOrdersTest extends BaseTest {
    private User user;
    private String accessToken;
    private final String VALID_INGREDIENT = "61c0c5a71d1f82001bdaaa6d";

    @Before
    @Step("Подготовка тестовых данных")
    public void setUp() {
        super.setUp();
        user = new User(
            "orders" + System.currentTimeMillis() + "@test.com",
            "password123",
            "TestUser"
        );
        Response response = given()
            .spec(requestSpec)
            .body(user)
            .when()
            .post(ApiConstants.CREATE_USER_PATH);
        
        accessToken = response.path("accessToken");

        Order order = new Order(Arrays.asList(VALID_INGREDIENT));
        given()
            .spec(requestSpec)
            .header("Authorization", accessToken)
            .body(order)
            .when()
            .post(ApiConstants.ORDERS_PATH);
    }

    @Test
    @Description("Получение заказов авторизованного пользователя")
    public void getOrdersWithAuthTest() {
        Response response = getUserOrdersWithAuth();

        response.then()
            .statusCode(200)
            .body("success", equalTo(true))
            .body("orders", notNullValue())
            .body("orders", not(empty()));
    }

    @Test
    @Description("Получение заказов неавторизованного пользователя")
    public void getOrdersWithoutAuthTest() {
        Response response = getUserOrdersWithoutAuth();

        response.then()
            .statusCode(401)
            .body("success", equalTo(false))
            .body("message", equalTo("You should be authorised"));
    }

    @Step("Получение заказов с авторизацией")
    private Response getUserOrdersWithAuth() {
        return given()
            .spec(requestSpec)
            .header("Authorization", accessToken)
            .log().all()
            .when()
            .get(ApiConstants.ORDERS_PATH)
            .then()
            .log().all()
            .extract()
            .response();
    }

    @Step("Получение заказов без авторизации")
    private Response getUserOrdersWithoutAuth() {
        return given()
            .spec(requestSpec)
            .log().all()
            .when()
            .get(ApiConstants.ORDERS_PATH)
            .then()
            .log().all()
            .extract()
            .response();
    }

    @After
    @Step("Удаление тестовых данных")
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