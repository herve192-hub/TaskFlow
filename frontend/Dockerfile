FROM node:26.11.1-alpine3.24@sha256:143494b1da2945f061539253adc65e4f1569ddf07da2d384c022c791a9d90a4a AS builder

WORKDIR /app

# Cache the locked dependency install independently from application source changes.
COPY package.json package-lock.json ./
RUN npm ci

COPY . .

# Vite embeds these public values in the browser bundle; never supply secrets as build arguments.
ARG VITE_API_BASE_URL=/
ENV VITE_API_BASE_URL=${VITE_API_BASE_URL}
ARG VITE_PUBLIC_REGISTRATION_ENABLED=true
ENV VITE_PUBLIC_REGISTRATION_ENABLED=${VITE_PUBLIC_REGISTRATION_ENABLED}

RUN npm run build

FROM nginx:1.31-alpine3.24@sha256:df221db836e1754089190208cee7eeda94f233197056426eda74a43ab1abeac2

# Fixed releases available in the base image's Alpine 3.24 repositories.
# Keep exact pins in sync with the repository; unavailable versions fail clean CI builds.
# libx11 patches CVE-2026-88806 in the dependency inherited from NGINX.
RUN apk add --no-cache \
    libexpat=2.9.0-r0 \
    libx11=1.8.14-r0 \
    pcre2=10.49-r0 \
    tiff=4.7.2-r0 && \
    mkdir -p /var/cache/nginx && \
    chown -R nginx:nginx /var/cache/nginx && \
    sed -i '/^user /d; s@^pid[[:space:]].*;@pid /tmp/nginx.pid;@' /etc/nginx/nginx.conf

# Copy compiled assets and runtime configuration; Node and application source stay in the builder stage.
COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY nginx-security.conf /etc/nginx/snippets/taskflow-security.conf
COPY nginx-proxy.conf /etc/nginx/snippets/taskflow-proxy.conf
COPY --from=builder /app/dist /usr/share/nginx/html

# The configured PID path and deployment tmpfs mounts support running without root.
USER nginx

EXPOSE 8080
