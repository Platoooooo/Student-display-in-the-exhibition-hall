using System;
using System.Collections;
using UnityEngine;
using NativeWebSocket;
using Newtonsoft.Json;
using Exhibition.Core;

namespace Exhibition.Network
{
    /// <summary>
    /// 接收后端推送 + 心跳保活 + 自动重连
    /// 消息类型: REFRESH_PLAYLIST / FORCE_PROFILE / SWITCH_MODE / NOTICE / WELCOME / PONG
    /// </summary>
    public class WebSocketClient : MonoBehaviour
    {
        public string wsUrl = "ws://localhost:8080/ws/display";
        public float heartbeatInterval = 25f;        // 25 秒发一次 ping
        public float reconnectInterval = 5f;
        private WebSocket _ws;
        private Coroutine _heartbeat;

        async void Start()
        {
            await Connect();
        }

        async System.Threading.Tasks.Task Connect()
        {
            _ws = new WebSocket(wsUrl);
            _ws.OnOpen    += () =>
            {
                Debug.Log("[WS] 已连接");
                if (_heartbeat == null) _heartbeat = StartCoroutine(HeartbeatLoop());
            };
            _ws.OnError   += err => Debug.LogError("[WS] 错误: " + err);
            _ws.OnClose   += code =>
            {
                Debug.Log("[WS] 关闭: " + code);
                if (_heartbeat != null) { StopCoroutine(_heartbeat); _heartbeat = null; }
                Invoke(nameof(Reconnect), reconnectInterval);
            };
            _ws.OnMessage += OnMessage;

            await _ws.Connect();
        }

        void Update()
        {
#if !UNITY_WEBGL || UNITY_EDITOR
            _ws?.DispatchMessageQueue();
#endif
        }

        IEnumerator HeartbeatLoop()
        {
            while (true)
            {
                yield return new WaitForSeconds(heartbeatInterval);
                if (_ws != null && _ws.State == WebSocketState.Open)
                {
                    _ = _ws.SendText("{\"type\":\"ping\"}");
                }
            }
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
                        if (long.TryParse(msg.payload, out long pid))
                            GameManager.Instance.ForceShowProfile(pid);
                        break;
                    case "SWITCH_MODE":
                        if (System.Enum.TryParse<DisplayMode>(msg.payload, out var mode))
                            GameManager.Instance.SwitchMode(mode);
                        break;
                    case "NOTICE":
                        EventBus.Emit("Notice", msg.payload);
                        break;
                    case "PONG":
                    case "WELCOME":
                        // 心跳/欢迎包,无需处理
                        break;
                }
            }
            catch (Exception e) { Debug.LogError("[WS] 解析失败: " + e.Message); }
        }

        async void Reconnect()
        {
            if (_ws == null || _ws.State == WebSocketState.Open) return;
            try { await Connect(); } catch (Exception e) { Debug.LogError("[WS] 重连失败: " + e.Message); }
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
