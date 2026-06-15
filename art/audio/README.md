# 音效素材占位说明

本目录未提交音频文件，由 Unity 工程在导入时另行准备：

| 文件名 | 用途 | 推荐时长/格式 |
|---|---|---|
| `bgm_ambient.ogg` | 默认轮播背景音 | 60s 循环 / OGG 128kbps |
| `sfx_card_in.wav` | 卡片切换 | 0.4s / WAV 16bit |
| `sfx_face_match.wav` | 识别命中欢迎音 | 0.8s / WAV |
| `sfx_search_ok.wav` | 搜索确认 | 0.3s / WAV |
| `sfx_notice.wav` | 通知提示 | 0.5s / WAV |

> 推荐资源：[Mixkit](https://mixkit.co/free-sound-effects/) / [Pixabay Music](https://pixabay.com/music/)（CC0 商用免费）。
> 导入 Unity 后，将以上音频拖入 `Assets/Art/Audio/` 并由 `UIManager` 引用。
