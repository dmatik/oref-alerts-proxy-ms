package com.dmatik.orefalerts.service;

import lombok.extern.slf4j.Slf4j;
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

        // Convert InputStream to String
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        for (int length; (length = responseBody.read(buffer)) != -1; ) {
            result.write(buffer, 0, length);
        }
        String responseBodyString = result.toString(StandardCharsets.UTF_8);
        log.debug("History Stream: " + responseBodyString);

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

}
