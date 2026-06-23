# ArcFace SDK 原生库放置说明

> 适用于：虹软 ArcFace 4.0 (Windows x64) + Unity 2022.3 LTS
> 现状：当前为 Mock 模式（不依赖 SDK 即可联调），需切换为真实模式请按下文操作。

## 1. 目录结构

Unity 要求 64 位 Windows 平台的原生库放这里：

```
Assets/Plugins/x86_64/
├── libarcsoft_face_engine.dll     ← 主 SDK 引擎 DLL
├── libarcsoft_face.dll            ← 人脸算法核心 DLL（引擎会动态加载它）
└── <授权文件>.dat                 ← 首次激活用的机器码文件（见下方说明）
```

> ⚠️ 必须放 `x86_64` 子目录，不能直接放 `Plugins/` 根目录。
> 否则 Unity 会按"任意平台 + 32 位"打包，运行时找不到库。

## 2. 从你下载的 SDK 包里拷贝哪些文件

你拿到的 4.0 SDK 包一般长这样：

```
arcsoft_face_sdk_4.0_xxx/
├── inc/                        ← 头文件，仅给 C++ 用，Unity 端不用
├── lib/
│   └── x64/
│       ├── libarcsoft_face_engine.dll   ← 拷贝这个
│       └── libarcsoft_face.dll          ← 拷贝这个
└── doc/
```

把 `lib/x64/` 下两个 `.dll` 全部拷到 `Assets/Plugins/x86_64/`。

## 3. 授权文件（首次激活用）

虹软 4.0 离线激活有 2 种方案：

### 方案 A：在线自动激活（推荐）
- 不用额外授权文件
- AppID + SDKKey 填到 `MainScene/[FaceDetector]` 的 Inspector 上
- 首次运行联网激活，写入 Windows 注册表，后续离线可用

### 方案 B：离线机器码授权
- 申请 SDK 时虹软会给你一个 `.dat` 文件（或 `ArcFaceTerm` 文件夹）
- 把它放在 `Assets/Plugins/x86_64/` 与 DLL 同目录
- 首次运行时 SDK 自动读取激活
- 优点：完全离线；缺点：换机器要重新申请

## 4. Unity 自动配置

文件拷贝完成后，Unity 首次编译会自动识别：
- 文件被归类到 "Standalone Windows x86_64" 平台
- 打包时自动复制到 `_Data/Plugins/x86_64/`

如果你手动拖文件到 Inspector 检查一下：
- `libarcsoft_face_engine.dll` → Select platforms: **Standalone Settings** 勾上 **Windows**
- 下方 **Settings for Standalone** → CPU: **x86_64**（已自动默认）
- 不需要勾 "Editor"（除非你 Editor 里也跑识别）

## 5. Player Settings 必改项

打开 **Edit → Project Settings → Player**：

| 项 | 建议值 | 原因 |
|---|---|---|
| **Configuration → Scripting Backend** | **IL2CPP** | C++ 原生库 IL2CPP 模式下 ABI 兼容最稳；Mono 也能用但偶有问题 |
| **Configuration → Target Architectures** | 勾 **x86_64**，取消 ARM64 | 大屏是 PC |
| **Other Settings → Camera Usage Description** | "校友人脸识别需要使用摄像头" | Windows 10+ 强制要求 |
| **Other Settings → .NET API Compatibility Level** | **.NET Framework** | DLLImport 默认用这个 |
| **Resolution → Fullscreen Mode** | Fullscreen Window | 4K 大屏专用 |

## 6. 验证接入

打开 MainScene，**Play** 后看 Console：

```
✅ 正常日志：
[ArcFace] 激活: 0                    ← ASFActivation 返回 0
[ArcFace] InitEngine: 0               ← ASFInitEngine 返回 0
[FaceDetector] 已授权摄像头
[FaceDetector] 检测到设备: HD Webcam
...（每 0.5s 触发一次 DetectAndExtract）

❌ 常见错误：
DllNotFoundException: libarcsoft_face_engine   → 库没找到，检查路径/平台
BadImageFormatException                         → 32/64 位混了，确认是 x64
ASFActivation 返回非 0（如 81925）                → appId/sdkKey 错或未联网激活
```

## 7. 切换 Mock ↔ 真实模式

`MainScene/[FaceDetector]` Inspector 上：
- `Use Mock` ✅ 勾上 → 用 Mock 哈希，不依赖 SDK（开发/演示用）
- `Use Mock` ❌ 取消 → 调真实 SDK（生产用）

修改后无需重启 Scene，运行期生效。
