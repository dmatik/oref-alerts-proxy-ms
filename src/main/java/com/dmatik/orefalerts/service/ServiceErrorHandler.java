package com.dmatik.orefalerts.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.ResponseErrorHandler;

import java.io.IOException;
import java.net.URI;

@Slf4j
public class ServiceErrorHandler implements ResponseErrorHandler {

    @Override
    public boolean hasError(ClientHttpResponse response) throws IOException {
        HttpStatusCode statusCode = response.getStatusCode();
        return statusCode.is4xxClientError() || statusCode.is5xxServerError();
    }

    @Override
    public void handleError(URI url, HttpMethod method, ClientHttpResponse response) throws IOException {
        HttpStatusCode statusCode = response.getStatusCode();

        if (statusCode.is5xxServerError()) {
            // handle 5xx errors
            // raw http status code e.g `500`
            log.error("Server error");
            log.error(String.valueOf(statusCode.value()));

            // http status code e.g. `500 INTERNAL_SERVER_ERROR`
            log.error(String.valueOf(statusCode));

        } else if (statusCode.is4xxClientError()) {
            // handle 4xx errors
            // raw http status code e.g `404`
            log.error("Client error");
            log.error(String.valueOf(statusCode.value()));

            // http status code e.g. `404 NOT_FOUND`
            log.error(String.valueOf(statusCode));

            // get response body
            log.error(String.valueOf(response.getBody()));

            if (statusCode.isSameCodeAs(HttpStatus.NOT_FOUND)) {
                log.error("HTTP not found");
            }
        }
    }
}
