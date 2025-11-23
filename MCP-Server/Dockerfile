FROM node:20-alpine
LABEL authors="arnotserunyan"

WORKDIR /app

# 1. Install deps (including dev – we need typescript to build)
# 1. Install deps (from MCP-Server package.json)
COPY MCP-Server/package.json MCP-Server/package-lock.json* ./
RUN npm install

# 2. Copy ONLY MCP-Server code into /app
COPY MCP-Server/tsconfig.json ./tsconfig.json
COPY MCP-Server/src ./src
# 2. Copy the rest of the project
COPY . .

# 3. Compile TypeScript -> dist/
# 3. Build
RUN npm run build

ENV NODE_ENV=production
EXPOSE 3333
CMD ["npm", "start"]
