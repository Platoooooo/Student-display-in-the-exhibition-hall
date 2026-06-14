using UnityEngine;
using UnityEngine.Rendering;
using UnityEngine.Rendering.Universal;
using DG.Tweening;
using Exhibition.Core;

namespace Exhibition.Display
{
    /// <summary>
    /// 监听卡片切换事件，做 Bloom 闪烁、暗角呼吸、色彩偏移等增强氛围
    /// </summary>
    public class PostFXController : MonoBehaviour
    {
        public Volume volume;
        private Bloom _bloom;
        private Vignette _vignette;
        private ChromaticAberration _chroma;

        void Start()
        {
            volume.profile.TryGet(out _bloom);
            volume.profile.TryGet(out _vignette);
            volume.profile.TryGet(out _chroma);

            EventBus.Subscribe(GameEvents.CardChanged, _ => FlashEffect());
            EventBus.Subscribe(GameEvents.PersonalModeStart, _ => EnterPersonalFX());
            EventBus.Subscribe(GameEvents.PersonalModeEnd,   _ => ExitPersonalFX());
        }

        void FlashEffect()
        {
            if (_bloom != null)
                DOTween.To(() => _bloom.intensity.value,
                           v => _bloom.intensity.value = v,
                           3f, 0.25f).SetLoops(2, LoopType.Yoyo);

            if (_chroma != null)
                DOTween.To(() => _chroma.intensity.value,
                           v => _chroma.intensity.value = v,
                           0.6f, 0.2f).SetLoops(2, LoopType.Yoyo);
        }

        void EnterPersonalFX()
        {
            if (_vignette != null)
                DOTween.To(() => _vignette.intensity.value,
                           v => _vignette.intensity.value = v,
                           0.5f, 1f);
        }

        void ExitPersonalFX()
        {
            if (_vignette != null)
                DOTween.To(() => _vignette.intensity.value,
                           v => _vignette.intensity.value = v,
                           0.2f, 1f);
        }
    }
}