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
import java.util.Collections;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

public class CreateOrderTest extends BaseTest {
    private User user;
    private String accessToken;
    private final String VALID_INGREDIENT = "61c0c5a71d1f82001bdaaa6d";
    private final String INVALID_INGREDIENT = "invalid_ingredient_hash";

    @Before
    @Step("Создание тестового пользователя")
    public void createTestUser() {
        user = new User(
            "order" + System.currentTimeMillis() + "@test.com",
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
    @Description("Создание заказа с авторизацией")
    public void createOrderWithAuthTest() {
        Order order = new Order(Arrays.asList(VALID_INGREDIENT));
        Response response = createOrderWithAuth(order);

        response.then()
            .statusCode(200)
            .body("success", equalTo(true))
            .body("order.number", notNullValue());
    }

    @Test
    @Description("Создание заказа без авторизации")
    public void createOrderWithoutAuthTest() {
        Order order = new Order(Arrays.asList(VALID_INGREDIENT));
        Response response = createOrderWithoutAuth(order);

        response.then()
            .statusCode(200)
            .body("success", equalTo(true))
            .body("order.number", notNullValue());
    }

    @Test
    @Description("Создание заказа без ингредиентов")
    public void createOrderWithoutIngredientsTest() {
        Order order = new Order(Collections.emptyList());
        Response response = createOrderWithAuth(order);

        response.then()
            .statusCode(400)
            .body("success", equalTo(false))
            .body("message", equalTo("Ingredient ids must be provided"));
    }

    @Test
    @Description("Создание заказа с неверным хешем ингредиентов")
    public void createOrderWithInvalidIngredientsTest() {
        Order order = new Order(Arrays.asList(INVALID_INGREDIENT));
        Response response = createOrderWithAuth(order);

        response.then()
            .statusCode(500);
    }

    @Step("Создание заказа с авторизацией")
    private Response createOrderWithAuth(Order order) {
        return given()
            .spec(requestSpec)
            .header("Authorization", accessToken)
            .body(order)
            .log().all()
            .when()
            .post(ApiConstants.ORDERS_PATH)
            .then()
            .log().all()
            .extract()
            .response();
    }

    @Step("Создание заказа без авторизации")
    private Response createOrderWithoutAuth(Order order) {
        return given()
            .spec(requestSpec)
            .body(order)
            .log().all()
            .when()
            .post(ApiConstants.ORDERS_PATH)
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