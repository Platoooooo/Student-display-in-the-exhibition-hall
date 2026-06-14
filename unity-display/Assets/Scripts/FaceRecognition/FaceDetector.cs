using System;
using System.Collections;
using UnityEngine;
using Exhibition.Core;
using Exhibition.Network;

namespace Exhibition.FaceRecognition
{
    public class FaceDetector : MonoBehaviour
    {
        [Header("配置")]
        public float detectInterval = 0.5f;     // 检测间隔
        public int requestedWidth = 1280;
        public int requestedHeight = 720;
        public float personalModeDuration = 30f;

        private WebCamTexture _webcam;
        private float _lastTriggerTime;
        private long _currentLockedUserId = -1;
        private Coroutine _personalTimer;

        IEnumerator Start()
        {
            yield return Application.RequestUserAuthorization(UserAuthorization.WebCam);
            if (!Application.HasUserAuthorization(UserAuthorization.WebCam))
            {
                Debug.LogError("[FaceDetector] 未授权摄像头");
                yield break;
            }

            var devices = WebCamTexture.devices;
            if (devices.Length == 0) { Debug.LogError("无摄像头"); yield break; }

            _webcam = new WebCamTexture(devices[0].name, requestedWidth, requestedHeight, 30);
            _webcam.Play();

            yield return new WaitUntil(() => _webcam.width > 16);
            StartCoroutine(DetectLoop());
        }

        IEnumerator DetectLoop()
        {
            while (true)
            {
                yield return new WaitForSeconds(detectInterval);
                if (!_webcam.isPlaying) continue;

                // 1) 抓帧
                var tex = new Texture2D(_webcam.width, _webcam.height, TextureFormat.RGB24, false);
                tex.SetPixels32(_webcam.GetPixels32());
                tex.Apply();

                // 2) ArcFace 检测+提取特征（伪调用，需根据 SDK 封装）
                byte[] feature = ArcFaceWrapper.DetectAndExtract(tex);
                Destroy(tex);

                if (feature == null || feature.Length == 0) continue;

                // 3) 上报后端比对
                string base64 = Convert.ToBase64String(feature);
                ApiClient.Instance.RecognizeFace(base64, OnRecognizeResult);
            }
        }

        void OnRecognizeResult(FaceRecognizeResult result)
        {
            if (result == null || !result.matched) return;
            if (result.userId == _currentLockedUserId) return;     // 同一人不重复触发
            if (Time.time - _lastTriggerTime < 3f) return;         // 防抖

            _lastTriggerTime = Time.time;
            _currentLockedUserId = result.userId;

            EventBus.Emit(GameEvents.PersonalModeStart, result);

            if (_personalTimer != null) StopCoroutine(_personalTimer);
            _personalTimer = StartCoroutine(PersonalModeCountdown());
        }

        IEnumerator PersonalModeCountdown()
        {
            yield return new WaitForSeconds(personalModeDuration);
            _currentLockedUserId = -1;
            EventBus.Emit(GameEvents.PersonalModeEnd);
        }

        void OnDestroy()
        {
            if (_webcam != null && _webcam.isPlaying) _webcam.Stop();
        }
    }
}