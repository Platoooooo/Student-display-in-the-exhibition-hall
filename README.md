# 校友成果展览系统

> 高校校友优秀成果 / 荣誉 / 作品的全流程管理平台。
> 学生填报 → 三级审核 → 大屏（Unity）人脸识别个性化展示。

## 技术栈

| 层 | 技术 |
|---|------|
| 后端 | Spring Boot 3.2 + MyBatis Plus + Sa-Token + MinIO + WebSocket |
| 存储 | MySQL 8 / Redis 7 / MinIO |
| 管理后台 | Vue 3 + TS + Vite + Element Plus + Pinia |
| 学生 H5 | Vue 3 + TS + Vite + Vant |
| 大屏 | Unity 2022.3 LTS (URP) + DOTween + Cinemachine |
| 人脸 | 虹软 ArcFace 4.x（C# DllImport） |

## 项目结构

```
exhibition-system/
├── backend/        # Spring Boot
├── admin-web/      # Vue3 管理后台 (Element Plus)
├── student-h5/     # Vue3 学生端 H5 (Vant)
├── unity-display/  # Unity 大屏 (Assets/Scripts)
├── docker/         # docker-compose + nginx + 初始化 SQL
└── docs/           # 设计稿 / 接口约定 / 视觉基调
```

## 一键启动（开发环境）

依赖：JDK 17+、Maven 3.8+、Node.js 18+、Docker Desktop。

```bash
# 1) 起基础组件（MySQL/Redis/MinIO）
cd docker
docker compose up -d mysql redis minio

# 2) 启动后端
cd ../backend
mvn spring-boot:run

# 3) 启动管理后台
cd ../admin-web
npm install && npm run dev   # http://localhost:5173

# 4) 启动学生端
cd ../student-h5
npm install && npm run dev   # http://localhost:5174

# 5) Unity：用 Unity Hub 打开 unity-display 工程，运行 MainScene
```

## 默认账号（密码均为 `123456`）

| 用户名 | 角色 | 说明 |
|---|---|---|
| admin | 校级管理员 | 上架、权重、Dashboard |
| jiaowu | 教务处 | 第二级审核 |
| cs_audit | 院级审核（计信院） | 第一级审核 |
| student01 | 学生 | 提交资料 / 录入人脸 |
| alumni01 | 校友 | 提交资料 / 录入人脸 |

## 核心 API（全部以 `/api` 开头）

| 模块 | 路径 | 说明 |
|---|---|---|
| 认证 | POST /auth/login · /auth/logout · GET /auth/me | Sa-Token，返回 `Authorization` token |
| 学院 | GET /college/list | 学院列表 |
| 资料 | POST /profile/draft · /submit · GET /profile/{id} · DELETE /profile/{id} · GET /profile/my | 学生端 CRUD |
| 文件 | POST /file/upload (?dir=cover\|media\|face\|avatar) | MinIO 上传 |
| 审核 | GET /audit/pending · POST /audit/{id}/audit · GET /audit/{id}/history | 三级审核 |
| 后台 | GET /admin/profile/library · PUT /admin/profile/{id}/shelf · /weight · POST /admin/profile/{id}/tags · GET /admin/dashboard | 上架 / 权重 / 标签 / 仪表盘 |
| 大屏 | GET /display/playlist · /display/profile/{id} · POST /display/face/recognize · WS /ws/display | Unity 接入 |
| 人脸 | POST /face/register · GET /face/status | 学生录入 |

## 三级审核状态机

```
0 草稿 ─submit─▶ 1 院审中 ─通过─▶ 2 教务审中 ─通过─▶ 3 已发布 ─上架─▶ 大屏展示
                  │ 驳回                │ 驳回
                  └────────▶ 4 驳回 ◀───┘
学生可在 0/4 状态修改重提
```

## 部署

```bash
cd backend && mvn clean package -DskipTests
cd ../admin-web  && npm run build
cd ../student-h5 && npm run build
# 拷贝 admin-web/dist 至 docker/nginx/html/admin
# 拷贝 student-h5/dist 至 docker/nginx/html/student
cd ../docker && docker compose up -d
```

或直接执行 [docker/start.sh](docker/start.sh) / [docker/start.ps1](docker/start.ps1)。

访问：

- 管理后台：http://localhost (host 加 `127.0.0.1 admin.exhibition.local`)
- 学生 H5：http://localhost (host 加 `127.0.0.1 student.exhibition.local`)
- MinIO 控制台：http://localhost:9001 (minioadmin / minio123456)

## 开发要点

- **统一响应**：所有接口返回 `R<T> { code, msg, data }`，前端拦截器自动剥壳。
- **人脸识别**：特征向量在 Unity 端用 ArcFace 提取，仅上传 1032 字节 base64 到后端，**不传图**。
- **大屏推送**：管理后台上架/调权 → 后端 WebSocket 广播 `REFRESH_PLAYLIST` → Unity 自动重载。
- **MinIO**：bucket 默认设置匿名读策略，方便 Unity 直接走 URL 访问图片/视频；生产环境应改预签名。

## 风险备忘

- 虹软 SDK 需离线授权（机器绑定），Demo 当前用占位特征。
- 4K 视频推荐 H.265 + AVPro 插件。
- WebSocket 心跳保活 + 5s 自动重连。
- 大屏建议夜间定时重启（避免内存泄漏）。
