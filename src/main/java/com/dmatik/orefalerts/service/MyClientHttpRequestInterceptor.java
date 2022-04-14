package com.dmatik.orefalerts.service;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
public class MyClientHttpRequestInterceptor implements ClientHttpRequestInterceptor {

    private static FileWriter file;

    @Override
    public ClientHttpResponse intercept(HttpRequest request,
                                        byte[] body,
                                        ClientHttpRequestExecution execution) throws IOException {

        ClientHttpResponse response = execution.execute(request, body);
        response = new BufferedClientHttpResponse(response);

        InputStream responseBody = response.getBody();

        JSONObject jsonObject;

        try {
            jsonObject = new JSONObject(new JSONTokener(responseBody));

            // Response parsed as JSON successfully. Saving as files.
            String envSaveToFiles = System.getenv("SAVE_ALERTS_TO_FILES");
            if ( "TRUE".equals(envSaveToFiles) || "true".equals(envSaveToFiles) ) {
                // Java object to JSON file
                DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
                LocalDateTime now = LocalDateTime.now();
                file = new FileWriter("alerts/" + dtf.format(now) + ".json");
                file.write(jsonObject.toString());
            }

        } catch (JSONException e) {
            // Response could not be parsed as JSON.
        } catch (IOException e) {
            // Do Nothing
        } finally {
            if (null != file) {
                file.flush();
                file.close();
            }
        }

        return response;
    }

    /**
     * Wrapper around ClientHttpResponse, buffers the body so it can be read repeatedly (for logging & consuming the result).
     */
    private static class BufferedClientHttpResponse implements ClientHttpResponse {

        private final ClientHttpResponse response;
        private byte[] body;

        public BufferedClientHttpResponse(ClientHttpResponse response) {
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
