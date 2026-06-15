using UnityEngine;
using UnityEngine.UI;
using TMPro;
using Exhibition.Core;
using Exhibition.Network;

namespace Exhibition.Display
{
    /// <summary>
    /// 搜索模式: 输入关键字调用 /api/display/search,以网格形式列出命中结果.
    /// </summary>
    public class SearchView : MonoBehaviour
    {
        [Header("UI")]
        public TMP_InputField inputField;
        public Button searchBtn;
        public Transform resultContainer;
        public GameObject itemPrefab;
        public TMP_Text emptyTip;

        void OnEnable()
        {
            if (searchBtn != null) searchBtn.onClick.AddListener(OnSearch);
            if (inputField != null) inputField.onSubmit.AddListener(_ => OnSearch());
            ShowEmpty(true, "请输入关键字搜索");
        }

        void OnDisable()
        {
            if (searchBtn != null) searchBtn.onClick.RemoveListener(OnSearch);
            if (inputField != null) inputField.onSubmit.RemoveAllListeners();
        }

        void OnSearch()
        {
            string kw = inputField != null ? inputField.text.Trim() : "";
            if (string.IsNullOrEmpty(kw)) { ShowEmpty(true, "请输入关键字"); return; }
            Clear();
            ApiClient.Instance.Search(kw,
                list =>
                {
                    if (list == null || list.Count == 0)
                    {
                        ShowEmpty(true, $"没有找到「{kw}」相关成果");
                        return;
                    }
                    ShowEmpty(false, "");
                    foreach (var p in list) AddItem(p);
                },
                err => ShowEmpty(true, "搜索失败: " + err));
        }

        void AddItem(ProfileDTO p)
        {
            var go = Instantiate(itemPrefab, resultContainer);
            var card = go.GetComponent<ListItemCard>();
            if (card != null) card.Bind(p);
        }

        void Clear()
        {
            for (int i = resultContainer.childCount - 1; i >= 0; i--)
                Destroy(resultContainer.GetChild(i).gameObject);
        }

        void ShowEmpty(bool show, string text)
        {
            if (emptyTip != null) { emptyTip.gameObject.SetActive(show); emptyTip.text = text; }
        }
    }
}
