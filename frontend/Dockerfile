# Frontend image contract: build the locked React/Vite application in a Node stage,
# then serve only dist through an unprivileged NGINX runtime on port 8080.
# Use frontend/ as the build context; .dockerignore excludes local secrets/artifacts.
# VITE_* arguments are public build-time settings. Default nginx.conf supports local
# and CI use; production/AWS Compose mount their own ingress configurations while
# reusing the security/proxy snippets copied here. Runtime tmpfs paths and UID 101
# must stay aligned with deploy/production/compose.yml when changing the base image.

FROM node:24.21.0-alpine3.24@sha256:ebfe2f90462722a7a4de65e91990e97fe0d401c70e0e762c5b53302f905ec1c1 AS builder

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

# NGINX 1.30.6 fixes the server advisories; this Alpine 3.24.3 digest also includes
# the musl fix for CVE-2026-107891.
FROM nginx:1.30.6-alpine3.24@sha256:47928bf67eb62a1863cb08d9ecb52d3e704304ba614eaee17884204677702951

# Fixed releases available in the base image's Alpine 3.24 repositories.
# Keep exact pins in sync with the repository; unavailable versions fail clean CI builds.
# libx11 patches CVE-2026-88806 in the dependency inherited from NGINX.
# The pinned base still needs the Brotli fix for CVE-2026-111497.
RUN apk add --no-cache \
    brotli-libs=1.2.1-r0 \
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
