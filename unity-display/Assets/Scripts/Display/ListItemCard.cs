using System.Collections;
using UnityEngine;
using UnityEngine.UI;
using UnityEngine.Networking;
using UnityEngine.EventSystems;
using TMPro;
using Exhibition.Core;
using Exhibition.Network;

namespace Exhibition.Display
{
    /// <summary>
    /// ListView / SearchView 中的小卡片,显示封面+标题+作者,点击触发详情查看.
    /// </summary>
    public class ListItemCard : MonoBehaviour, IPointerClickHandler
    {
        public RawImage cover;
        public TMP_Text title;
        public TMP_Text userName;
        public TMP_Text categoryTag;

        private ProfileDTO _profile;

        public void Bind(ProfileDTO p)
        {
            _profile = p;
            if (title != null) title.text = p.title;
            if (userName != null) userName.text = $"{p.userName ?? ""} · {p.collegeName ?? ""}";
            if (categoryTag != null)
            {
                string[] cats = { "", "荣誉", "作品", "成绩", "活动", "其他" };
                categoryTag.text = (p.category > 0 && p.category < cats.Length) ? cats[p.category] : "";
            }
            if (!string.IsNullOrEmpty(p.coverUrl) && cover != null)
                StartCoroutine(LoadCover(p.coverUrl));
        }

        IEnumerator LoadCover(string url)
        {
            using var req = UnityWebRequestTexture.GetTexture(url);
            yield return req.SendWebRequest();
            if (req.result == UnityWebRequest.Result.Success && cover != null)
                cover.texture = ((DownloadHandlerTexture)req.downloadHandler).texture;
        }

        public void OnPointerClick(PointerEventData e)
        {
            if (_profile != null) EventBus.Emit("ListItemClicked", _profile);
        }
    }
}
