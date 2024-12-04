package com.dmatik.orefalerts.service;

import lombok.extern.slf4j.Slf4j;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@Slf4j
@Component
public class HistoryHttpRequestInterceptor implements ClientHttpRequestInterceptor {


    @Override
    public ClientHttpResponse intercept(HttpRequest request,
                                        byte[] body,
                                        ClientHttpRequestExecution execution) throws IOException {

        ClientHttpResponse response = execution.execute(request, body);
        response = new HistoryBufferedClientHttpResponse(response);

        InputStream responseBody = response.getBody();

        JSONObject jsonObject;

        // Convert InputStream to String
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        for (int length; (length = responseBody.read(buffer)) != -1; ) {
            result.write(buffer, 0, length);
        }
        String responseBodyString = result.toString(StandardCharsets.UTF_8);

        // Remove wrong characters from response to be able to parse to JSON.
        String responseBodyStringClean =
                responseBodyString.replaceAll("[\r\n\t\u0001\0\\x00-\\x09\\x11\\x12\\x14-\\x1F\\x7F\\x0B\\x0C\\x0E-\\x1F\\u00a0]","");


        // Checking if there is "{" in the response.
        int i = responseBodyStringClean.indexOf("[");

        if (i > -1) {
            responseBodyStringClean = responseBodyStringClean.substring(i);
            response = new HistoryHttpRequestInterceptor.GoodHistoryBufferedClientHttpResponse(response, responseBodyStringClean);
        } else {
            // Empty response.
            log.debug("History Stream: " + responseBodyString);
            log.debug("History is not valid JSON. Setting to empty response.");
            // Setting to empty response
            response = new HistoryHttpRequestInterceptor.EmptyHistoryBufferedClientHttpResponse(response);
        }

        return response;
    }

    /**
     * Wrapper around ClientHttpResponse, buffers the body so it can be read repeatedly (for logging & consuming the result).
     */
    private static class HistoryBufferedClientHttpResponse implements ClientHttpResponse {

        private final ClientHttpResponse response;
        private byte[] body;

        public HistoryBufferedClientHttpResponse(ClientHttpResponse response) {
            this.response = response;
        }

        @Override
        public HttpStatus getStatusCode() throws IOException {
            return response.getStatusCode();
        }

        @Override
        public int getRawStatusCode() throws IOException {
            return response.getRawStatusCode();
        }

        @Override
        public String getStatusText() throws IOException {
            return response.getStatusText();
        }

        @Override
        public void close() {
            response.close();
        }

        @Override
        public InputStream getBody() throws IOException {
            if (body == null) {
                body = StreamUtils.copyToByteArray(response.getBody());
            }
            return new ByteArrayInputStream(body);
        }

        @Override
        public HttpHeaders getHeaders() {
            return response.getHeaders();
        }
    }

    /**
     * Wrapper around ClientHttpResponse, with EMPTY_RESPONSE as title.
     */
    private static class EmptyHistoryBufferedClientHttpResponse implements ClientHttpResponse {

        private final ClientHttpResponse response;
        private byte[] body;

        public EmptyHistoryBufferedClientHttpResponse(ClientHttpResponse response) {
            this.response = response;
        }

        @Override
        public HttpStatus getStatusCode() throws IOException {
            return response.getStatusCode();
        }

        @Override
        public int getRawStatusCode() throws IOException {
            return response.getRawStatusCode();
        }

        @Override
        public String getStatusText() throws IOException {
            return response.getStatusText();
        }

        @Override
        public void close() {
            response.close();
        }

        @Override
        public InputStream getBody() throws IOException {
            return null;
        }

        @Override
        public HttpHeaders getHeaders() {
            return response.getHeaders();
        }
    }

    /**
     * Wrapper around ClientHttpResponse, with good response.
     */
    private static class GoodHistoryBufferedClientHttpResponse implements ClientHttpResponse {

        private final ClientHttpResponse response;
        private String body;

        public GoodHistoryBufferedClientHttpResponse(ClientHttpResponse response, String body) {
            this.response = response;
            this.body = body;
        }

        @Override
        public HttpStatus getStatusCode() throws IOException {
            return response.getStatusCode();
        }

        @Override
        public int getRawStatusCode() throws IOException {
            return response.getRawStatusCode();
        }

        @Override
        public String getStatusText() throws IOException {
            return response.getStatusText();
        }

        @Override
        public void close() {
            response.close();
        }

        @Override
        public InputStream getBody() throws IOException {
            return new ByteArrayInputStream(this.body.getBytes());
        }

        @Override
        public HttpHeaders getHeaders() {
            return response.getHeaders();
        }
    }
}
