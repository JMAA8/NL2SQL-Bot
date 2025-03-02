package com.example.chatbot.nextcloud;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/nextcloud")
public class NextCloudController {

    private static final String NEXT_CLOUD_URL = "https://onecloud.torulethemall.org";
    private static final String CLIENT_ID = "YA9x6v4amPuI3BjCas6t45Oz904mqYnQ3Q0JZONQSs7AIHjy7WsG2oBXgPgdn4Aa";
    private static final String CLIENT_SECRET = "fyOEVfc078p2zDrveju9t2mSpCP1OzRzIRAG40ZLryb8Vv1QOR0LaN7AB3H5qSWk";
    private static final String REDIRECT_URI = "https://backend.com/oauth/callback";

    ;

    @GET
    @Path("/login-url")
    @Produces(MediaType.TEXT_PLAIN)
    public String getLoginUrl() {
        return "https://www.youtube.com";
        //return NEXT_CLOUD_URL + "/apps/oauth2/authorize?client_id=" + CLIENT_ID + "&redirect_uri=" + REDIRECT_URI + "&response_type=code";
    }
}
