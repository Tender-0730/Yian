# 前端编码规范

> Vue 3 社区最佳实践 + Element Plus 规范，AI 编码时必须严格遵守。

---

## 一、技术栈

| 层面 | 选型 | 版本 |
|------|------|------|
| 框架 | Vue 3 Composition API | 3.5.x |
| 构建 | Vite | 6.x |
| UI 库 | Element Plus | 2.9.x |
| 状态管理 | Pinia + persistedstate | 2.2.x |
| 路由 | Vue Router | 4.4.x |
| HTTP | Axios | 1.7.x |
| CSS | Sass | 1.83.x |

---

## 二、目录结构

```
src/
├── api/              # API 层，按模块拆分 (request.js + auth/resident/room/health/meal/dashboard/user.js)
├── components/       # 公共组件，按功能分子目录
├── composables/      # 可复用组合函数 (usePagination、useForm)
├── router/index.js   # 路由表 + 导航守卫
├── stores/           # Pinia store，按领域拆分
├── utils/            # 纯工具函数
└── views/            # 页面视图，按模块分子目录 (Login/ Layout/ Dashboard/ Resident/ Room/ Health/ Meal/ User/)
```

---

## 三、组件规范

### SFC 结构

强制 `<script setup>`，禁止 Options API。段落顺序固定：`<script>` → `<template>` → `<style lang="scss" scoped>`。

### 数据流

| 方向 | 机制 | 场景 |
|------|------|------|
| 父→子 | `defineProps` | 传递数据 |
| 子→父 | `defineEmits` | 事件回调 |
| 双向 | `defineModel` (Vue 3.4+) | 弹窗显隐、表单值 |
| 跨层级 | Pinia store | 用户信息、权限 |
| 深层依赖 | `provide`/`inject` | 仅表单上下文等少数场景 |

### 组件拆分标准

满足任一条件必须拆分：
- 同时持有状态 + 大量 UI 布局
- 模板中有 3+ 独立区块（筛选、表格、分页、弹窗）
- 模板块需要复用

```vue
<!-- ✅ 好的拆分 -->
views/Resident/
├── ResidentList.vue     # 容器，组装子组件
├── ResidentFilter.vue   # 筛选表单
├── ResidentTable.vue    # 数据表格
└── ResidentForm.vue     # 新增/编辑弹窗
```

### 模板规则

- `:key` 必须用唯一 ID，禁止用 `index`
- `v-if` 和 `v-for` 禁止同层
- 模板中禁止写复杂表达式，移到 `computed` 中
- 频繁切换用 `v-show`，单分支用 `v-if`

---

## 四、API 层规范

### 拦截器（`api/request.js`）

1. **请求拦截器**：自动注入 `Authorization: Bearer <token>`
2. **响应拦截器**：解包直接返回 `data`；code≠200 统一 `ElMessage.error`；401 清 token 跳登录；403 提示无权限
3. `baseURL: '/api'`，`timeout: 15000`

### API 模块

每个文件只导出函数，用 JSDoc 标注类型。视图层**决不直接 import axios**：

```js
// api/resident.js
import request from './request'

/** @returns {Promise<{ records: Array, total: number }>} */
export const pageResidents = (params) => request.get('/residents', { params })
export const getResidentById = (id) => request.get(`/residents/${id}`)
export const createResident = (data) => request.post('/residents', data)
export const updateResident = (id, data) => request.put(`/residents/${id}`, data)
export const deleteResident = (id) => request.delete(`/residents/${id}`)
```

视图层调用只处理 loading，错误拦截器已统一处理。

---

## 五、状态管理（Pinia）

使用 Setup Store 语法（函数式），不写 Options Store。

| 规则 | 说明 |
|------|------|
| 单一数据源 | 同一数据只存一处 |
| 计算优先 | 能推导的值用 getter，不另建 state |
| Action 修改 | 视图通过 action 修改，不直接写 state |
| 按需持久化 | 只持久化 token 和用户信息，表单草稿不持久化 |
| 按领域拆分 | 不要所有状态塞一个 store |

```js
export const useUserStore = defineStore('user', () => {
  const accessToken = ref('')
  const userInfo = ref(null)
  const isLoggedIn = computed(() => !!accessToken.value)
  const setLogin = (data) => { accessToken.value = data.accessToken; userInfo.value = data.userInfo }
  const logout = () => { accessToken.value = ''; userInfo.value = null }
  return { accessToken, userInfo, isLoggedIn, setLogin, logout }
}, { persist: { key: 'yian-user', pick: ['accessToken', 'refreshToken', 'userInfo'] } })
```

---

## 六、路由规范

- 路由路径 kebab-case，路由 name PascalCase，页面级组件全部懒加载
- 导航守卫：未登录→跳 `/login`，已登录访问 `/login`→重定向 `/dashboard`
- meta 声明页面标题和图标，`hidden: true` 不出现在侧边栏

---

## 七、Element Plus 使用规范

| 场景 | 规范 |
|------|------|
| 表单 | 统一用 `el-form` + `:model` + `:rules`，提交前 `await formRef.value.validate()` |
| 表格 | `v-loading` 加载态，空数据用 `el-empty`，分页用 `el-pagination` |
| 确认 | 删除等危险操作 `ElMessageBox.confirm('确定删除？')` |
| 提示 | 成功 `ElMessage.success`，失败 `ElMessage.error` |
| 图标 | 从 `@element-plus/icons-vue` 按需引入，禁止全局注册 |

---

## 八、命名规范

| 类型 | 规则 | 示例 |
|------|------|------|
| 页面文件 | PascalCase | `ResidentList.vue` |
| 公共组件 | PascalCase | `PageHeader.vue` |
| Composables | useXxx.js | `usePagination.js` |
| API 文件 | kebab-case | `resident.js` |
| Store 文件 | kebab-case | `user.js` |
| 路由路径 | kebab-case | `/health-records` |
| 变量/函数 | camelCase | `fetchData`、`handleSubmit` |
| Props | camelCase | `residentId` |
| Events | kebab-case | `@item-saved` |
| CSS class | kebab-case | `.resident-list` |

---

## 九、Composables

可复用状态/副作用逻辑抽到 `composables/`，命名 `useXxx.js`，在 `<script setup>` 顶层调用（不在循环/条件中）。通用分页用 `usePagination(apiFn)` 封装 loading/data/page/size/total。

---

## 十、禁止事项

- [ ] 禁止 Options API，统一 `<script setup>`
- [ ] 禁止视图直接 `import axios`
- [ ] 禁止 `:key` 用 index
- [ ] 禁止 `v-if` 与 `v-for` 同层
- [ ] 禁止模板中写复杂表达式（移到 computed）
- [ ] 禁止直接修改 store state（通过 action）
- [ ] 禁止 `computed` 中产生副作用
- [ ] 禁止样式不加 `scoped`
- [ ] 禁止行内样式和 `!important`
- [ ] 禁止全局注册图标
- [ ] 禁止单文件超 300 行
- [ ] 禁止 `watch` 中 `deep: true`（除非确有必要）
- [ ] 禁止循环/条件中调用 composables
- [ ] 禁止吞掉错误（try/catch 必须处理或重抛）
- [ ] 禁止 `console.log`（开发调试除外）

---

## 十一、自检清单

- [ ] `<script setup>` + 段落顺序正确
- [ ] Props/Emits 显式声明
- [ ] 样式 scoped + SCSS
- [ ] API 通过 `api/` 层
- [ ] 表单有校验，提交前 `validate()`
- [ ] 列表有 `v-loading` + 空数据 `el-empty`
- [ ] 危险操作有 `ElMessageBox.confirm`
- [ ] 页面级组件懒加载
- [ ] 路由 meta 有 `title`
- [ ] 单文件 ≤ 300 行
