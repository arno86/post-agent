# Dockerfile for Node MCP server

FROM node:20-alpine
LABEL authors="arnotserunyan"
WORKDIR /app

COPY package.json package-lock.json* ./
RUN npm install --only=production

COPY . .

# Expose MCP port
EXPOSE 3333

# Environment:
# - BACKEND_BASE_URL: URL of the Spring Boot service (in cloud)
# - PORT: MCP listening port (3333)
ENV PORT=3333
ENV BACKEND_BASE_URL=""

CMD ["npm", "start"]
