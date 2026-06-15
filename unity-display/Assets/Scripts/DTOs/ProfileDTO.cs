using System;
using System.Collections.Generic;

namespace Exhibition.Network
{
    [Serializable]
    public class ApiResult<T>
    {
        public int code;
        public string msg;
        public T data;
        public bool IsSuccess => code == 200;
    }

    [Serializable]
    public class PageResult<T>
    {
        public long total;
        public List<T> records;
    }

    [Serializable]
    public class ProfileDTO
    {
        public long id;
        public long userId;
        public string userName;
        public string avatarUrl;
        public string collegeName;
        public string major;
        public int graduationYear;
        public string title;
        public int category;          // 1荣誉 2作品 3成绩
        public string description;
        public string coverUrl;
        public string achieveDate;
        public string achieveLevel;
        public string issuingOrg;
        public List<MediaDTO> mediaList;
        public List<string> tags;
    }

    [Serializable]
    public class MediaDTO
    {
        public long id;
        public int mediaType;          // 1图片 2视频
        public string fileUrl;
        public string thumbnailUrl;
        public int duration;
    }

    [Serializable]
    public class FaceRecognizeResult
    {
        public bool matched;
        public long userId;
        public string userName;
        public float score;
        public List<ProfileDTO> profiles;
    }

    [Serializable]
    public class FaceRecognizeRequest
    {
        public string deviceId;
        public string featureBase64;
    }
}