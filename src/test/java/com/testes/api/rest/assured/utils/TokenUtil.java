package com.testes.api.rest.assured.utils;

import io.restassured.RestAssured;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;

public class TokenUtil {

//    @Value("${security.teste.client-id:myclientid}") // passando o valor diretamente na anotação
//    private String clientId;
//    @Value("${security.test.client-secret:myclientsecret}") // passando o valor diretamente na anotação
//    private String clientSecret;

    public static String obtainAccessToken(String username, String password) throws Exception {

        String clientId = "myclientid";
        String clientSecret = "myclientsecret";

//        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
//        params.add("grant_type", "client_credentials");
//        params.add("scope", "READ WRITE");

        Response response = RestAssured.given()
            .auth()
            .preemptive()
            .basic(clientId, clientSecret)
            .contentType(MediaType.APPLICATION_FORM_URLENCODED.toString())
            .formParam("grant_type", "client_credentials")
            .formParam("scope", "READ WRITE")
            .header("username", username)
            .header("password", password)
            .when()
            .post("/oauth2/token");

        JsonPath jsonBody = response.jsonPath();
        String token = jsonBody.getString("access_token");
        return token;
    }
}



