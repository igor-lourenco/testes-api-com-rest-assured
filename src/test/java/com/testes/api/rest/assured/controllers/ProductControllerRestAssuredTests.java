package com.testes.api.rest.assured.controllers;


import com.testes.api.rest.assured.utils.TokenUtil;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

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
                    System.out.println(">>> TESTE: Busca todos produtos páginados: GET " + endpoint);
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


    @Test //  <findAllPagedProductProjection> deve <RetornarProductDTO> [quando <ParamNameNaoEhEmpty>]
    public void findAllPagedProductProjectionShouldReturnProductDTOWhenParamNameIsDoesNotEmpty() {

        String endpoint = "/v1/products/page/projections?page=0&name={productName}&size=5&sort=id,ASC";

        RestAssured
            .given()
                .filter((request, response, ctx) -> {
                    System.out.println(">>> TESTE: Busca todos produtos páginados: GET " + endpoint);
                    return ctx.next(request, response);
                })
                .log().all() // log da requisição
            .when()
                .get(endpoint, productName)
            .then()
                .log().all()  // log da resposta
                .statusCode(200)
                .body("content.id[0]", is(4))
                .body("content.id[1]", is(13))
                .body("content.name[0]", containsString(productName))
                .body("content.name[1]", containsString(productName))
        ;
    }


    @Test //  <findAll> deve <ConterProductDTOComPrecoEhMaiorQue2000> [quando <>]
    public void findAllShouldContainsProductDTOWithPriceGreaterThan2000() {

        String endpoint = "/v1/products";

        RestAssured
            .given()
                .filter((request, response, ctx) -> {
                    System.out.println(">>> TESTE: Busca todos produtos: GET " + endpoint);
                    return ctx.next(request, response);
                })
                .log().all() // log da requisição
            .when()
                .get(endpoint)
            .then()
                .log().all()  // log da resposta
                .statusCode(200)
                .body("findAll { it.price > 2000 }.name"  // método do RestAssured que filtra todos que tem o price maior que 2000 e retorna apenas os name dos products filtrados
                    , hasItems("MacBook Pro", "Dell XPS"))
        ;
    }


    @Test //  <findAllPaged> deve <ConterProductDTOComPrecoEhMaiorQue1000> [quando <ParamsPageEhZeroAndSizeEh5>]
    public void findAllPagedShouldContainsProductDTOWithPriceGreaterThan1000WhenParamsPageIsZeroAndSizeIs5() {

        String endpoint = "/v1/products/page?page=0&size=5&sort=id,ASC";

        RestAssured
            .given()
                .filter((request, response, ctx) -> {
                    System.out.println(">>> TESTE: Busca todos produtos páginados: GET " + endpoint);
                    return ctx.next(request, response);
                })
                .log().all() // log da requisição
            .when()
                .get(endpoint)
            .then()
                .log().all()  // log da resposta
                .statusCode(200)
                .body("content.findAll { it.price > 1000 }.name"  // método do RestAssured que filtra todos que tem o price maior que 2000 e retorna apenas os name dos products filtrados
                    , hasItems("Refrigerator", "Washing Machine"))
        ;
    }



    /* Inserção de produto quando logado como admin*/
    @Test //  <insert> deve <RetornarProductCriado> [quando <LogadoComoAdmin>]
    public void insertShouldReturnProductCreatedWhenLoggedInAsAdmin() throws Exception{

        String endpoint = "/v1/products";
        String token = TokenUtil.obtainAccessToken(adminUsername, adminPassword);

        JSONObject newProduct = new JSONObject()
            .put("name", "Desktop PC Pro")
            .put("description", "High-end gaming desktop with RTX GPU")
            .put("price", 8500.0)
            .put("imgUrl", "https://example.com")
            .put("categories", new JSONArray()
                .put(new JSONObject()
                    .put("id", 10))
                .put(new JSONObject()
                    .put("id", 8))
            );

        RestAssured
            .given()
            .filter((request, response, ctx) -> {
                System.out.println(">>> TESTE: Insere novo produto: POST " + endpoint);
                return ctx.next(request, response);
            })
            .log().all() // log da requisição
        .header("Content-type", MediaType.APPLICATION_JSON)
        .header("Authorization", "Bearer " + token)
        .body(newProduct.toString())
        .contentType(ContentType.JSON)
        .accept(ContentType.JSON)
        .when()
            .post(endpoint)
        .then()
            .log().all()  // log da resposta
            .statusCode(201)
            .body("name", equalTo(newProduct.getString("name")))
            .body("categories.id", hasItems(10, 8))
        ;
    }



    /* Inserção de produto retorna 422 e mensagem customizada com dados inválidos quando logado como admin e campo 'name' for inválido  */
    @Test //  <insert> deve <RetornarStatusCode422> [quando <LogadoComoAdminENameInvalido>]
    public void insertShouldReturnStatusCode422WhenLoggedInAsAdminAndInvalidName() throws Exception{

        String endpoint = "/v1/products";
        String token = TokenUtil.obtainAccessToken(adminUsername, adminPassword);

        JSONObject newProduct = new JSONObject()
            .put("name", "Des") // Campo 'name' deve ter entre 5 e 60 caracteres
            .put("description", "High-end gaming desktop with RTX GPU")
            .put("price", 8500.0)
            .put("imgUrl", "https://example.com")
            .put("categories", new JSONArray()
                .put(new JSONObject()
                    .put("id", 10))
                .put(new JSONObject()
                    .put("id", 8))
            );

        RestAssured
            .given()
            .filter((request, response, ctx) -> {
                System.out.println(">>> TESTE: Insere novo produto retorna erro quando campo 'name' for inválido: POST " + endpoint);
                return ctx.next(request, response);
            })
            .log().all() // log da requisição
        .header("Content-type", MediaType.APPLICATION_JSON)
        .header("Authorization", "Bearer " + token)
        .body(newProduct.toString())
        .contentType(ContentType.JSON)
        .accept(ContentType.JSON)
        .when()
            .post(endpoint)
        .then()
            .log().all()  // log da resposta
            .statusCode(422)
            .body("errors.message[0]", equalTo("Campo 'name' deve ter entre 5 e 60 caracteres"))
        ;
    }

}