package com.example.chatbot.nextcloud;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;
import jakarta.inject.Inject;

@Path("/")
public class NextCloudController {

    private static final String NEXT_CLOUD_URL = "https://onecloud.torulethemall.org";
    private static final String CLIENT_ID = "dnNV7QzFoy7bwXHipBvQPigzQ2MrONFGFKw0ke8jns8zDWQmGGDzNWIL7fDxNzWM";
    private static final String CLIENT_SECRET = "l1GbupK7WVIN5Cd4So5rkWoUSWgDAGxZdu7Kg1dMHFm0bJfGEC1DSoWXRcKdKdfl";
    private static final String REDIRECT_URI = "http://localhost:8080/oauth/callback";




    @GET
    @Path("/nextcloud/login-url")
    @Produces(MediaType.TEXT_PLAIN)
    public String getLoginUrl() {
        System.out.println("Login - Nextcloud");
        return NEXT_CLOUD_URL + "/apps/oauth2/authorize?client_id=" + CLIENT_ID +
                "&redirect_uri=" + REDIRECT_URI +
                "&response_type=code" +
                "&state=user123";  // Hier könnte man eine dynamische User-ID setzen
    }

    @GET
    @Path("/oauth/callback")
    @Produces(MediaType.APPLICATION_JSON)
    public Response exchangeCodeForToken(@QueryParam("code") String code, @QueryParam("state") String state) {
        System.out.println("exchangeCodeForToke!");
        if (code == null || code.isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST).entity("{\"error\":\"Code fehlt!\"}").build();
        }

        try (CloseableHttpClient client = HttpClients.createDefault()) {
            String tokenUrl = NEXT_CLOUD_URL + "/apps/oauth2/api/v1/token";

            HttpPost post = new HttpPost(tokenUrl);
            StringEntity entity = new StringEntity("grant_type=authorization_code" +
                    "&code=" + code +
                    "&client_id=" + CLIENT_ID +
                    "&client_secret=" + CLIENT_SECRET +
                    "&redirect_uri=" + REDIRECT_URI);

            post.setEntity(entity);
            post.setHeader("Content-Type", "application/x-www-form-urlencoded");

            try (CloseableHttpResponse response = client.execute(post)) {
                String result = EntityUtils.toString(response.getEntity());

                JSONObject jsonResponse = new JSONObject(result);
                String accessToken = jsonResponse.optString("access_token", null);

                if (accessToken != null) {
                    // 🎯 Speichere das Token für den aktuellen Benutzer (Session-basiert)
                    TokenStorage.saveToken(state, accessToken);  // 'state' = userId oder Session-ID
                    System.out.println("Accesstoken: " + accessToken);

                    return Response.ok("{\"message\": \"Token gespeichert!\"}").build();
                } else {
                    return Response.status(500).entity("{\"error\":\"Kein Access Token erhalten\"}").build();
                }
            }
        } catch (Exception e) {
            return Response.status(500).entity("{\"error\":\"Fehler beim Abrufen des Tokens: " + e.getMessage() + "\"}").build();
        }
    }

}


