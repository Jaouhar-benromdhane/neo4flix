# ─── Stage 1 : Build Angular ──────────────────────────────────────
FROM node:20-alpine AS build
WORKDIR /app

# Copier package.json séparément pour profiter du cache Docker
COPY package*.json ./
RUN npm ci --silent

# Copier le reste et builder
COPY . .
RUN npm run build -- --configuration production

# ─── Stage 2 : Servir avec Nginx ──────────────────────────────────
FROM nginx:alpine
WORKDIR /usr/share/nginx/html

# Supprimer le contenu nginx par défaut
RUN rm -rf ./*

# Copier le build Angular (output dans dist/frontend/browser pour Angular 17+)
COPY --from=build /app/dist/frontend/browser .

# Config nginx avec support du routing Angular (HTML5 pushState)
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
