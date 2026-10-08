# 🚀 Production Deployment Guide

This guide details everything you need to deploy the **Project Management Platform** to production.

---

## 🏗️ Architecture Overview

| Component | Technology | Recommended Cloud Provider |
| :--- | :--- | :--- |
| **Frontend** | Next.js 16 (React 19, Tailwind CSS) | [Vercel](https://vercel.com) / [Render](https://render.com) |
| **Backend** | Spring Boot 4 (Java 21, Spring Security) | [Render](https://render.com) / [Railway](https://railway.app) / VPS |
| **Database** | PostgreSQL 16 + Flyway Migrations | [Neon](https://neon.tech) / [Supabase](https://supabase.com) / Render Postgres |

---

## 🌟 Method 1: Cloud Deployment (Vercel + Render + Neon/Supabase)

This is the standard zero-maintenance cloud approach.

### Step 1: Set Up Cloud PostgreSQL (Neon / Supabase / Render)
1. Create a free PostgreSQL instance on **[Neon](https://neon.tech)** or **[Supabase](https://supabase.com)**.
2. Note your JDBC connection string:
   ```text
   jdbc:postgresql://<HOST>:<PORT>/<DATABASE>?sslmode=require
   ```
3. Note your database **Username** and **Password**.

---

### Step 2: Deploy Spring Boot Backend (Render / Railway)

1. Push your repository to **GitHub**.
2. Go to **[Render](https://dashboard.render.com)** -> Click **New +** -> **Web Service**.
3. Connect your GitHub repository:
   * **Root Directory**: `ProjectManagement`
   * **Runtime**: `Docker` (or `Java`)
4. In **Environment Variables**, configure:

| Key | Example Value | Note |
| :--- | :--- | :--- |
| `PORT` | `8080` | Port for backend |
| `DB_URL` | `jdbc:postgresql://ep-xyz.neon.tech:5432/neondb?sslmode=require` | Database JDBC URL |
| `DB_USERNAME` | `neondb_owner` | DB User |
| `DB_PASSWORD` | `your_db_password` | DB Password |
| `JWT_SECRET` | `XGL8HCBzI7A7t30ZfsRtfpJMz7r69XxH0EsH8aEmBC4=` | Base64 256-bit secret |
| `CORS_ALLOWED_ORIGINS` | `https://your-frontend.vercel.app,http://localhost:3000` | Frontend origins |

5. Click **Deploy Web Service**.
6. Once deployed, test your health check at `https://<your-backend-url>/api/health`.

> [!NOTE]
> Flyway database migrations and initial demo seeding run automatically on application startup.

---

### Step 3: Deploy Next.js Frontend (Vercel)

1. Go to **[Vercel](https://vercel.com)** -> Click **Add New Project**.
2. Select your GitHub repository.
3. In Project Settings:
   * **Framework Preset**: `Next.js`
   * **Root Directory**: Select `frontend`
4. Under **Environment Variables**, add:
   * `NEXT_PUBLIC_API_URL` = `https://<your-backend-url-from-step-2>` *(without trailing slash)*
5. Click **Deploy**.

Your frontend is now live with automated SSL/TLS certificates and global CDN caching!

---

## 🐳 Method 2: 1-Command Docker Compose (VPS / AWS EC2 / DigitalOcean)

If deploying to your own Linux server or cloud VM:

1. **Clone repository on server**:
   ```bash
   git clone <YOUR_REPO_URL>
   cd ProjectManagement
   ```

2. **Create `.env` file**:
   ```bash
   cp .env.example .env
   ```
   *Edit `.env` to set secure database passwords and JWT secrets.*

3. **Start All Services**:
   ```bash
   docker compose up -d --build
   ```

4. **Verify Running Containers**:
   ```bash
   docker compose ps
   ```

All three services will spin up:
- Frontend: `http://<SERVER_IP>:3000`
- Backend API & Swagger UI: `http://<SERVER_IP>:8080` / `http://<SERVER_IP>:8080/swagger-ui.html`
- PostgreSQL: `localhost:5432`

---

## 🔒 Production Security Checklist

- [x] **Safe Password Hashing**: Passwords stored via BCrypt (`strength=12`).
- [x] **Configurable CORS**: Dynamic origin whitelisting via `CORS_ALLOWED_ORIGINS`.
- [x] **Health Endpoint**: Liveness probe configured at `/api/health`.
- [x] **Database Migrations**: Managed automatically by Flyway (`src/main/resources/db/migration`).
- [x] **JWT Expiration**: Configurable session timeouts via `JWT_EXPIRATION`.
