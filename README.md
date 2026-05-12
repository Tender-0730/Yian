# 颐安养老管理系统 (Yian)

Vue 3 + Java 前后端分离的养老院管理系统。

## 项目结构

```
Yian/
├── frontend/        # Vue 3 前端应用
├── backend/         # Java 后端应用
├── db.json          # Mock 数据（过渡期使用，后端完成后删除）
```

## 前端启动

```sh
cd frontend
npm install
npm run dev
```

## Mock 数据启动（过渡期）

在根目录下运行：

```sh
npx json-server --watch db.json --port 3000
```

> 注意：前端默认代理到 `localhost:8080`（Java 后端），使用 mock 数据时需将 `frontend/vite.config.js` 中的代理目标临时改为 `http://localhost:3000`。

## 后端启动

```sh
cd backend
# Java 项目启动命令
```
