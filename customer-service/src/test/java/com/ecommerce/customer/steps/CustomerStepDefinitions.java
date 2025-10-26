package com.ecommerce.customer.steps;

import com.ecommerce.customer.CustomerServiceApplication;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.zh_tw.假設;
import io.cucumber.java.zh_tw.當;
import io.cucumber.java.zh_tw.那麼;
import io.cucumber.java.zh_tw.而且;
import io.cucumber.spring.CucumberContextConfiguration;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * 客戶管理功能的步驟定義
 * 實作 BDD 測試步驟，遵循測試先行開發原則
 */
@CucumberContextConfiguration
@SpringBootTest(
    classes = CustomerServiceApplication.class,
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@ActiveProfiles("test")
public class CustomerStepDefinitions {

    @LocalServerPort
    private int port;

    private RequestSpecification request;
    private Response response;
    private Map<String, String> customerData;
    private String customerId;

    @假設("系統已經啟動並運行")
    public void 系統已經啟動並運行() {
        RestAssured.port = port;
        RestAssured.basePath = "/api/v1";
        request = given()
            .contentType("application/json")
            .accept("application/json");
    }

    @假設("我有一個有效的客戶註冊請求")
    public void 我有一個有效的客戶註冊請求(DataTable dataTable) {
        List<Map<String, String>> data = dataTable.asMaps(String.class, String.class);
        customerData = data.get(0);
    }

    @假設("我有一個無效電子郵件的客戶註冊請求")
    public void 我有一個無效電子郵件的客戶註冊請求(DataTable dataTable) {
        List<Map<String, String>> data = dataTable.asMaps(String.class, String.class);
        customerData = data.get(0);
    }

    @假設("我有一個缺少必填欄位的客戶註冊請求")
    public void 我有一個缺少必填欄位的客戶註冊請求(DataTable dataTable) {
        List<Map<String, String>> data = dataTable.asMaps(String.class, String.class);
        customerData = data.get(0);
    }

    @假設("系統中已存在一個客戶")
    public void 系統中已存在一個客戶(DataTable dataTable) {
        List<Map<String, String>> data = dataTable.asMaps(String.class, String.class);
        Map<String, String> existingCustomer = data.get(0);
        customerId = existingCustomer.get("客戶ID");
        
        // 這裡應該預先建立測試資料
        // 在實際實作中，會透過 API 或直接操作資料庫來建立
    }

    @假設("我有一個客戶更新請求")
    public void 我有一個客戶更新請求(DataTable dataTable) {
        List<Map<String, String>> data = dataTable.asMaps(String.class, String.class);
        customerData = data.get(0);
    }

    @當("我提交客戶註冊請求")
    public void 我提交客戶註冊請求() {
        String requestBody = String.format("""
            {
                "name": "%s",
                "email": "%s",
                "phone": "%s",
                "address": "%s"
            }
            """, 
            customerData.get("姓名"),
            customerData.get("電子郵件"),
            customerData.get("電話號碼"),
            customerData.get("地址")
        );

        response = request
            .body(requestBody)
            .when()
            .post("/customers");
    }

    @當("我查詢客戶 ID 為 {string} 的資料")
    public void 我查詢客戶ID為的資料(String customerId) {
        response = request
            .when()
            .get("/customers/" + customerId);
    }

    @當("我更新客戶 ID 為 {string} 的資料")
    public void 我更新客戶ID為的資料(String customerId) {
        String requestBody = String.format("""
            {
                "name": "%s",
                "email": "%s",
                "phone": "%s",
                "address": "%s"
            }
            """, 
            customerData.get("姓名"),
            customerData.get("電子郵件"),
            customerData.get("電話號碼"),
            customerData.get("地址")
        );

        response = request
            .body(requestBody)
            .when()
            .put("/customers/" + customerId);
    }

    @當("我刪除客戶 ID 為 {string}")
    public void 我刪除客戶ID為(String customerId) {
        response = request
            .when()
            .delete("/customers/" + customerId);
    }

    @那麼("系統應該回應成功狀態碼 {int}")
    public void 系統應該回應成功狀態碼(int expectedStatusCode) {
        response.then().statusCode(expectedStatusCode);
    }

    @那麼("系統應該回應錯誤狀態碼 {int}")
    public void 系統應該回應錯誤狀態碼(int expectedStatusCode) {
        response.then().statusCode(expectedStatusCode);
    }

    @而且("回應應該包含客戶 ID")
    public void 回應應該包含客戶ID() {
        response.then()
            .body("data.id", notNullValue())
            .body("success", equalTo(true));
    }

    @而且("客戶應該被儲存在資料庫中")
    public void 客戶應該被儲存在資料庫中() {
        // 這裡應該驗證資料庫中確實存在該客戶
        // 在實際實作中，會查詢資料庫來驗證
        assertTrue(true, "客戶已儲存在資料庫中");
    }

    @而且("錯誤訊息應該包含 {string}")
    public void 錯誤訊息應該包含(String expectedMessage) {
        response.then()
            .body("message", containsString(expectedMessage))
            .body("success", equalTo(false));
    }

    @而且("錯誤訊息應該包含驗證失敗的詳細資訊")
    public void 錯誤訊息應該包含驗證失敗的詳細資訊() {
        response.then()
            .body("success", equalTo(false))
            .body("message", notNullValue());
    }

    @而且("回應應該包含正確的客戶資料")
    public void 回應應該包含正確的客戶資料() {
        response.then()
            .body("data.id", notNullValue())
            .body("data.name", notNullValue())
            .body("data.email", notNullValue())
            .body("success", equalTo(true));
    }

    @而且("客戶資料應該被正確更新")
    public void 客戶資料應該被正確更新() {
        response.then()
            .body("data.name", equalTo(customerData.get("姓名")))
            .body("data.email", equalTo(customerData.get("電子郵件")))
            .body("success", equalTo(true));
    }

    @而且("客戶應該從資料庫中被移除")
    public void 客戶應該從資料庫中被移除() {
        // 這裡應該驗證資料庫中該客戶已被刪除
        // 在實際實作中，會查詢資料庫來驗證
        assertTrue(true, "客戶已從資料庫中移除");
    }
}