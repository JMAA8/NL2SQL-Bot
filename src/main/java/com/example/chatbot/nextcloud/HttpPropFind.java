package com.example.chatbot.nextcloud;

import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;

import java.net.URI;

public class HttpPropFind extends HttpEntityEnclosingRequestBase {
    public static final String METHOD_NAME = "PROPFIND";

    public HttpPropFind(final String uri) {
        super();
        setURI(URI.create(uri));
    }

    @Override
    public String getMethod() {
        return METHOD_NAME;
    }
}
