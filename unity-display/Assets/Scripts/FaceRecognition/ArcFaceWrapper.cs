using System;
using System.Runtime.InteropServices;
using UnityEngine;

namespace Exhibition.FaceRecognition
{
    /// <summary>
    /// 虹软 ArcFace SDK 的 C# 封装（伪代码骨架）
    /// 实际开发需根据虹软提供的 dll 与函数签名实现 DllImport
    /// 官方文档：https://ai.arcsoft.com.cn/manual/arcface.html
    /// </summary>
    public static class ArcFaceWrapper
    {
        private const string DLL = "libarcsoft_face_engine";
        private static IntPtr _engine = IntPtr.Zero;
        private static bool _initialized = false;

        // ===== DllImport 示例（具体签名以官方为准） =====
        [DllImport(DLL)]
        private static extern int ASFActivation(string appId, string sdkKey);

        [DllImport(DLL)]
        private static extern int ASFInitEngine(int detectMode, int orientPriority, int scale,
            int maxFaceNum, int combinedMask, out IntPtr engine);

        // ... 其他函数：ASFDetectFaces / ASFFaceFeatureExtract 等

        public static bool Init(string appId, string sdkKey)
        {
            if (_initialized) return true;
            try
            {
                int rc = ASFActivation(appId, sdkKey);
                Debug.Log($"[ArcFace] 激活返回 {rc}");
                rc = ASFInitEngine(1, 1, 16, 5, 0x00000005, out _engine);
                _initialized = (rc == 0);
                return _initialized;
            }
            catch (Exception e)
            {
                Debug.LogError("[ArcFace] 初始化失败: " + e.Message);
                return false;
            }
        }

        /// <summary>
        /// 输入纹理 → 输出 1032 字节人脸特征向量（无人脸返回 null）
        /// 真实实现需要：BGR 转换 → 调用 ASFDetectFaces → ASFFaceFeatureExtract
        /// </summary>
        public static byte[] DetectAndExtract(Texture2D tex)
        {
            if (!_initialized) return null;

            // TODO: 调用 SDK 完整流程
            // 此处为占位实现，实际需 Marshal 内存交互
            return null;
        }

        public static void Release()
        {
            if (_engine != IntPtr.Zero)
            {
                // ASFUninitEngine(_engine);
                _engine = IntPtr.Zero;
                _initialized = false;
            }
        }
    }
}