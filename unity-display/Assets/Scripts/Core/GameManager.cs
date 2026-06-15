using System.Collections.Generic;
using UnityEngine;
using Exhibition.Network;

namespace Exhibition.Core
{
    public enum DisplayMode { Carousel, List, Search, Personal }

    public class GameManager : MonoBehaviour
    {
        public static GameManager Instance { get; private set; }
        public DisplayMode currentMode = DisplayMode.Carousel;
        public List<ProfileDTO> playlist = new();
        public List<ProfileDTO> personalPlaylist = new();
        public long currentPersonalUserId = -1;

        [Header("引用")]
        public GameObject carouselRoot;
        public GameObject listRoot;
        public GameObject searchRoot;
        public GameObject personalRoot;

        void Awake()
        {
            if (Instance == null) Instance = this;
            else Destroy(gameObject);
        }

        void Start()
        {
            LoadPlaylist();
            EventBus.Subscribe(GameEvents.PersonalModeStart, OnPersonalModeStart);
            EventBus.Subscribe(GameEvents.PersonalModeEnd, OnPersonalModeEnd);
        }

        public void LoadPlaylist()
        {
            ApiClient.Instance.GetPlaylist(list =>
            {
                playlist = list;
                EventBus.Emit(GameEvents.PlaylistLoaded, list);
                Debug.Log($"[GameManager] 加载到 {list.Count} 条展示数据");
            }, err => Debug.LogError("加载失败: " + err));
        }

        public void SwitchMode(DisplayMode mode)
        {
            currentMode = mode;
            if (carouselRoot != null) carouselRoot.SetActive(mode == DisplayMode.Carousel);
            if (listRoot != null) listRoot.SetActive(mode == DisplayMode.List);
            if (searchRoot != null) searchRoot.SetActive(mode == DisplayMode.Search);
            if (personalRoot != null) personalRoot.SetActive(mode == DisplayMode.Personal);
            EventBus.Emit("ModeChanged", mode);
        }

        /// <summary>
        /// 由后端 FORCE_PROFILE 推送触发: 临时把指定资料插到 playlist 第一位并切到轮播
        /// </summary>
        public void ForceShowProfile(long profileId)
        {
            ApiClient.Instance.GetProfile(profileId, p =>
            {
                if (p == null) return;
                playlist.RemoveAll(x => x.id == p.id);
                playlist.Insert(0, p);
                EventBus.Emit(GameEvents.PlaylistLoaded, playlist);
                SwitchMode(DisplayMode.Carousel);
                Debug.Log($"[GameManager] 强制展示资料 {p.title}");
            }, err => Debug.LogError("[GameManager] 强制展示失败: " + err));
        }

        void OnPersonalModeStart(object data)
        {
            var result = (FaceRecognizeResult)data;
            currentPersonalUserId = result.userId;
            personalPlaylist = result.profiles;
            SwitchMode(DisplayMode.Personal);
        }

        void OnPersonalModeEnd(object _)
        {
            currentPersonalUserId = -1;
            SwitchMode(DisplayMode.Carousel);
        }
    }
}