import { defineConfig } from "vite";
import react from "@vitejs/plugin-react";

function chenileProxyPlugin() {
  return {
    name: "chenile-dev-proxy",
    configureServer(server) {
      server.middlewares.use("/__chenile_proxy", async (req, res) => {
        try {
          const requestUrl = new URL(req.url, "http://localhost");
          const target = requestUrl.searchParams.get("target");
          const path = requestUrl.searchParams.get("path");

          if (!target || !path) {
            res.statusCode = 400;
            res.setHeader("Content-Type", "application/json");
            res.end(JSON.stringify({ error: "Both target and path are required." }));
            return;
          }

          const targetUrl = new URL(path, target.endsWith("/") ? target : `${target}/`);
          const headers = {};
          if (req.headers["chenile-trajectory-id"]) {
            headers["chenile-trajectory-id"] = req.headers["chenile-trajectory-id"];
          }

          const response = await fetch(targetUrl, {
            method: "GET",
            headers
          });

          res.statusCode = response.status;
          const contentType = response.headers.get("content-type");
          if (contentType) {
            res.setHeader("Content-Type", contentType);
          }
          res.end(await response.text());
        } catch (error) {
          res.statusCode = 502;
          res.setHeader("Content-Type", "application/json");
          res.end(
            JSON.stringify({
              error: error instanceof Error ? error.message : "Proxy request failed."
            })
          );
        }
      });
    }
  };
}

export default defineConfig({
  plugins: [react(), chenileProxyPlugin()],
  server: {
    port: 4173
  }
});
