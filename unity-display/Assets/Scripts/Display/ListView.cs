using System.Collections.Generic;
using UnityEngine;
using UnityEngine.UI;
using TMPro;
using DG.Tweening;
using Exhibition.Core;
using Exhibition.Network;

namespace Exhibition.Display
{
    /// <summary>
    /// 列表模式: 网格瀑布流展示已上架资料,支持滚动+点击进入详情.
    /// 适合操作人员触屏浏览全部内容.
    /// </summary>
    public class ListView : MonoBehaviour
    {
        [Header("UI")]
        public ScrollRect scrollRect;
        public Transform itemContainer;
        public GameObject itemPrefab;            // 子项 prefab,带 RawImage cover + TMP_Text title + Button
        public TMP_Text titleLabel;              // 顶部标题 "全部成果"
        public TMP_Dropdown categoryFilter;      // 类目筛选

        [Header("分页")]
        public int pageSize = 24;
        private int _page = 1;
        private bool _loading = false;
        private bool _noMore = false;
        private int? _category = null;

        void OnEnable()
        {
            ClearItems();
            _page = 1; _noMore = false;
            LoadMore();
            if (categoryFilter != null) categoryFilter.onValueChanged.AddListener(OnCategoryChanged);
            if (scrollRect != null) scrollRect.onValueChanged.AddListener(OnScroll);
        }

        void OnDisable()
        {
            if (categoryFilter != null) categoryFilter.onValueChanged.RemoveListener(OnCategoryChanged);
            if (scrollRect != null) scrollRect.onValueChanged.RemoveListener(OnScroll);
        }

        void OnCategoryChanged(int idx)
        {
            // 0=全部,1-5=类目码
            _category = idx == 0 ? (int?)null : idx;
            ClearItems();
            _page = 1; _noMore = false;
            LoadMore();
        }

        void OnScroll(Vector2 v)
        {
            // 滚到底部 5% 时加载下一页
            if (!_loading && !_noMore && v.y < 0.05f) LoadMore();
        }

        void LoadMore()
        {
            _loading = true;
            ApiClient.Instance.GetList(_page, pageSize, _category,
                page =>
                {
                    if (page == null || page.records == null || page.records.Count == 0)
                    {
                        _noMore = true; _loading = false; return;
                    }
                    foreach (var p in page.records) AddItem(p);
                    if (itemContainer.childCount >= page.total) _noMore = true;
                    _page++;
                    _loading = false;
                },
                err => { _loading = false; Debug.LogError("[ListView] 加载失败: " + err); });
        }

        void AddItem(ProfileDTO p)
        {
            var go = Instantiate(itemPrefab, itemContainer);
            var card = go.GetComponent<ListItemCard>();
            if (card != null) card.Bind(p);

            // 入场动画
            go.transform.localScale = Vector3.zero;
            go.transform.DOScale(1, 0.4f).SetEase(Ease.OutBack)
                .SetDelay(itemContainer.childCount * 0.03f);
        }

        void ClearItems()
        {
            for (int i = itemContainer.childCount - 1; i >= 0; i--)
                Destroy(itemContainer.GetChild(i).gameObject);
        }
    }
}
