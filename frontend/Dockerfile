# ==========================================
# Stage 1: Build React Vite application
# ==========================================
FROM node:18-alpine AS builder
WORKDIR /app

# Nhận biến môi trường VITE_API_BASE_URL từ build-time (ARG)
ARG VITE_API_BASE_URL=/api/v1
ENV VITE_API_BASE_URL=${VITE_API_BASE_URL}

# Copy package.json & package-lock.json trước để tận dụng Docker cache layer
COPY package.json package-lock.json* ./
RUN npm ci

# Copy toàn bộ source code và build production bundle ra thư mục dist
COPY . .
RUN npm run build

# ==========================================
# Stage 2: Serve static files với Nginx Alpine
# ==========================================
FROM nginx:alpine AS runtime

# Xóa cấu hình mặc định của Nginx
RUN rm /etc/nginx/conf.d/default.conf

# Copy file cấu hình custom nginx.conf vào Nginx
COPY nginx.conf /etc/nginx/conf.d/default.conf

# Copy thư mục build static (dist) từ builder stage sang thư mục host của Nginx
COPY --from=builder /app/dist /usr/share/nginx/html

EXPOSE 80
ENTRYPOINT ["nginx", "-g", "daemon off;"]
