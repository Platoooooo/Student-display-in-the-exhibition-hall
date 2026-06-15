using UnityEngine;
using UnityEngine.UI;
using TMPro;
using DG.Tweening;
using Exhibition.Core;
using Exhibition.Network;

namespace Exhibition.UI
{
    /// <summary>
    /// 大屏顶部 HUD:模式切换按钮、通知滚动条、当前时间.
    /// 监听 GameEvents 状态来同步样式.
    /// </summary>
    public class UIManager : MonoBehaviour
    {
        public static UIManager Instance { get; private set; }

        [Header("模式按钮")]
        public Button btnCarousel;
        public Button btnList;
        public Button btnSearch;

        [Header("HUD")]
        public TMP_Text clockText;
        public TMP_Text noticeText;
        public CanvasGroup noticeGroup;

        void Awake()
        {
            if (Instance == null) Instance = this; else Destroy(gameObject);
        }

        void Start()
        {
            if (btnCarousel != null) btnCarousel.onClick.AddListener(() => GameManager.Instance.SwitchMode(DisplayMode.Carousel));
            if (btnList != null) btnList.onClick.AddListener(() => GameManager.Instance.SwitchMode(DisplayMode.List));
            if (btnSearch != null) btnSearch.onClick.AddListener(() => GameManager.Instance.SwitchMode(DisplayMode.Search));

            EventBus.Subscribe("Notice", ShowNotice);
        }

        void Update()
        {
            if (clockText != null)
                clockText.text = System.DateTime.Now.ToString("yyyy-MM-dd  HH:mm:ss");
        }

        public void ShowNotice(object data)
        {
            string text = data as string;
            if (string.IsNullOrEmpty(text) || noticeText == null) return;
            noticeText.text = "📢 " + text;
            if (noticeGroup != null)
            {
                noticeGroup.alpha = 0;
                noticeGroup.DOFade(1, 0.5f);
                DOVirtual.DelayedCall(8f, () => noticeGroup.DOFade(0, 0.6f));
            }
        }
    }
}
