# Unity 工程目录说明

## 第一次打开

1. 安装 **Unity Hub** + **Unity 2022.3.20f1**（LTS）
2. Unity Hub → "Add" → 选择本目录（unity-display）
3. 首次打开会自动：
   - 生成 `ProjectSettings/*.asset`（GraphicsSettings, EditorSettings, InputManager …）
   - 拉取 `Packages/manifest.json` 中声明的依赖（含 OpenUPM 的 DOTween 与 NativeWebSocket）
   - 编译 `Assets/Scripts/` 下全部 C# 脚本
4. 打开 `Assets/Scenes/MainScene.unity`（首次需手动创建，挂载方式见 `docs/unity/场景挂载对象说明.md`）

## 包依赖（manifest.json）

- 内置：URP 14、Cinemachine 2.9、TextMeshPro、Timeline、UGUI、Newtonsoft.Json
- OpenUPM：
  - `com.demigiant.dotween` — DOTween（动效）
  - `com.endel.nativewebsocket` — NativeWebSocket（与 Spring 后端通信）

> 若 OpenUPM 无法访问，可改为本地导入 .unitypackage：
> - DOTween：https://dotween.demigiant.com/download
> - NativeWebSocket：https://github.com/endel/NativeWebSocket（导入 `WebSocket/` 目录）

## 关键脚本

```
Assets/Scripts/
├── Core/         GameManager, EventBus
├── Network/      ApiClient, WebSocketClient
├── Display/      CarouselController, ProfileCard, ListView, SearchView, PersonalView, PostFXController
├── FaceRecognition/  ArcFaceWrapper（默认 Mock 模式）, FaceDetector
├── DTOs/         ProfileDTO 等数据契约
└── UI/           UIManager
```

## 运行配置

打开后进入 **Edit → Project Settings**：

- **Player → Resolution**：3840×2160 全屏（生产）/1920×1080 调试
- **Player → Other Settings → Camera Usage Description**：填写 "校友人脸识别需要使用摄像头"
- **Quality**：4K 建议 High，1080p 建议 Balanced
- **Graphics → Scriptable Render Pipeline Settings**：选择 URP-HighFidelity（首次打开 URP 模板会自动生成）

## 后端联调

- ApiClient 在 GameManager 上挂载，inspector 中设置 `BaseUrl = http://<后端IP>:8080`
- WebSocketClient 设置 `Url = ws://<后端IP>:8080/ws/display`
- 默认 25s 心跳 + 5s 重连，已硬编码在 WebSocketClient.cs

## 视觉素材

`art/` 目录已提供基础贴图与字体，导入路径：
- `Assets/Art/Backgrounds/`（bg_main / bg_grid / bg_radial）
- `Assets/Art/Particles/`（star_dot / transition_burst / light_streak）
- `Assets/Art/UI/`（frame_card / icon_xxx / tag_chip / progress_dot）
- `Assets/Art/Fonts/`（HarmonyOS Sans / Rajdhani / DIN）

将仓库根目录 `art/` 复制到 `unity-display/Assets/Art/` 即可。
