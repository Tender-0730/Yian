# 颐安养老管理系统 — 项目级指令

> AI 编码时必须严格遵守。

---

## 全局规则

- **所有回复使用中文**
- **未经用户明确同意，不得执行 `git commit`**
- 优先最小改动，避免无关重构
- 修改前先读懂现有代码的上下文

---

## 子规范入口

Claude Code 在处理对应目录代码时会自动加载子目录的 `CLAUDE.md`：

| 目录 | 规范文件 | 适用场景 |
|------|---------|---------|
| `backend/` | `backend/CLAUDE.md` | Java / Spring Boot / MyBatis-Plus 后端 |
| `frontend/` | `frontend/CLAUDE.md` | Vue 3 / Element Plus / Vite 前端 |

---

## 参考文档

| 文档 | 路径 | 说明 |
|------|------|------|
| API 参考 | `docx/api-reference.md` | 全部 33 个后端接口的完整说明 |
| 前端规范 | `frontend/CLAUDE.md` | 前端编码规范（组件、API 层、状态管理） |
| 后端规范 | `backend/CLAUDE.md` | 后端编码规范（阿里手册 + Spring Boot 最佳实践） |

---

## 项目结构

```
Yian/
├── backend/           # Spring Boot 3.5 后端
├── frontend/          # Vue 3 + Element Plus 前端
└── docx/              # 项目文档
```
