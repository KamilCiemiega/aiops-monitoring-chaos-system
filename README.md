AIOps Monitoring & Chaos Engineering System
🎓 Engineering Thesis Project
An intelligent, high-availability monitoring ecosystem designed for proactive server infrastructure diagnostics using statistical anomaly detection and controlled fault injection.

🚀 Key Features
* Intelligent Diagnostics (AIOps): Three custom-implemented engines in Java:

Linear Drift Detection (Memory leak identification).

Cross-Metric Correlation (Pearson correlation between system resources).

Holt-Winters Forecasting (Seasonal trend prediction).

High Availability (HA): Backend cluster with Nginx Load Balancer ensuring 99.9% uptime for the monitoring service.

Chaos Engineering Module: Dedicated agents with a Chaos API for controlled simulation of failures (CPU stress, RAM leaks, network latency).

Real-time Telemetry: Instant alert delivery via WebSockets (STOMP) and live data visualization.

Hybrid Dashboard: Custom React UI integrated with embedded Grafana panels.

🛠️ Tech Stack
Backend: Java 17, Spring Boot, Spring Security, WebSockets.

Frontend: React, TypeScript, Tailwind CSS, Grafana (Embedded).

Data: InfluxDB (Time-series), PostgreSQL (Relational).

Infrastructure: Docker, Docker Compose, Nginx (Load Balancing).

Analysis: Custom Statistical Models (Holt-Winters, Pearson).

🏗️ Architecture
The system follows a distributed architecture orchestrated via Docker Compose:

Agent Layer: Lightweight containers simulating production nodes with telemetry exporters.

Processing Layer: Clustered Spring Boot instances analyzing incoming data streams.

Persistence Layer: Optimized storage for high-frequency time-series data.

Presentation Layer: TypeScript-based dashboard for system management and fault injection.
🧪 Testing & Validation
The project includes a comprehensive test suite:

Unit Tests: JUnit/Mockito for statistical algorithms.

Integration Tests: Validating InfluxDB data flow.

Chaos Scenarios: End-to-end validation of anomaly detection under simulated stress.
