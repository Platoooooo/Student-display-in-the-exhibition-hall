# Unity 大屏工程（暂时停用）

> 当前大屏展示已改用 Vue 网页版（`admin-web/src/views/DisplayScreen.vue`），Unity 端暂时停用但代码保留。

## 为什么切换

- TMP 中文字体在 Unity 中配置复杂（需手动 SDF 烘焙）
- 浏览器原生支持中文渲染，字体即拖即用
- Vite 热更新比 Unity 编译快
- 人脸识别已迁移到后端 JNA 桥接，浏览器摄像头可用

## 如需恢复 Unity

### 环境准备

1. 安装 **Unity Hub** + **Unity 2022.3.62f3**
2. Unity Hub → "Add" → 选择本目录
3. 打开 `Assets/Scenes/MainScene.unity`

### 场景搭建

目前 `MainScene.unity` 只有 Main Camera，需手动搭建：
- Canvas + EventSystem
- Managers（GameManager / ApiClient / WebSocketClient）
- CarouselRoot + CardA / CardB（ProfileCard 组件 + TMP 子对象）
- 详见 `docs/unity/场景挂载对象说明.md`

### 运行配置

- **Player → Resolution**：3840x2160（生产）/ 1920x1080（调试）
- **Player → Scripting Backend**：IL2CPP（ArcFace 原生库兼容）
- **Player → Target Architectures**：仅 x86_64
- **Quality**：4K → High，1080p → Balanced
- **Graphics → SRP Settings**：选择 URP-HighFidelity

### ArcFace SDK

DLL 放置路径：`Assets/Plugins/x86_64/libarcsoft_face_engine.dll` + `libarcsoft_face.dll`

`ArcFaceWrapper.cs` 默认 `useMock=true`，真机上线时传入 AppId + SDKKey：
```csharp
ArcFaceWrapper.Init(appId, sdkKey, useMock: false);
```

### 包依赖

- 内置：URP 14 / Cinemachine 2.9 / TextMeshPro / Timeline / Newtonsoft.Json
- OpenUPM：DOTween / NativeWebSocket
- 本地：DOTween Modules 源文件置于 `Assets/Scripts/DOTween/`

## 关键脚本

```
Assets/Scripts/
├── Core/               GameManager, EventBus
├── Network/            ApiClient, WebSocketClient
├── Display/            CarouselController, ProfileCard, ListView, SearchView, PersonalView, PostFXController
├── FaceRecognition/    ArcFaceWrapper（Mock 模式）, FaceDetector
├── DTOs/               ProfileDTO 等
└── UI/                 UIManager
```

## 后端联调

- ApiClient BaseUrl = `http://<后端IP>:8080`
- WebSocketClient Url = `ws://<后端IP>:8080/ws/display`
- 25s 心跳 + 5s 自动重连

## 视觉素材

`art/` 目录提供基础贴图与字体，复制到 `Assets/Art/` 即可：
- `Art/Backgrounds/` — bg_main / bg_grid / bg_radial
- `Art/Particles/` — star_dot / transition_burst / light_streak
- `Art/UI/` — frame_card / icon_xxx / tag_chip / progress_dot
- `Art/Fonts/` — HarmonyOS Sans / Rajdhani / DIN
