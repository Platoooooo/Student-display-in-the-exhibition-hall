using System;
using System.Runtime.InteropServices;
using System.Security.Cryptography;
using UnityEngine;

namespace Exhibition.FaceRecognition
{
    /// <summary>
    /// 虹软 ArcFace SDK 的 C# 封装。
    /// - useMock = true: Mock 模式,基于图像哈希生成确定性特征,无需 SDK 即可联调
    /// - useMock = false: 调用真实 ArcFace 4.x dll
    /// </summary>
    public static class ArcFaceWrapper
    {
        private const string DLL = "libarcsoft_face_engine";
        private static IntPtr _engine = IntPtr.Zero;
        private static bool _initialized = false;
        private static bool _useMock = true;

        public const int FEATURE_SIZE = 1032;

        // ===== DllImport 占位（以官方 SDK 为准） =====
        [DllImport(DLL)] private static extern int ASFActivation(string appId, string sdkKey);
        [DllImport(DLL)] private static extern int ASFInitEngine(int detectMode, int orientPriority,
            int scale, int maxFaceNum, int combinedMask, out IntPtr engine);
        [DllImport(DLL)] private static extern int ASFUninitEngine(IntPtr engine);

        public static bool IsInitialized => _initialized;
        public static bool UseMock { get => _useMock; set => _useMock = value; }

        public static bool Init(string appId, string sdkKey, bool useMock = true)
        {
            _useMock = useMock;
            if (_initialized) return true;
            if (_useMock)
            {
                Debug.LogWarning("[ArcFace] Mock 模式运行");
                _initialized = true;
                return true;
            }
            try
            {
                int rc = ASFActivation(appId, sdkKey);
                Debug.Log($"[ArcFace] 激活: {rc}");
                rc = ASFInitEngine(1, 1, 16, 5, 0x00000005, out _engine);
                _initialized = (rc == 0);
                return _initialized;
            }
            catch (Exception e)
            {
                Debug.LogError("[ArcFace] 初始化异常: " + e.Message);
                return false;
            }
        }

        /// <summary>
        /// 输入纹理 → 输出 1032 字节特征（无人脸返回 null）
        /// </summary>
        public static byte[] DetectAndExtract(Texture2D tex)
        {
            if (!_initialized || tex == null) return null;
            if (_useMock) return MockExtract(tex);
            // TODO: 真实 SDK 调用
            return null;
        }

        /// <summary>
        /// Mock: 基于图像哈希生成确定性特征,同一人不同帧返回相近特征
        /// </summary>
        private static byte[] MockExtract(Texture2D tex)
        {
            int w = tex.width, h = tex.height;
            var center = tex.GetPixel(w / 2, h / 2);
            if (center.grayscale < 0.05f) return null;

            var hash = ComputeHash(tex);
            int seed = BitConverter.ToInt32(hash, 0);
            var rng = new System.Random(seed);
            var feature = new byte[FEATURE_SIZE];
            for (int i = 0; i < FEATURE_SIZE / 4; i++)
            {
                float v = (float)(rng.NextDouble() * 2 - 1);
                Buffer.BlockCopy(BitConverter.GetBytes(v), 0, feature, i * 4, 4);
            }
            return feature;
        }

        private static byte[] ComputeHash(Texture2D tex)
        {
            int n = 16;
            byte[] buf = new byte[n * n];
            float dx = (float)tex.width / n, dy = (float)tex.height / n;
            for (int y = 0; y < n; y++)
                for (int x = 0; x < n; x++)
                {
                    var c = tex.GetPixel((int)(x * dx), (int)(y * dy));
                    buf[y * n + x] = (byte)(c.grayscale * 255);
                }
            using var md5 = MD5.Create();
            return md5.ComputeHash(buf);
        }

        public static void Release()
        {
            if (_engine != IntPtr.Zero)
            {
                try { ASFUninitEngine(_engine); } catch { }
                _engine = IntPtr.Zero;
            }
            _initialized = false;
        }
    }
}