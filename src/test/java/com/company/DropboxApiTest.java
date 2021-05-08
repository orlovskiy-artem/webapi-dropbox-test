package com.company;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.json.simple.JSONObject;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

import static io.restassured.RestAssured.given;
import static io.restassured.config.EncoderConfig.encoderConfig;
import static org.junit.jupiter.api.Assertions.assertEquals;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class DropboxApiTest {
    private static final String TOKEN = "cyBXcK24C48AAAAAAAAAAYhp1BM_KLTUY4MRr-74NXAIWRHKerQjd0nV3q7cAiRa";
    private static final String DATA_FILENAME = "TestFileToTransfer.txt";
    private static final String PATH_LOCAL_DATA_FILENAME = "./data/TestFileToTransfer.txt";
    private static final String PATH_CLOUD_DATA_FILENAME = "/TestFileToTransfer.txt";

    @Test
    @Order(1)
    public void UploadTest() throws IOException {
        byte[] data = Files.readAllBytes(Paths.get(PATH_LOCAL_DATA_FILENAME));
        Response response = given().
                config(RestAssured.config().
                        encoderConfig(encoderConfig().appendDefaultContentCharsetToContentTypeIfUndefined(false))).
                header("Authorization", "Bearer "+TOKEN).
                header("Dropbox-API-Arg",
                        "{\"mode\": \"add\"," +
                         "\"autorename\": true," +
                         "\"mute\": false," +
                         "\"path\":" +"\"" + PATH_CLOUD_DATA_FILENAME +"\"," +
                         "\"strict_conflict\": false}").
                header("Content-Type", "application/octet-stream").
                body(data).
                when().
                post("https://content.dropboxapi.com/2/files/upload");
    }

    @Test
    @Order(2)
    public void GetFileMetadataTest() {
//        Get File id first
        JSONObject requestBodyForGettingId = new JSONObject();
        requestBodyForGettingId.put("path", PATH_CLOUD_DATA_FILENAME);
        requestBodyForGettingId.put("include_media_info", false);
        requestBodyForGettingId.put("include_deleted", false);
        requestBodyForGettingId.put("include_has_explicit_shared_members", false);
        Response responseWithId = RestAssured.given().
                config(RestAssured.config().
                        encoderConfig(encoderConfig().appendDefaultContentCharsetToContentTypeIfUndefined(false))).
                header("Authorization", "Bearer " + TOKEN).
                header("Content-Type", "application/json").
                body(requestBodyForGettingId.toJSONString()).
                when().
                post("https://api.dropboxapi.com/2/files/get_metadata");
        String fileId= responseWithId.jsonPath().get("id");

        JSONObject requestBody = new JSONObject();
        requestBody.put("file", fileId);
        requestBody.put("actions", new ArrayList());
        Response response = RestAssured.given().
                config(RestAssured.config().
                        encoderConfig(encoderConfig().appendDefaultContentCharsetToContentTypeIfUndefined(false))).
                header("Authorization", "Bearer "+TOKEN).
                header("Content-Type", "application/json").
                body(requestBody.toJSONString()).
                when().
                post("https://api.dropboxapi.com/2/sharing/get_file_metadata");
        assertEquals(200, response.getStatusCode());
    }

    @Test
    @Order(3)
    public void DeleteFileTest() {
        JSONObject requestBody = new JSONObject();
        requestBody.put("path", PATH_CLOUD_DATA_FILENAME);
        Response response = given().
                config(RestAssured.config().
                        encoderConfig(encoderConfig().appendDefaultContentCharsetToContentTypeIfUndefined(false))).
                header("Authorization", "Bearer " + TOKEN).
                header("Content-Type", "application/json").
                body(requestBody.toJSONString()).
                when().
                post("https://api.dropboxapi.com/2/files/delete_v2");
        assertEquals(200, response.getStatusCode());

    }
}