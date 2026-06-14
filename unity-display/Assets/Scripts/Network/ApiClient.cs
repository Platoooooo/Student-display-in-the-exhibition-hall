using System;
using System.Collections;
using System.Collections.Generic;
using System.Text;
using UnityEngine;
using UnityEngine.Networking;
using Newtonsoft.Json;

namespace Exhibition.Network
{
    public class ApiClient : MonoBehaviour
    {
        public static ApiClient Instance { get; private set; }
        public string baseUrl = "http://localhost:8080";

        void Awake()
        {
            if (Instance == null) { Instance = this; DontDestroyOnLoad(gameObject); }
            else Destroy(gameObject);
        }

                public void GetPlaylist(Action<List<ProfileDTO>> onSuccess, Action<string> onError = null)
        {
            StartCoroutine(GetRequest<List<ProfileDTO>>("/api/display/playlist", onSuccess, onError));
        }

        public void GetProfile(long id, Action<ProfileDTO> onSuccess, Action<string> onError = null)
        {
            StartCoroutine(GetRequest<ProfileDTO>($"/api/display/profile/{id}", onSuccess, onError));
        }

        public void RecognizeFace(string featureBase64, Action<FaceRecognizeResult> onSuccess, Action<string> onError = null)
        {
            var req = new FaceRecognizeRequest
            {
                deviceId = SystemInfo.deviceUniqueIdentifier,
                featureBase64 = featureBase64
            };
            StartCoroutine(PostRequest<FaceRecognizeResult>("/api/display/face/recognize", req, onSuccess, onError));
        }

        IEnumerator GetRequest<T>(string path, Action<T> onSuccess, Action<string> onError)
        {
            using var req = UnityWebRequest.Get(baseUrl + path);
            req.timeout = 10;
            yield return req.SendWebRequest();

            if (req.result != UnityWebRequest.Result.Success)
            {
                onError?.Invoke(req.error);
                Debug.LogError($"[API] GET {path} 失败: {req.error}");
                yield break;
            }
            try
            {
                var result = JsonConvert.DeserializeObject<ApiResult<T>>(req.downloadHandler.text);
                if (result.IsSuccess) onSuccess?.Invoke(result.data);
                else onError?.Invoke(result.msg);
            }
            catch (Exception e) { onError?.Invoke(e.Message); }
        }

        IEnumerator PostRequest<T>(string path, object body, Action<T> onSuccess, Action<string> onError)
        {
            var json = JsonConvert.SerializeObject(body);
            using var req = new UnityWebRequest(baseUrl + path, "POST");
            req.uploadHandler = new UploadHandlerRaw(Encoding.UTF8.GetBytes(json));
            req.downloadHandler = new DownloadHandlerBuffer();
            req.SetRequestHeader("Content-Type", "application/json");
            req.timeout = 10;
            yield return req.SendWebRequest();

            if (req.result != UnityWebRequest.Result.Success)
            {
                onError?.Invoke(req.error);
                yield break;
            }
            try
            {
                var result = JsonConvert.DeserializeObject<ApiResult<T>>(req.downloadHandler.text);
                if (result.IsSuccess) onSuccess?.Invoke(result.data);
                else onError?.Invoke(result.msg);
            }
            catch (Exception e) { onError?.Invoke(e.Message); }
        }
    }
}