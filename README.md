# Autonomous Monitoring & Chaos Engineering Platform (AIOps Core)

A distributed, autonomous IT infrastructure monitoring platform that leverages advanced AIOps analytics and a closed-loop **MAPE-K** self-healing mechanism, integrated with a Chaos Engineering injection engine.

## Overview

The `monitoring-chaos-system` serves as the central orchestration, decision-making, and analytics core of the entire ecosystem. It shifts infrastructure management from reactive firefighting to proactive mitigation by predicting failures, detecting real-time anomalies, and autonomously correcting infrastructure state before a critical outage occurs.

### System Architecture

The platform employs a decoupled, multi-tier distributed architecture consisting of three core pillars:

1. **Core Backend (Spring Boot):** The central engine managing time-series data pipelines, executing statistical algorithms, orchestrating alerts, and pushing asynchronous events via WebSockets.
2. **Distributed Infrastructure Agents ([`aiops-agent`](./aiops-agent/)):** Lightweight daemons residing on target hosts that perform low-level resource sampling and execute corrective shell actions.
3. **Control Panel Frontend ([`aiops-frontend`](./aiops-frontend/)):** A real-time React SPA providing deep observability, threshold management, and a centralized operations dashboard.

```text
                    ┌──────────────────────────────┐
                    │        aiops-frontend        │
                    │         (React SPA)          │
                    └──────────────┬───────────────┘
                                   │  ▲
                     REST (Axios)  │  │  WebSockets (STOMP)
                                   ▼  │
                    ┌──────────────────────────────┐
                    │    monitoring-chaos-system   │
                    │     (Spring Boot Core)       │
                    └──────────────┬───────────────┘
                                   │  ▲
                     Flux Queries  │  │  Line Protocol (HTTP)
                                   ▼  │
┌──────────────────────────────┐ ┌─┴──┴─────────────────────────┐
│          InfluxDB            │ │         aiops-agent          │
│    (Time Series Database)    │ │   (Chaos API & Self-Healing) │
└──────────────────────────────┘ └──────────────────────────────┘
```

## AIOps Analytics Engine & MAPE-K Loop

The platform closes the autonomous feedback loop using the **MAPE-K** (Monitor, Analyse, Plan, Execute, Knowledge) framework:

* **Monitor:** `aiops-agent` samples hardware utilization from kernel interfaces (`/proc` and `cgroups`) and streams it directly into InfluxDB.
* **Analyse:** The `AnomalyDetectionScheduler` runs asynchronous analytical routines:
  * **Linear Regression (`DataDriftDetectionService`):** Captures slow-burning, long-term anomalies like memory leaks (*RAM drift*).
  * **Triple Exponential Smoothing (`HoltWintersPredictionService`):** Models CPU utilization background noise, capturing seasonality and short-term trends.
* **Plan:** The engine dynamically projects metrics and applies a Triple Standard Deviation ($3\sigma$) filter. If real-time metrics breach this dynamic corridor, a critical incident payload is generated.
* **Execute:** `SelfHealingService` dispatches an asynchronous, non-blocking network payload to the agent to enforce immediate mitigation.

## Grafana Observability & Real-Time Metrics

The following production-simulation charts showcase the analytics engine in action during a managed failure injection experiment:

### Scenario A: Memory Leak Identification (RAM Data Drift)
* **Critical State (Anomaly Detected):** The regression slope violates the safety threshold ($a > 0.05$), flipping the status to `critical`.


* **Nominal State (Post Self-Healing):** The agent flushes caches and reallocates process groups, instantly shifting the container back to `nominal`.
  

### Scenario B: Dynamic CPU Spike Detection
* **Critical State (Corridor Breach):** A high-intensity processing load breaches the dynamic $3\sigma$ confidence interval calculated by the Holt-Winters algorithm.

* **Nominal State (Post Self-Healing):** High-priority process throttling successfully normalizes core temperatures and utilization.


## Validation & Test Results

The framework has been fully validated through rigorous automated and empirical verification cycles.

### 1. Automated Unit Testing (JUnit 5 & Mockito)
Ensures zero-regression execution of math modules on isolated data matrices:

## Deployment

### Prerequisites

Ensure you have the following software installed on your local environment before executing the installation steps:

* **Java 17 JDK** (required for running the Spring Boot backend)
* **Node.js** (v18 or higher, required for compiling the React frontend)
* **Docker & Docker Compose** (required to orchestrate InfluxDB and containerized environments)
* **Bash** (required to execute low-level telemetry sampling scripts on the agent host)

---

### Installation & Quick Start

Follow these terminal steps to clone the repository and launch the system components:

```bash
# Clone the repository
git clone [https://github.com/your-username/monitoring-chaos-system.git](https://github.com/your-username/monitoring-chaos-system.git)

# Navigate into the project root directory
cd monitoring-chaos-system

# Spin up the database and infrastructure containers in the background
docker-compose up -d