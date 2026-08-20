package com.testes.api.rest.assured.controllers;


import io.restassured.RestAssured;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.baseURI;
import static io.restassured.RestAssured.port;
import static org.hamcrest.Matchers.*;



public class ProductControllerRestAssuredTests {
//    private static final Logger log = LogManager.getLogger(ProductControllerRestAssuredTests.class);


    private long existingId, nonExistingId;
    private String adminUsername, adminPassword;
    private String operatorUsername, operatorPassword;
    private String productName;


    @BeforeEach // Preparação antes de cada teste da classe
    void setUp() throws Exception {
//      Os valores agora têm que ser reais porque vai ser testado o banco de dados

        baseURI = "http://localhost:9200";
        port = 9200;
        existingId = 1L;
        productName = "Ma";

        adminUsername = "maria@gmail.com"; // perfil de admin
        adminPassword = "maria123";

        operatorUsername = "alex@gmail.com"; // perfil de operator
        operatorPassword = "alex123";

    }

//	Nomenclatura de um teste: <AÇÃO> should <EFEITO> [when <CENÁRIO>]

    @Test //  <findById> deve <RetornarProductDTO> [quando <IdExistir>]
    public void findByIdShouldReturnProductDTOWhenIdExists() {

        String endpoint = "/v1/products/{id}";

        RestAssured
            .given()
                .filter((request, response, ctx) -> {
                    System.out.println(">>> TESTE: Busca usuário pelo ID: GET " + endpoint);
                    return ctx.next(request, response);
                })
                .log().all() // log da requisição
            .when()
                .get(endpoint, existingId)
            .then()
                .log().all()  // log da resposta
                .statusCode(200)
                .body("id", is(Integer.valueOf("" + existingId)))
                .body("name", equalTo("Microwave"))
                .body("imgUrl", equalTo("https://example.com/images/products/microwave.png"))
                .body("price", is(450.0f))
                .body("categories.id", hasItems(1))
                .body("categories.name", hasItems("Home appliances"))
        ;
    }


    @Test //  <findAllPaged> deve <RetornarProductDTO> [quando <ParamNameIsEmpty>]
    public void findAllPagedShouldReturnProductDTOWhenParamNameIsEmpty(){

        String endpoint = "/v1/products/page?page=0&size=5&sort=id,ASC";

        RestAssured
            .given()
                .filter((request, response, ctx) -> {
                    System.out.println(">>> TESTE: Busca todos usuários páginados: GET " + endpoint);
                    return ctx.next(request, response);
                })
                .log().all() // log da requisição
            .when()
                .get(endpoint)
        .then()
            .log().all()  // log da resposta
            .statusCode(200)
            .body("content.id", hasItems(1,2))
            .body("content.name", hasItems("Microwave", "Refrigerator"))
        ;
    }
}