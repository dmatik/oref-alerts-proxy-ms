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
public class CurrentAlertHttpRequestInterceptor implements ClientHttpRequestInterceptor {

    @Override
    public ClientHttpResponse intercept(HttpRequest request,
                                        byte[] body,
                                        ClientHttpRequestExecution execution) throws IOException {

        ClientHttpResponse response = execution.execute(request, body);
        response = new CurrentBufferedClientHttpResponse(response);

        InputStream responseBody = response.getBody();

        JSONObject jsonObject;

        try {
            // Convert InputStream to String
            ByteArrayOutputStream result = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            for (int length; (length = responseBody.read(buffer)) != -1; ) {
                result.write(buffer, 0, length);
            }
            String responseBodyString = result.toString(StandardCharsets.UTF_8);
            log.debug("Current Alert Stream: " + responseBodyString);

            // Remove wrong characters from response to be able to parse to JSON.
            responseBodyString=responseBodyString.replaceAll("[\r\n\u0001\0\\x00-\\x09\\x11\\x12\\x14-\\x1F\\x7F\\x0B\\x0C\\x0E-\\x1F]","");

            // Parse to JSON
            jsonObject = new JSONObject(responseBodyString);
            log.info("Current Alert JSON: " + jsonObject);

            response = new GoodCurrentBufferedClientHttpResponse(response, responseBodyString);

        } catch (JSONException e) {
            // Response could not be parsed as JSON.
            log.debug("Could not parse Current Alert as JSON. Setting to empty response.");
            // Setting to empty response
            response = new EmptyCurrentBufferedClientHttpResponse(response);
        }

        return response;
    }

    /**
     * Wrapper around ClientHttpResponse, buffers the body so it can be read repeatedly (for logging & consuming the result).
     */
    private static class CurrentBufferedClientHttpResponse implements ClientHttpResponse {

        private final ClientHttpResponse response;
        private byte[] body;

        public CurrentBufferedClientHttpResponse(ClientHttpResponse response) {
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
    private static class EmptyCurrentBufferedClientHttpResponse implements ClientHttpResponse {

        private final ClientHttpResponse response;

        public EmptyCurrentBufferedClientHttpResponse(ClientHttpResponse response) {
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
        public InputStream getBody() {
            String emptyJson = "{\"id\": \"\",\"cat\": \"\",\"title\": \"EMPTY_RESPONSE\",\"data\": null,\"desc\": \"\"}";
            return new ByteArrayInputStream(emptyJson.getBytes());
        }

        @Override
        public HttpHeaders getHeaders() {
            return response.getHeaders();
        }
    }

    /**
     * Wrapper around ClientHttpResponse, with good response.
     */
    private static class GoodCurrentBufferedClientHttpResponse implements ClientHttpResponse {

        private final ClientHttpResponse response;

        private final String body;

        public GoodCurrentBufferedClientHttpResponse(ClientHttpResponse response, String body) {
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
        public InputStream getBody() {
            return new ByteArrayInputStream(this.body.getBytes());
        }

        @Override
        public HttpHeaders getHeaders() {
            return response.getHeaders();
        }
    }

}
