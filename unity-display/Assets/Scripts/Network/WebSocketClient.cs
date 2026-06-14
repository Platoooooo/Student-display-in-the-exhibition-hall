using System;
using UnityEngine;
using NativeWebSocket;
using Newtonsoft.Json;
using Exhibition.Core;

namespace Exhibition.Network
{
    /// <summary>
    /// 接收后端推送：手动切换、刷新播放列表、紧急公告等
    /// </summary>
    public class WebSocketClient : MonoBehaviour
    {
        public string wsUrl = "ws://localhost:8080/ws/display";
        private WebSocket _ws;

        async void Start()
        {
            _ws = new WebSocket(wsUrl);

            _ws.OnOpen    += () => Debug.Log("[WS] 已连接");
            _ws.OnError   += err => Debug.LogError("[WS] 错误: " + err);
            _ws.OnClose   += code => { Debug.Log("[WS] 关闭: " + code); Invoke(nameof(Reconnect), 5f); };
            _ws.OnMessage += OnMessage;

            await _ws.Connect();
        }

        void Update()
        {
#if !UNITY_WEBGL || UNITY_EDITOR
            _ws?.DispatchMessageQueue();
#endif
        }

        void OnMessage(byte[] bytes)
        {
            var json = System.Text.Encoding.UTF8.GetString(bytes);
            try
            {
                var msg = JsonConvert.DeserializeObject<WsMessage>(json);
                switch (msg.type)
                {
                    case "REFRESH_PLAYLIST":
                        GameManager.Instance.LoadPlaylist();
                        break;
                    case "FORCE_PROFILE":
                        long id = long.Parse(msg.payload);
                        // 强制切到指定资料
                        break;
                    case "NOTICE":
                        EventBus.Emit("Notice", msg.payload);
                        break;
                }
            }
            catch (Exception e) { Debug.LogError("[WS] 解析失败: " + e.Message); }
        }

        async void Reconnect()
        {
            if (_ws != null && _ws.State != WebSocketState.Open)
                await _ws.Connect();
        }

        async void OnApplicationQuit()
        {
            if (_ws != null) await _ws.Close();
        }
    }

    [Serializable]
    public class WsMessage
    {
        public string type;
        public string payload;
    }
}