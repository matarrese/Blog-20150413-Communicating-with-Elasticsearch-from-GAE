package com.foodit.example.es.http;

import com.foodit.example.es.IndexerService;
import com.foodit.example.util.Properties;
import com.google.api.client.http.ByteArrayContent;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpContent;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Map;

public class HttpBasedIndexerService implements IndexerService {

    private static final String ES_BASE_URL = "http://localhost:9200/" + Properties.INDEX_NAME + "/" + Properties.TYPE_NAME + "/";

    public String indexDocument(String documentId, Map<String, String> properties) {
        JsonObject rootObject = new JsonObject();
        JsonObject jsonDocument = new JsonObject();

        for(Map.Entry<String, String> entry : properties.entrySet()) {
            jsonDocument.addProperty(entry.getKey(), entry.getValue());;
        }
        jsonDocument.addProperty("id", documentId);

        rootObject.add("doc", jsonDocument);
        rootObject.addProperty("doc_as_upsert", true);

        return executeUpdateRequest(documentId, rootObject);
    }

    private String executeUpdateRequest(String documentId, JsonObject rootObject) {
        HttpContent httpContent = new ByteArrayContent("application/json", rootObject.toString().getBytes(Charset.forName("UTF-8")));
        GenericUrl url = new GenericUrl(ES_BASE_URL + documentId + "/_update");

        HttpRequestFactory requestFactory = new NetHttpTransport().createRequestFactory();

        try {
            HttpRequest httpRequest = requestFactory.buildPostRequest(url, httpContent);
            HttpResponse httpResponse = httpRequest.execute();
            return httpResponse.parseAsString();
        } catch (IOException e) {
            throw new RuntimeException("Error updating document in Elastic Search", e);
        }
    }
}
