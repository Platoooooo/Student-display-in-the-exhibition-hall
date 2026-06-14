using System.Collections;
using UnityEngine;
using UnityEngine.UI;
using UnityEngine.Networking;
using TMPro;
using DG.Tweening;
using Exhibition.Network;

namespace Exhibition.Display
{
    public class ProfileCard : MonoBehaviour
    {
        [Header("UI 引用")]
        public RawImage coverImage;
        public TMP_Text titleText;
        public TMP_Text userNameText;
        public TMP_Text collegeText;
        public TMP_Text descriptionText;
        public TMP_Text dateText;
        public Transform tagContainer;
        public GameObject tagPrefab;

        private CanvasGroup _cg;
        private RectTransform _coverRect;
        private Vector2 _coverInitPos;
        private Vector3 _coverInitScale;

        public CanvasGroup GetCanvasGroup()
        {
            if (_cg == null) _cg = GetComponent<CanvasGroup>() ?? gameObject.AddComponent<CanvasGroup>();
            return _cg;
        }

        void Awake()
        {
                        _coverRect = coverImage.rectTransform;
            _coverInitPos = _coverRect.anchoredPosition;
            _coverInitScale = _coverRect.localScale;
        }

        public void Bind(ProfileDTO profile)
        {
            titleText.text = profile.title;
            userNameText.text = profile.userName;
            collegeText.text = $"{profile.collegeName} · {profile.major}";
            descriptionText.text = profile.description;
            dateText.text = profile.achieveDate;

            // 标签
            foreach (Transform t in tagContainer) Destroy(t.gameObject);
            if (profile.tags != null)
            {
                foreach (var tag in profile.tags)
                {
                    var go = Instantiate(tagPrefab, tagContainer);
                    go.GetComponentInChildren<TMP_Text>().text = tag;
                }
            }

            // 异步加载封面
            if (!string.IsNullOrEmpty(profile.coverUrl))
                StartCoroutine(LoadImage(profile.coverUrl));
        }

        IEnumerator LoadImage(string url)
        {
            using var req = UnityWebRequestTexture.GetTexture(url);
            yield return req.SendWebRequest();
            if (req.result == UnityWebRequest.Result.Success)
            {
                coverImage.texture = ((DownloadHandlerTexture)req.downloadHandler).texture;
            }
        }

        /// <summary>
        /// 入场动画：KenBurns 缓推 + 文字打字机 + 标签错落弹出
        /// </summary>
        public void PlayEnterAnimation()
        {
            GetCanvasGroup().alpha = 0;
            GetCanvasGroup().DOFade(1, 0.5f);

            // KenBurns：图片缓慢放大并轻微平移
            _coverRect.localScale = _coverInitScale;
            _coverRect.anchoredPosition = _coverInitPos;
            _coverRect.DOScale(_coverInitScale * 1.15f, 8f).SetEase(Ease.Linear);
            _coverRect.DOAnchorPos(_coverInitPos + new Vector2(30, -20), 8f).SetEase(Ease.Linear);

            // 标题打字机
            StartCoroutine(TypeWriter(titleText, titleText.text, 0.04f));

            // 标签错落弹出
            for (int i = 0; i < tagContainer.childCount; i++)
            {
                var t = tagContainer.GetChild(i);
                t.localScale = Vector3.zero;
                t.DOScale(1, 0.4f).SetDelay(0.5f + i * 0.1f).SetEase(Ease.OutBack);
            }

            // 描述区淡入上滑
            descriptionText.alpha = 0;
            descriptionText.rectTransform.anchoredPosition += new Vector2(0, -30);
            descriptionText.DOFade(1, 0.6f).SetDelay(0.8f);
            descriptionText.rectTransform.DOAnchorPosY(
                descriptionText.rectTransform.anchoredPosition.y + 30, 0.6f).SetDelay(0.8f);
        }

        IEnumerator TypeWriter(TMP_Text label, string fullText, float speed)
        {
            label.text = "";
            foreach (var ch in fullText)
            {
                label.text += ch;
                yield return new WaitForSeconds(speed);
            }
        }
    }
}