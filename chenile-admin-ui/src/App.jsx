import { useEffect, useMemo, useState } from "react";

const DEFAULT_BASE_URL = "http://localhost:8000";
const REGISTRY_SERVICE_ID = "serviceregistryService";
const SWAGGER_CANDIDATE_PATHS = [
  "/swagger-ui/index.html",
  "/swagger-ui.html",
  "/v3/api-docs",
];

function normalizeBaseUrl(input) {
  const trimmed = input.trim();
  if (!trimmed) {
    return "";
  }
  if (/^https?:\/\//i.test(trimmed)) {
    return trimmed.replace(/\/+$/, "");
  }
  return `http://${trimmed.replace(/\/+$/, "")}`;
}

async function fetchJson(url, options = {}) {
  const response = await fetch(url, options);
  const text = await response.text();
  let body = null;
  if (text) {
    body = JSON.parse(text);
  }
  if (!response.ok) {
    const message =
      body?.errors?.[0]?.description ||
      body?.responseMessage?.description ||
      body?.description ||
      `${response.status} ${response.statusText}`;
    throw new Error(message);
  }
  if (body && typeof body === "object" && "payload" in body) {
    return body.payload;
  }
  return body;
}

function shouldUseProxy(baseUrl) {
  if (typeof window === "undefined" || !baseUrl) {
    return false;
  }
  try {
    return window.location.origin !== new URL(baseUrl).origin;
  } catch {
    return false;
  }
}

function buildRequestUrl(baseUrl, path) {
  if (!shouldUseProxy(baseUrl)) {
    return `${baseUrl}${path}`;
  }
  const proxyUrl = new URL("/__chenile_proxy", window.location.origin);
  proxyUrl.searchParams.set("target", baseUrl);
  proxyUrl.searchParams.set("path", path);
  return proxyUrl.toString();
}

async function findSwaggerUrl(baseUrl) {
  for (const path of SWAGGER_CANDIDATE_PATHS) {
    try {
      const response = await fetch(buildRequestUrl(baseUrl, path), {
        method: "GET",
      });
      if (response.ok) {
        return `${baseUrl}${path}`;
      }
    } catch {
      // Ignore probe failures and keep checking other known paths.
    }
  }
  return "";
}

function valueOrDash(value) {
  if (value === null || value === undefined || value === "") {
    return "—";
  }
  if (typeof value === "boolean") {
    return value ? "true" : "false";
  }
  if (Array.isArray(value)) {
    return value.length ? value.join(", ") : "—";
  }
  return String(value);
}

function normalizeLocalService(service) {
  const serviceId = service.id || service.serviceId || service.name;
  return {
    ...service,
    catalog: "local",
    uniqueKey: `local:${serviceId}`,
    lookupName: serviceId,
    displayName: serviceId,
    serviceId,
    serviceVersion: service.version,
    sourceLabel: "This monolith",
  };
}

function normalizeRegistryService(service) {
  const serviceId = service.serviceId || service.id || service.name;
  const serviceVersion = service.serviceVersion || service.version;
  return {
    ...service,
    catalog: "ecosystem",
    uniqueKey: `ecosystem:${serviceId}:${serviceVersion || ""}:${service.baseUrl || ""}`,
    lookupName: serviceId,
    displayName: serviceId,
    id: serviceId,
    name: serviceId,
    version: serviceVersion,
    serviceId,
    serviceVersion,
    sourceLabel: service.baseUrl || service.monolithName || service.moduleName || "Remote monolith",
  };
}

function isRegistryHost(infoPayload) {
  return (infoPayload?.services || []).some((service) => {
    const serviceId = service.id || service.serviceId || service.name;
    return serviceId === REGISTRY_SERVICE_ID;
  });
}

function deriveWorkflowInfoLookupName(service) {
  const lookupName = service?.lookupName || service?.serviceId || service?.id;
  if (!lookupName || lookupName.endsWith("StateEntityInfoService")) {
    return "";
  }
  if (lookupName.endsWith("Service")) {
    return `${lookupName.slice(0, -"Service".length)}StateEntityInfoService`;
  }
  return `${lookupName}StateEntityInfoService`;
}

function resolveWorkflowInfoService(service, services) {
  const infoLookupName = deriveWorkflowInfoLookupName(service);
  if (!infoLookupName) {
    return null;
  }
  return (
    services.find((candidate) => candidate.lookupName === infoLookupName) || null
  );
}

function getOperationUrl(service, suffix) {
  const operation = (service?.operations || []).find((item) =>
    (item.url || "").endsWith(suffix)
  );
  return operation?.url || "";
}

function deriveWorkflowOperationUrl(selectedService, workflowInfoService, suffix) {
  const explicitUrl = getOperationUrl(workflowInfoService, suffix);
  if (explicitUrl) {
    return explicitUrl;
  }
  const lookupName =
    selectedService?.lookupName || selectedService?.serviceId || selectedService?.id || "";
  if (!lookupName) {
    return "";
  }
  const servicePath = lookupName.endsWith("Service")
    ? lookupName.slice(0, -"Service".length)
    : lookupName;
  return `/${servicePath}/info${suffix}`;
}

function App() {
  const [baseUrlInput, setBaseUrlInput] = useState(
    window.localStorage.getItem("chenile-admin-base-url") || DEFAULT_BASE_URL
  );
  const [trajectoryId, setTrajectoryId] = useState("");
  const [baseUrl, setBaseUrl] = useState(normalizeBaseUrl(baseUrlInput));
  const [info, setInfo] = useState(null);
  const [servicesLoading, setServicesLoading] = useState(false);
  const [servicesError, setServicesError] = useState("");
  const [catalogMode, setCatalogMode] = useState("local");
  const [ecosystemServices, setEcosystemServices] = useState([]);
  const [ecosystemLoading, setEcosystemLoading] = useState(false);
  const [ecosystemError, setEcosystemError] = useState("");
  const [selectedServiceName, setSelectedServiceName] = useState("");
  const [selectedService, setSelectedService] = useState(null);
  const [serviceLoading, setServiceLoading] = useState(false);
  const [serviceError, setServiceError] = useState("");
  const [healthInfo, setHealthInfo] = useState(null);
  const [healthLoading, setHealthLoading] = useState(false);
  const [healthError, setHealthError] = useState("");
  const [workflowDiagram, setWorkflowDiagram] = useState("");
  const [workflowDiagramLoading, setWorkflowDiagramLoading] = useState(false);
  const [workflowDiagramError, setWorkflowDiagramError] = useState("");
  const [testDiagramImages, setTestDiagramImages] = useState([]);
  const [testDiagramLoading, setTestDiagramLoading] = useState(false);
  const [testDiagramError, setTestDiagramError] = useState("");
  const [swaggerUrl, setSwaggerUrl] = useState("");

  useEffect(() => {
    window.localStorage.setItem("chenile-admin-base-url", baseUrlInput);
  }, [baseUrlInput]);

  const sortedLocalServices = useMemo(() => {
    return [...(info?.services || [])]
      .map(normalizeLocalService)
      .sort((a, b) => a.displayName.localeCompare(b.displayName));
  }, [info]);

  const sortedEcosystemServices = useMemo(() => {
    return [...ecosystemServices]
      .map(normalizeRegistryService)
      .sort((a, b) => {
        const nameCompare = a.displayName.localeCompare(b.displayName);
        if (nameCompare !== 0) {
          return nameCompare;
        }
        const versionCompare = valueOrDash(a.serviceVersion).localeCompare(
          valueOrDash(b.serviceVersion)
        );
        if (versionCompare !== 0) {
          return versionCompare;
        }
        return valueOrDash(a.baseUrl).localeCompare(valueOrDash(b.baseUrl));
      });
  }, [ecosystemServices]);

  const activeServices = useMemo(() => {
    return catalogMode === "ecosystem" ? sortedEcosystemServices : sortedLocalServices;
  }, [catalogMode, sortedEcosystemServices, sortedLocalServices]);

  const registryHostDetected = useMemo(() => isRegistryHost(info), [info]);
  const workflowInfoService = useMemo(
    () => resolveWorkflowInfoService(selectedService, activeServices),
    [selectedService, activeServices]
  );

  async function loadServices() {
    const normalized = normalizeBaseUrl(baseUrlInput);
    setBaseUrl(normalized);
    setServicesLoading(true);
    setServicesError("");
    setCatalogMode("local");
    setEcosystemServices([]);
    setEcosystemError("");
    setSelectedServiceName("");
    setSelectedService(null);
    setServiceError("");
    setHealthInfo(null);
    setHealthError("");
    setWorkflowDiagram("");
    setWorkflowDiagramError("");
    setTestDiagramImages([]);
    setTestDiagramError("");
    setSwaggerUrl("");
    try {
      const [payload, detectedSwaggerUrl] = await Promise.all([
        fetchJson(buildRequestUrl(normalized, "/info")),
        findSwaggerUrl(normalized),
      ]);
      setInfo(payload);
      setSwaggerUrl(detectedSwaggerUrl);
      if (isRegistryHost(payload)) {
        setEcosystemLoading(true);
        try {
          const registryPayload = await fetchJson(
            buildRequestUrl(normalized, "/serviceregistry")
          );
          setEcosystemServices(registryPayload || []);
        } catch (error) {
          setEcosystemError(error.message);
        } finally {
          setEcosystemLoading(false);
        }
      }
    } catch (error) {
      setInfo(null);
      setServicesError(error.message);
    } finally {
      setServicesLoading(false);
    }
  }

  async function loadServiceDetails(serviceSummary) {
    if (!baseUrl) {
      return;
    }
    setSelectedServiceName(serviceSummary.displayName);
    setServiceLoading(true);
    setServiceError("");
    setSelectedService(null);
    setHealthInfo(null);
    setHealthError("");
    setWorkflowDiagram("");
    setWorkflowDiagramError("");
    setTestDiagramImages([]);
    setTestDiagramError("");
    try {
      if (serviceSummary.catalog === "ecosystem") {
        setSelectedService(serviceSummary);
      } else {
        const payload = await fetchJson(
          buildRequestUrl(
            baseUrl,
            `/service-info/${encodeURIComponent(serviceSummary.lookupName)}`
          )
        );
        setSelectedService(normalizeLocalService(payload));
      }
    } catch (error) {
      setServiceError(error.message);
    } finally {
      setServiceLoading(false);
    }
  }

  async function runHealthCheck() {
    if (!selectedService || !selectedService.healthCheckerName) {
      return;
    }
    setHealthLoading(true);
    setHealthError("");
    setHealthInfo(null);
    try {
      const targetBaseUrl =
        selectedService.catalog === "ecosystem" && selectedService.baseUrl
          ? normalizeBaseUrl(selectedService.baseUrl)
          : baseUrl;
      const headers = {};
      if (trajectoryId.trim()) {
        headers["chenile-trajectory-id"] = trajectoryId.trim();
      }
      const payload = await fetchJson(
        buildRequestUrl(
          targetBaseUrl,
          `/health-check/${encodeURIComponent(selectedService.lookupName)}`
        ),
        { headers }
      );
      setHealthInfo(payload);
    } catch (error) {
      setHealthError(error.message);
    } finally {
      setHealthLoading(false);
    }
  }

  async function loadWorkflowDiagram() {
    if (!workflowInfoService) {
      return;
    }
    const operationUrl = deriveWorkflowOperationUrl(
      selectedService,
      workflowInfoService,
      "/state-diagram"
    );
    if (!operationUrl) {
      setWorkflowDiagramError("Workflow state-diagram endpoint is not available.");
      return;
    }
    setWorkflowDiagramLoading(true);
    setWorkflowDiagramError("");
    setWorkflowDiagram("");
    try {
      const targetBaseUrl =
        workflowInfoService.catalog === "ecosystem" && workflowInfoService.baseUrl
          ? normalizeBaseUrl(workflowInfoService.baseUrl)
          : baseUrl;
      const payload = await fetchJson(buildRequestUrl(targetBaseUrl, operationUrl), {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({}),
      });
      setWorkflowDiagram(payload?.data ? `data:image/png;base64,${payload.data}` : "");
    } catch (error) {
      setWorkflowDiagramError(error.message);
    } finally {
      setWorkflowDiagramLoading(false);
    }
  }

  async function loadWorkflowTestDiagrams() {
    if (!workflowInfoService) {
      return;
    }
    const operationUrl = deriveWorkflowOperationUrl(
      selectedService,
      workflowInfoService,
      "/test-state-diagrams"
    );
    if (!operationUrl) {
      setTestDiagramError("Workflow test-diagram endpoint is not available.");
      return;
    }
    setTestDiagramLoading(true);
    setTestDiagramError("");
    setTestDiagramImages([]);
    try {
      const targetBaseUrl =
        workflowInfoService.catalog === "ecosystem" && workflowInfoService.baseUrl
          ? normalizeBaseUrl(workflowInfoService.baseUrl)
          : baseUrl;
      const payload = await fetchJson(buildRequestUrl(targetBaseUrl, operationUrl), {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({}),
      });
      const images = Object.entries(payload?.data || {}).map(([name, value]) => ({
        name,
        src: `data:image/png;base64,${value}`,
      }));
      setTestDiagramImages(images);
    } catch (error) {
      setTestDiagramError(error.message);
    } finally {
      setTestDiagramLoading(false);
    }
  }

  return (
    <div className="app-shell">
      <header className="hero">
        <div>
          <p className="eyebrow">Chenile Admin UI</p>
          <h1>Inspect deployed Chenile services</h1>
          <p className="hero-copy">
            Connect to a running Chenile system, read its deployed service
            catalog from <code>/info</code>, inspect a service via{" "}
            <code>/service-info/{"{service}"}</code>, and trigger health checks
            where available. If the target monolith is hosting the central
            service registry, the UI also exposes an ecosystem-wide service
            catalog from <code>/serviceregistry</code>.
          </p>
        </div>
        <form
          className="connect-panel"
          onSubmit={(event) => {
            event.preventDefault();
            void loadServices();
          }}
        >
          <label>
            Chenile base URL
            <input
              value={baseUrlInput}
              onChange={(event) => setBaseUrlInput(event.target.value)}
              placeholder="localhost:8000"
            />
          </label>
          <button type="submit" disabled={servicesLoading}>
            {servicesLoading ? "Loading..." : "Load Services"}
          </button>
          {servicesError ? <p className="error-text">{servicesError}</p> : null}
        </form>
      </header>

      <main className="content-grid">
        <section className="panel">
          <div className="panel-header">
            <div>
              <p className="panel-title">Deployment</p>
              <h2>Services</h2>
            </div>
            {info ? (
              <button className="secondary-button" onClick={() => void loadServices()}>
                Refresh
              </button>
            ) : null}
          </div>

          {info ? (
            <>
              <div className="deployment-summary">
                <div className="metric-card">
                  <span>Base URL</span>
                  <strong>{baseUrl}</strong>
                </div>
                <div className="metric-card">
                  <span>Module</span>
                  <strong>{valueOrDash(info.monolithName || info.moduleName)}</strong>
                </div>
                <div className="metric-card">
                  <span>Version</span>
                  <strong>{valueOrDash(info.version)}</strong>
                </div>
                <div className="metric-card">
                  <span>Services</span>
                  <strong>{sortedLocalServices.length}</strong>
                </div>
              </div>

              {registryHostDetected ? (
                <div className="registry-callout">
                  <div>
                    <span>Central service registry detected</span>
                    <p>
                      This monolith is hosting <code>{REGISTRY_SERVICE_ID}</code>.
                      You can switch from local deployment services to the
                      ecosystem-wide catalog aggregated from all delegates.
                    </p>
                  </div>
                  <div className="registry-stats">
                    <strong>
                      {ecosystemLoading ? "Loading..." : ecosystemServices.length}
                    </strong>
                    <span>Ecosystem services</span>
                  </div>
                </div>
              ) : null}

              {swaggerUrl ? (
                <div className="swagger-callout">
                  <span>Swagger/OpenAPI detected</span>
                  <a href={swaggerUrl} target="_blank" rel="noreferrer">
                    Open API docs
                  </a>
                </div>
              ) : null}

              {registryHostDetected ? (
                <div className="catalog-toggle">
                  <button
                    className={catalogMode === "local" ? "" : "secondary-button"}
                    onClick={() => {
                      setCatalogMode("local");
                      setSelectedService(null);
                      setSelectedServiceName("");
                      setServiceError("");
                      setHealthInfo(null);
                      setHealthError("");
                      setWorkflowDiagram("");
                      setWorkflowDiagramError("");
                      setTestDiagramImages([]);
                      setTestDiagramError("");
                    }}
                    type="button"
                  >
                    Local Services
                  </button>
                  <button
                    className={catalogMode === "ecosystem" ? "" : "secondary-button"}
                    onClick={() => {
                      setCatalogMode("ecosystem");
                      setSelectedService(null);
                      setSelectedServiceName("");
                      setServiceError("");
                      setHealthInfo(null);
                      setHealthError("");
                      setWorkflowDiagram("");
                      setWorkflowDiagramError("");
                      setTestDiagramImages([]);
                      setTestDiagramError("");
                    }}
                    type="button"
                  >
                    Ecosystem Registry
                  </button>
                </div>
              ) : null}

              <div className="versions-block">
                <h3>Available version properties</h3>
                <div className="version-list">
                  {Object.entries(info.versions || {}).map(([key, value]) => (
                    <div key={key} className="version-row">
                      <code>{key}</code>
                      <span>{value}</span>
                    </div>
                  ))}
                </div>
              </div>

              {catalogMode === "ecosystem" && ecosystemError ? (
                <p className="error-text">{ecosystemError}</p>
              ) : null}

              <div className="service-list">
                {activeServices.map((service) => (
                  <article key={service.uniqueKey} className="service-card">
                    <div>
                      <h3>{service.displayName}</h3>
                      <p>{service.operations?.length || 0} operations</p>
                      {catalogMode === "ecosystem" ? (
                        <p className="source-text">{service.sourceLabel}</p>
                      ) : null}
                    </div>
                    <button
                      className="secondary-button"
                      onClick={() => void loadServiceDetails(service)}
                    >
                      Details
                    </button>
                  </article>
                ))}
              </div>
            </>
          ) : (
            <div className="empty-state">
              Enter a Chenile base URL and load the deployed services.
            </div>
          )}
        </section>

        <section className="panel">
          <div className="panel-header">
            <div>
              <p className="panel-title">Service Details</p>
              <h2>{selectedServiceName || "Select a service"}</h2>
            </div>
          </div>

          {serviceLoading ? <div className="empty-state">Loading details...</div> : null}
          {serviceError ? <p className="error-text">{serviceError}</p> : null}

          {selectedService ? (
            <>
              <div className="detail-grid">
                <DetailRow label="Catalog" value={selectedService.catalog} />
                <DetailRow
                  label="ID"
                  value={selectedService.serviceId || selectedService.id}
                />
                <DetailRow label="Bean name" value={selectedService.name} />
                <DetailRow
                  label="Service module"
                  value={selectedService.serviceModule}
                />
                <DetailRow
                  label="Version"
                  value={selectedService.serviceVersion || selectedService.version}
                />
                <DetailRow label="Monolith" value={selectedService.monolithName || selectedService.moduleName} />
                <DetailRow label="Base URL" value={selectedService.baseUrl} />
                <DetailRow
                  label="Health checker"
                  value={selectedService.healthCheckerName}
                />
                <DetailRow label="Mock service" value={selectedService.mockName} />
              </div>

              <div className="health-block">
                <div className="health-toolbar">
                  <label>
                    Trajectory ID
                    <input
                      value={trajectoryId}
                      onChange={(event) => setTrajectoryId(event.target.value)}
                      placeholder="optional"
                    />
                  </label>
                  <button
                    onClick={() => void runHealthCheck()}
                    disabled={!selectedService.healthCheckerName || healthLoading}
                    type="button"
                  >
                    {healthLoading ? "Checking..." : "Run Health Check"}
                  </button>
                </div>
                {!selectedService.healthCheckerName ? (
                  <p className="muted-text">
                    This service does not expose a health checker.
                  </p>
                ) : null}
                {healthError ? <p className="error-text">{healthError}</p> : null}
                {healthInfo ? (
                  <div
                    className={
                      healthInfo.healthy ? "health-result healthy" : "health-result unhealthy"
                    }
                  >
                    <strong>{healthInfo.healthy ? "Healthy" : "Unhealthy"}</strong>
                    <span>Status code: {valueOrDash(healthInfo.statusCode)}</span>
                    <p>{valueOrDash(healthInfo.message)}</p>
                  </div>
                ) : null}
              </div>

              <div className="operations-block">
                <h3>Operations</h3>
                <table>
                  <thead>
                    <tr>
                      <th>Name</th>
                      <th>Method</th>
                      <th>URL</th>
                    </tr>
                  </thead>
                  <tbody>
                    {(selectedService.operations || []).map((operation) => (
                      <tr key={`${operation.name}-${operation.url || ""}`}>
                        <td>{valueOrDash(operation.name)}</td>
                        <td>{valueOrDash(operation.httpMethod)}</td>
                        <td>
                          <code>{valueOrDash(operation.url)}</code>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>

              {workflowInfoService ? (
                <div className="workflow-info-block">
                  <div className="workflow-info-header">
                    <div>
                      <h3>Workflow Tools</h3>
                      <p className="muted-text">
                        Available because <code>{workflowInfoService.lookupName}</code> is
                        exposed by this deployment.
                      </p>
                    </div>
                    <div className="workflow-actions">
                      <button
                        type="button"
                        onClick={() => void loadWorkflowDiagram()}
                        disabled={workflowDiagramLoading}
                      >
                        {workflowDiagramLoading ? "Rendering..." : "Load State Diagram"}
                      </button>
                      <button
                        className="secondary-button"
                        type="button"
                        onClick={() => void loadWorkflowTestDiagrams()}
                        disabled={testDiagramLoading}
                      >
                        {testDiagramLoading ? "Rendering..." : "Load Test Diagrams"}
                      </button>
                    </div>
                  </div>

                  {workflowDiagramError ? (
                    <p className="error-text">{workflowDiagramError}</p>
                  ) : null}
                  {workflowDiagram ? (
                    <div className="workflow-image-card">
                      <span>State Diagram</span>
                      <img src={workflowDiagram} alt="Workflow state diagram" />
                    </div>
                  ) : null}

                  {testDiagramError ? <p className="error-text">{testDiagramError}</p> : null}
                  {testDiagramImages.length ? (
                    <div className="workflow-gallery">
                      {testDiagramImages.map((image) => (
                        <div key={image.name} className="workflow-image-card">
                          <span>{image.name}</span>
                          <img src={image.src} alt={image.name} />
                        </div>
                      ))}
                    </div>
                  ) : null}
                </div>
              ) : null}
            </>
          ) : !serviceLoading ? (
            <div className="empty-state">
              Choose a service from the list to inspect its full definition.
            </div>
          ) : null}
        </section>
      </main>
    </div>
  );
}

function DetailRow({ label, value }) {
  return (
    <div className="detail-row">
      <span>{label}</span>
      <strong>{valueOrDash(value)}</strong>
    </div>
  );
}

export default App;
