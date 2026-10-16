FROM node:24.21.0-alpine3.24@sha256:ebfe2f90462722a7a4de65e91990e97fe0d401c70e0e762c5b53302f905ec1c1 AS builder

WORKDIR /app

COPY package.json package-lock.json ./
RUN npm ci

COPY . .

ARG VITE_API_BASE_URL=/
ENV VITE_API_BASE_URL=${VITE_API_BASE_URL}
ARG VITE_PUBLIC_REGISTRATION_ENABLED=true
ENV VITE_PUBLIC_REGISTRATION_ENABLED=${VITE_PUBLIC_REGISTRATION_ENABLED}

RUN npm run build

FROM nginx:1.30.5-alpine3.24@sha256:0985e772fb9f729e6fa0980da05fca5d9c468e870eed43071545afa9d2e27d94

# Fixed releases for CVEs still present in the pinned base image.
RUN apk add --no-cache pcre2=10.49-r0 tiff=4.7.2-r0 libexpat=2.8.5-r0 && \
    mkdir -p /var/cache/nginx && \
    chown -R nginx:nginx /var/cache/nginx && \
    sed -i '/^user /d; s@^pid[[:space:]].*;@pid /tmp/nginx.pid;@' /etc/nginx/nginx.conf

COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY nginx-security.conf /etc/nginx/snippets/taskflow-security.conf
COPY nginx-proxy.conf /etc/nginx/snippets/taskflow-proxy.conf
COPY --from=builder /app/dist /usr/share/nginx/html

USER nginx

EXPOSE 8080
