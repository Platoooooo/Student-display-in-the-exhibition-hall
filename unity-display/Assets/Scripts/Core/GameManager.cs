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
            carouselRoot.SetActive(mode == DisplayMode.Carousel);
            listRoot.SetActive(mode == DisplayMode.List);
            searchRoot.SetActive(mode == DisplayMode.Search);
            personalRoot.SetActive(mode == DisplayMode.Personal);
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