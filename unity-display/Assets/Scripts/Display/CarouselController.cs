using System.Collections;
using UnityEngine;
using DG.Tweening;
using Exhibition.Core;
using Exhibition.Network;

namespace Exhibition.Display
{
    public class CarouselController : MonoBehaviour
    {
        [Header("配置")]
        public float interval = 8f;            // 切换间隔
        public float transitionDuration = 0.8f;

        [Header("引用")]
        public ProfileCard cardA;              // 双卡片交叉切换
        public ProfileCard cardB;
        public ParticleSystem transitionParticle;
        public Camera mainCamera;

        private ProfileCard _currentCard;
        private ProfileCard _nextCard;
        private int _index = 0;
        private Coroutine _loop;

        void Start()
        {
            _currentCard = cardA;
            _nextCard = cardB;
            cardA.gameObject.SetActive(true);
            cardB.gameObject.SetActive(false);

            EventBus.Subscribe(GameEvents.PlaylistLoaded, _ => StartCarousel());
        }

        public void StartCarousel()
        {
            if (_loop != null) StopCoroutine(_loop);
            _loop = StartCoroutine(CarouselLoop());
        }

        public void StopCarousel()
        {
            if (_loop != null) StopCoroutine(_loop);
        }

        IEnumerator CarouselLoop()
        {
            var list = GameManager.Instance.playlist;
            if (list == null || list.Count == 0) yield break;

            // 首张直接展示
            _currentCard.Bind(list[0]);
            _currentCard.PlayEnterAnimation();
            yield return new WaitForSeconds(interval);

            while (true)
            {
                _index = (_index + 1) % list.Count;
                yield return SwitchTo(list[_index]);
                yield return new WaitForSeconds(interval);
            }
        }

        IEnumerator SwitchTo(ProfileDTO profile)
        {
            // 1) 准备下一张
            _nextCard.gameObject.SetActive(true);
            _nextCard.Bind(profile);
            _nextCard.transform.localScale = Vector3.zero;
            _nextCard.transform.localEulerAngles = new Vector3(0, 90, 0);

            // 2) 粒子转场
            if (transitionParticle != null) transitionParticle.Play();

            // 3) 镜头轻微震动 + Bloom 闪烁（由 PostFXController 监听事件实现）
            mainCamera.DOShakePosition(0.3f, 0.1f, 10, 90);
            EventBus.Emit(GameEvents.CardChanged, profile);

            // 4) 旧卡片飞出
            var seq = DOTween.Sequence();
            seq.Append(_currentCard.transform.DORotate(new Vector3(0, -90, 0), transitionDuration).SetEase(Ease.InCubic));
            seq.Join(_currentCard.transform.DOScale(0.5f, transitionDuration));
            seq.Join(_currentCard.GetCanvasGroup().DOFade(0, transitionDuration));

            // 5) 新卡片飞入
            seq.Append(_nextCard.transform.DORotate(Vector3.zero, transitionDuration).SetEase(Ease.OutCubic));
            seq.Join(_nextCard.transform.DOScale(1f, transitionDuration));

            yield return seq.WaitForCompletion();

            _currentCard.gameObject.SetActive(false);
            _nextCard.PlayEnterAnimation();

            // 交换引用
            (_currentCard, _nextCard) = (_nextCard, _currentCard);
        }
    }
}