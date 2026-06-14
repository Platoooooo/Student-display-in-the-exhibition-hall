using System.Collections;
using UnityEngine;
using TMPro;
using DG.Tweening;
using Cinemachine;
using Exhibition.Core;
using Exhibition.Network;

namespace Exhibition.Display
{
    /// <summary>
    /// 人脸识别成功后切换到的个人专属模式
    /// 镜头推近 + 光圈聚焦 + 欢迎语 + 个人作品轮播
    /// </summary>
    public class PersonalView : MonoBehaviour
    {
        [Header("UI 引用")]
        public TMP_Text welcomeText;
        public TMP_Text userNameText;
        public CanvasGroup welcomeGroup;
        public CarouselController personalCarousel;

        [Header("镜头")]
        public CinemachineVirtualCamera personalCam;

        void Start()
        {
            EventBus.Subscribe(GameEvents.PersonalModeStart, OnEnter);
            EventBus.Subscribe(GameEvents.PersonalModeEnd, OnExit);
            gameObject.SetActive(false);
        }

        void OnEnter(object data)
        {
            var result = (FaceRecognizeResult)data;
            gameObject.SetActive(true);

            // 镜头推近
            if (personalCam != null) personalCam.Priority = 20;

            // 欢迎语
            welcomeText.text = "欢迎回来";
            userNameText.text = result.userName;
            welcomeGroup.alpha = 0;
            welcomeGroup.transform.localScale = Vector3.one * 0.6f;

            var seq = DOTween.Sequence();
            seq.Append(welcomeGroup.DOFade(1, 0.6f));
            seq.Join(welcomeGroup.transform.DOScale(1f, 0.6f).SetEase(Ease.OutBack));
            seq.AppendInterval(2.5f);
            seq.Append(welcomeGroup.DOFade(0, 0.5f));
            seq.OnComplete(() =>
            {
                // 加载个人专属轮播
                GameManager.Instance.playlist = result.profiles;
                personalCarousel.StartCarousel();
            });
        }

        void OnExit(object _)
        {
            personalCarousel.StopCarousel();
            if (personalCam != null) personalCam.Priority = 0;
            gameObject.SetActive(false);

            // 回到默认轮播
            GameManager.Instance.LoadPlaylist();
        }
    }
}