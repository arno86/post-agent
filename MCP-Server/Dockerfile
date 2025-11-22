FROM node:20-alpine
LABEL authors="arnotserunyan"

WORKDIR /app

# 1. Install deps (including dev – we need typescript to build)
COPY package.json package-lock.json* ./
RUN npm install

# 2. Copy the rest of the project
COPY . .

# 3. Compile TypeScript -> dist/
RUN npm run build

# 4. Runtime config
ENV NODE_ENV=production
EXPOSE 3333

# 5. Start the compiled JS
CMD ["npm", "start"]
