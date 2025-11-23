import express, { Request, Response, NextFunction } from "express";
import { z } from "zod";
import {
  McpServer
} from "@modelcontextprotocol/sdk/server/mcp.js";
import { StreamableHTTPServerTransport } from "@modelcontextprotocol/sdk/server/streamableHttp.js";

const BACKEND_BASE_URL =
  process.env.BACKEND_BASE_URL || "http://localhost:8080";
const PORT = parseInt(process.env.PORT || "3333", 10);

// Basic helper for POSTing Spring service
async function callBackend(path: string, body: unknown) {
  const url = `${BACKEND_BASE_URL}${path}`;
  const res = await fetch(url, {
    method: "POST",
    headers: {
      "Content-Type": "application/json"
    },
    body: JSON.stringify(body ?? {})
  });

  if (!res.ok) {
    const text = await res.text();
    throw new Error(
      `Backend error ${res.status} ${res.statusText}: ${text}`
    );
  }

  return res.json();
}

// ---- MCP server ----

const server = new McpServer({
  name: "linkedin-post-agent",
  version: "1.0.0"
});

// ---------- TOOLS ----------

// 1) /posts/ideas
server.registerTool(
  "posts_ideas",
  {
    title: "Generate LinkedIn post ideas",
    description:
      "Calls the /posts/ideas endpoint on the LinkedIn post agent. " +
      "Pass the exact JSON body that the backend expects in `body`.",
    // Let the agent pass any JSON – your backend will validate
    inputSchema: {
      body: z.any().describe(
        "JSON body for /posts/ideas. Use the same fields as IdeasInput in the Java service."
      )
    }
  },
  async ({ body }) => {
    const result = await callBackend("/posts/ideas", body);
    return {
      structuredContent: result,
      content: [
        {
          type: "text" as const,
          text: "Generated LinkedIn post ideas from /posts/ideas."
        }
      ]
    };
  }
);

// 2) /posts/outline
server.registerTool(
  "posts_outline",
  {
    title: "Generate outline for a LinkedIn post",
    description:
      "Calls the /posts/outline endpoint. Provide OutlineInput JSON in `body`.",
    inputSchema: {
      body: z.any().describe(
        "JSON body for /posts/outline (OutlineInput)."
      )
    }
  },
  async ({ body }) => {
    const result = await callBackend("/posts/outline", body);
    return {
      structuredContent: result,
      content: [
        {
          type: "text" as const,
          text: "Generated outline from /posts/outline."
        }
      ]
    };
  }
);

// 3) /posts/draft
server.registerTool(
  "posts_draft",
  {
    title: "Generate full LinkedIn post draft",
    description:
      "Calls the /posts/draft endpoint. Provide DraftInput JSON in `body`.",
    inputSchema: {
      body: z.any().describe(
        "JSON body for /posts/draft (DraftInput)."
      )
    }
  },
  async ({ body }) => {
    const result = await callBackend("/posts/draft", body);
    return {
      structuredContent: result,
      content: [
        {
          type: "text" as const,
          text: "Generated draft from /posts/draft."
        }
      ]
    };
  }
);

// 4) /posts/polish
server.registerTool(
  "posts_polish",
  {
    title: "Polish an existing LinkedIn post",
    description:
      "Calls the /posts/polish endpoint. Provide PolishInput JSON in `body`.",
    inputSchema: {
      body: z.any().describe(
        "JSON body for /posts/polish (PolishInput)."
      )
    }
  },
  async ({ body }) => {
    const result = await callBackend("/posts/polish", body);
    return {
      structuredContent: result,
      content: [
        {
          type: "text" as const,
          text: "Polished draft using /posts/polish."
        }
      ]
    };
  }
);

// 5) /posts/hashtagize
server.registerTool(
  "posts_hashtagize",
  {
    title: "Generate hashtags for a post",
    description:
      "Calls the /posts/hashtagize endpoint. Provide HashtagizeInput JSON in `body`.",
    inputSchema: {
      body: z.any().describe(
        "JSON body for /posts/hashtagize (HashtagizeInput)."
      )
    }
  },
  async ({ body }) => {
    const result = await callBackend("/posts/hashtagize", body);
    return {
      structuredContent: result,
      content: [
        {
          type: "text" as const,
          text: "Generated hashtags from /posts/hashtagize."
        }
      ]
    };
  }
);

// 6) /posts/image-prompts
server.registerTool(
  "posts_image_prompts",
  {
    title: "Generate image prompts for the post",
    description:
      "Calls the /posts/image-prompts endpoint. Provide ImagePromptsInput JSON in `body`.",
    inputSchema: {
      body: z.any().describe(
        "JSON body for /posts/image-prompts (ImagePromptsInput)."
      )
    }
  },
  async ({ body }) => {
    const result = await callBackend("/posts/image-prompts", body);
    return {
      structuredContent: result,
      content: [
        {
          type: "text" as const,
          text: "Generated image prompts from /posts/image-prompts."
        }
      ]
    };
  }
);

// 7) /posts/package
server.registerTool(
    "posts_package",
    {
      title: "Package text + hashtags + image prompt",
      description:
          "Calls the /posts/package endpoint. Provide PackageInput JSON in `body`.",
      inputSchema: {
        body: z.object({
          text: z.string().min(1, "text is required"),
          hashtags: z.array(z.string()).optional(),
          imagePrompt: z.string().optional(),
          constraints: z.string().optional()
        })
      }
    },
    async ({ body }) => {
      const payload = (body as any).body ?? body;
      const result = await callBackend("/posts/package", payload);
      return {
        structuredContent: result,
        content: [
          {
            type: "text" as const,
            text: result.finalText ?? "Packaged LinkedIn post."
          }
        ]
      };
    }
);


// ---------- HTTP transport (/mcp endpoint) ----------

const app = express();
app.use((req: Request, res: Response, next: NextFunction) => {
  console.log("Incoming request:", req.method, req.url);
  next();
});
app.use(express.json());

app.get("/health", (req: Request, res: Response) => {
  res.json({ ok: true, backend: BACKEND_BASE_URL });
});
app.get("/", (req: Request, res: Response) => {
  res.send("MCP server OK");
});

process.on("SIGTERM", () => {
  console.log("Received SIGTERM, shutting down gracefully");
  process.exit(0);
});

process.on("uncaughtException", (err) => {
  console.error("Uncaught exception:", err);
});

process.on("unhandledRejection", (reason) => {
  console.error("Unhandled rejection:", reason);
});

app.use(express.json());


app.post("/mcp", async (req: Request, res: Response) => {
  const transport = new StreamableHTTPServerTransport({sessionIdGenerator: undefined});

  res.on("close", () => {
    transport.close();
  });

  await server.connect(transport);
  await transport.handleRequest(req, res, req.body);
});

app.listen(PORT, "0.0.0.0", () => {
  console.log(
      `LinkedIn MCP server running on 0.0.0.0:${PORT}/mcp (backend: ${BACKEND_BASE_URL})`
  );
});

server.registerTool(
    "posts_full",
    {
      title: "Generate a full LinkedIn post (idea → draft → hashtags → image prompt)",
      description:
          "High-level tool that orchestrates multiple steps in the Java backend. " +
          "Provide topic, audience, goal, and optional tone/constraints.",
      inputSchema: {
        body: z.object({
          topic: z.string().describe("Main topic, e.g. 'flaky tests in CI'"),
          audience: z.string().describe("Target audience, e.g. 'senior SDETs'"),
          goal: z.string().describe("Goal, e.g. 'practical tip post'"),
          tone: z.string().optional(),
          constraints: z.string().optional(),
          maxHashtags: z.number().optional(),
          style: z.string().optional()
        })
      }
    },
    async ({ body }) => {
      // if your other tools unwrap, follow same pattern; simplest:
      const payload = (body as any).body ?? body;

      const result = await callBackend("/posts/full", payload);

      return {
        structuredContent: result,
        content: [
          {
            type: "text" as const,
            // show the final LinkedIn-ready text in chat
            text:
                result.finalText ??
                result.draft ??
                "Full LinkedIn post generated via /posts/full."
          }
        ]
      };
    }

);


