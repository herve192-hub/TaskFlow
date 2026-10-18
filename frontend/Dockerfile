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

FROM nginx:1.30.5-alpine3.24@sha256:0985e772fb9f729e6fa0980da05fca5d9c468e870eed43071545afa9d2e27d94

# Fixed releases available in the base image's Alpine 3.24 repositories.
# Keep exact pins in sync with the repository; unavailable versions fail clean CI builds.
RUN apk add --no-cache pcre2=10.49-r0 tiff=4.7.2-r0 libexpat=2.9.0-r0 && \
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
