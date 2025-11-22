# Dockerfile (for MCP service) at the repo root
# If you keep it inside MCP-Server instead, I’ll note the small tweak below

FROM node:20-alpine
LABEL authors="arnotserunyan"

WORKDIR /app

# 1) Copy package metadata
COPY MCP-Server/package.json MCP-Server/package-lock.json* ./

# 2) Install ALL deps (including dev) so ts-node/tsx/typescript can run
RUN npm install

# 3) Copy the rest of the MCP server source
COPY MCP-Server/. .

# 4) Environment
ENV NODE_ENV=production
# Let your code decide PORT (Railway will inject PORT too)
# Your code already does: parseInt(process.env.PORT || "3333", 10)
EXPOSE 3333

# 5) Start (uses "start" script from MCP-Server/package.json)
CMD ["npm", "start"]
