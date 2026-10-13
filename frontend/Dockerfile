FROM node:26.11.1-alpine3.24@sha256:143494b1da2945f061539253adc65e4f1569ddf07da2d384c022c791a9d90a4a AS builder

WORKDIR /app

COPY package.json package-lock.json ./
RUN npm ci

COPY . .

ARG VITE_API_BASE_URL=http://localhost:8080
ENV VITE_API_BASE_URL=${VITE_API_BASE_URL}

RUN npm run build

FROM nginx:1.31-alpine3.24@sha256:df221db836e1754089190208cee7eeda94f233197056426eda74a43ab1abeac2

# Fixed releases for CVEs still present in the pinned base image.
RUN apk add --no-cache pcre2=10.49-r0 tiff=4.7.2-r0 libexpat=2.8.5-r0 && \
    mkdir -p /var/cache/nginx && \
    chown -R nginx:nginx /var/cache/nginx && \
    sed -i '/^user /d; s@^pid[[:space:]].*;@pid /tmp/nginx.pid;@' /etc/nginx/nginx.conf

COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=builder /app/dist /usr/share/nginx/html

USER nginx

EXPOSE 8080
