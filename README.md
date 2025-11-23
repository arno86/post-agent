# LinkedIn Post Agent

An end-to-end system that generates high-quality LinkedIn posts about **project management**, **automation**, and **DevOps** using:

- A **Java / Spring Boot backend** that talks to OpenAI and provides clean REST endpoints.
- A **Node.js MCP server** that wraps those endpoints as MCP tools, so an OpenAI Agent (or MCP-aware client) can orchestrate end-to-end flows (idea → outline → draft → polish → package).

---

## High-Level Architecture

```text
User / Agent
    │
    │  (MCP / tools.* calls)
    ▼
Node MCP Server (Express + MCP SDK)
    - /mcp  (JSON-RPC MCP endpoint)
    - /health
    │
    │  (HTTP POST JSON)
    ▼
Spring Boot Backend (Java)
    - /posts/ideas
    - /posts/outline
    - /posts/draft
    - /posts/polish
    - /posts/hashtagize
    - /posts/image-prompts
    - /posts/package
    - /posts/full    ← orchestrated “one shot” post generator
    │
    │  (OpenAI API)
    ▼
OpenAI LLM (gpt-4o-mini, etc.)
