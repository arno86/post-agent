FROM node:20-alpine
LABEL authors="arnotserunyan"
WORKDIR /app

COPY MCP-Server/package.json MCP-Server/package-lock.json* ./
RUN npm install

COPY . .

EXPOSE 3333

CMD ["npm", "start"]
