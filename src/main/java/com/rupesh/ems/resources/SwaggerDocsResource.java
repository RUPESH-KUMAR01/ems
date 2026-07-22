package com.rupesh.ems.resources;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Set;

@Path("/swagger")
public class SwaggerDocsResource {

  private static final Set<String> ROLES = Set.of("admin", "moderator", "user");

  @GET
  @Produces(MediaType.TEXT_HTML)
  public Response getIndexPage() {
    String html =
        """
        <!doctype html>
        <html lang="en">
          <head><meta charset="utf-8"><title>EMS Swagger Docs</title></head>
          <body>
            <h2>EMS Swagger Docs</h2>
            <ul>
              <li><a href="/swagger/admin">Admin API docs</a></li>
              <li><a href="/swagger/moderator">Moderator API docs</a></li>
              <li><a href="/swagger/user">User API docs</a></li>
            </ul>
          </body>
        </html>
        """;
    return Response.ok(html).build();
  }

  @GET
  @Path("/{role}")
  @Produces(MediaType.TEXT_HTML)
  public Response getRoleDocs(@PathParam("role") String role) {
    String normalizedRole = role.toLowerCase();
    if (!ROLES.contains(normalizedRole)) {
      throw new NotFoundException("No swagger docs available for role: " + role);
    }

    String html =
        """
        <!doctype html>
        <html lang="en">
          <head>
            <meta charset="utf-8" />
            <meta name="viewport" content="width=device-width, initial-scale=1" />
            <title>%s API Docs</title>
            <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/swagger-ui-dist@5/swagger-ui.css" />
          </head>
          <body>
            <div id="swagger-ui"></div>
            <script src="https://cdn.jsdelivr.net/npm/swagger-ui-dist@5/swagger-ui-bundle.js"></script>
            <script>
              window.ui = SwaggerUIBundle({
                url: "/swagger/specs/%s.yaml",
                dom_id: "#swagger-ui",
                deepLinking: true
              });
            </script>
          </body>
        </html>
        """
            .formatted(normalizedRole.toUpperCase(), normalizedRole);
    return Response.ok(html).build();
  }

  @GET
  @Path("/specs/{role}.yaml")
  @Produces("application/yaml")
  public Response getRoleSpec(@PathParam("role") String role) {
    String normalizedRole = role.toLowerCase();
    if (!ROLES.contains(normalizedRole)) {
      throw new NotFoundException("No swagger spec available for role: " + role);
    }
    String resourcePath = "swagger/specs/" + normalizedRole + ".yaml";
    return Response.ok(readResource(resourcePath)).type("application/yaml; charset=utf-8").build();
  }

  private String readResource(String path) {
    try (InputStream inputStream =
        Thread.currentThread().getContextClassLoader().getResourceAsStream(path)) {
      if (inputStream == null) {
        throw new NotFoundException("Resource not found: " + path);
      }
      return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
    } catch (IOException e) {
      throw new IllegalStateException("Failed to read swagger resource: " + path, e);
    }
  }
}
