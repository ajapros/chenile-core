import { defineConfig } from "vite";
import react from "@vitejs/plugin-react";

function readRequestBody(req) {
  return new Promise((resolve, reject) => {
    const chunks = [];
    req.on("data", (chunk) => chunks.push(chunk));
    req.on("end", () => resolve(chunks.length ? Buffer.concat(chunks) : undefined));
    req.on("error", reject);
  });
}

function proxyHeaders(req) {
  const headers = {};
  for (const [key, value] of Object.entries(req.headers)) {
    if (value === undefined) {
      continue;
    }
    if (
      key === "accept" ||
      key === "content-type" ||
      key === "chenile-trajectory-id" ||
      key.startsWith("x-chenile-")
    ) {
      headers[key] = Array.isArray(value) ? value.join(",") : value;
    }
  }
  return headers;
}

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
          const method = req.method || "GET";
          const body = method === "GET" || method === "HEAD"
            ? undefined
            : await readRequestBody(req);

          const response = await fetch(targetUrl, {
            method,
            headers: proxyHeaders(req),
            body
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
  base: "/chenile/admin/",
  server: {
    port: 4173
  }
});
