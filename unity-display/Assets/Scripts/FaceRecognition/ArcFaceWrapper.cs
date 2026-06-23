using System;
using System.Runtime.InteropServices;
using System.Security.Cryptography;
using UnityEngine;

namespace Exhibition.FaceRecognition
{
    /// <summary>
    /// 虹软 ArcFace 4.x SDK 的 C# 封装。
    /// - useMock = true: Mock 模式,基于图像哈希生成确定性特征,无需 SDK 即可联调
    /// - useMock = false: 调用真实 ArcFace 4.x dll (Windows x64)
    ///
    /// 放置 dll: Assets/Plugins/x86_64/libarcsoft_face_engine.dll + libarcsoft_face.dll
    /// 详见 Assets/Plugins/README.md
    /// </summary>
    public static class ArcFaceWrapper
    {
        private const string DLL = "libarcsoft_face_engine";
        private const int ASVL_PAF_RGB24_B8G8R8 = 0x201;

        // ====== 状态 ======
        private static IntPtr _engine = IntPtr.Zero;
        private static bool _initialized = false;
        private static bool _useMock = true;
        private static string _appId = "";
        private static string _sdkKey = "";

        public const int FEATURE_SIZE = 1032;          // 258 float * 4 byte
        public const int MAX_FACE_NUM = 5;

        // ====== DllImport ======
        // 4.0 签名: MRESULT ASFActivation(LPBYTE appId, LPBYTE sdkKey)
        [DllImport(DLL, CallingConvention = CallingConvention.Cdecl)]
        private static extern int ASFActivation(byte[] appId, byte[] sdkKey);

        // 4.0 签名: MRESULT ASFInitEngine(ASF_DETECT_MODE, ASF_OrientPriority, int, ASF_MAX_FACE_NUM, MInt32 combinedMask, MHandle*)
        [DllImport(DLL, CallingConvention = CallingConvention.Cdecl)]
        private static extern int ASFInitEngine(
            int detectMode, int orientPriority, int scale,
            int maxFaceNum, int combinedMask, out IntPtr engine);

        [DllImport(DLL, CallingConvention = CallingConvention.Cdecl)]
        private static extern int ASFUninitEngine(IntPtr engine);

        // MRESULT ASFDetectFaces(MHandle, LPASF_ImageData, LPASF_MultiFaceInfo)
        [DllImport(DLL, CallingConvention = CallingConvention.Cdecl)]
        private static extern int ASFDetectFaces(IntPtr engine, IntPtr imgData, IntPtr multiFaceInfo);

        // MRESULT ASFFaceFeatureExtract(MHandle, LPASF_ImageData, LPASF_SingleFaceInfo, LPASF_FaceFeature)
        [DllImport(DLL, CallingConvention = CallingConvention.Cdecl)]
        private static extern int ASFFaceFeatureExtract(IntPtr engine, IntPtr imgData, IntPtr singleFaceInfo, IntPtr feature);

        public static bool IsInitialized => _initialized;
        public static bool UseMock { get => _useMock; set => _useMock = value; }

        /// <summary>
        /// 初始化 SDK。useMock=true 时跳过真实调用,直接进入 Mock 模式。
        /// </summary>
        public static bool Init(string appId, string sdkKey, bool useMock = true)
        {
            _appId = appId ?? "";
            _sdkKey = sdkKey ?? "";
            _useMock = useMock;
            if (_initialized) return true;

            if (_useMock)
            {
                Debug.LogWarning("[ArcFace] Mock 模式运行（不调用真实 SDK）");
                _initialized = true;
                return true;
            }

            try
            {
                int rc = ASFActivation(StringToBytes(_appId), StringToBytes(_sdkKey));
                Debug.Log($"[ArcFace] 激活 rc={rc}");
                if (rc != 0)
                {
                    Debug.LogError($"[ArcFace] 激活失败 rc={rc}（appId/sdkKey 错或离线授权缺失）");
                    return false;
                }

                // detectMode=1 (VIDEO), orient=1 (ASF_OP_0_ONLY), scale=16, maxFace=5
                // combinedMask = 0x00000001 (FACE_DETECT) | 0x00000004 (FACE_RECOGNITION) = 0x5
                rc = ASFInitEngine(1, 1, 16, MAX_FACE_NUM, 0x00000005, out _engine);
                Debug.Log($"[ArcFace] InitEngine rc={rc}");
                _initialized = (rc == 0 && _engine != IntPtr.Zero);
                if (!_initialized) Debug.LogError("[ArcFace] InitEngine 失败");
                return _initialized;
            }
            catch (DllNotFoundException)
            {
                Debug.LogError("[ArcFace] DllNotFoundException: 请检查 Assets/Plugins/x86_64/ 下是否有 libarcsoft_face_engine.dll");
                return false;
            }
            catch (BadImageFormatException)
            {
                Debug.LogError("[ArcFace] BadImageFormatException: 32/64 位不匹配,确认使用 x64 SDK");
                return false;
            }
            catch (Exception e)
            {
                Debug.LogError("[ArcFace] 初始化异常: " + e);
                return false;
            }
        }

        /// <summary>
        /// 输入 BGR 字节数组(RGB24 B8G8R8) + 宽高 → 输出 1032 字节特征。
        /// 无人脸或初始化失败返回 null。
        /// </summary>
        public static byte[] DetectAndExtractBGR(byte[] bgr, int width, int height)
        {
            if (!_initialized) return null;
            if (_useMock)
            {
                // Mock 模式用 bgr 中心灰度代替
                if (bgr == null || bgr.Length < 3) return null;
                byte g = bgr[(bgr.Length / 2) % bgr.Length];
                if (g < 5) return null;
                return MockExtractFromHash(bgr);
            }
            return RealExtract(bgr, width, height);
        }

        /// <summary>
        /// 输入 Texture2D(RGB24) → 输出 1032 字节特征。
        /// 兼容 FaceDetector.cs 现有调用,内部转 BGR。
        /// </summary>
        public static byte[] DetectAndExtract(Texture2D tex)
        {
            if (!_initialized || tex == null) return null;

            // 1) Texture2D → byte[] (BGRA/RGBA 转换)
            Color32[] pixels = tex.GetPixels32();
            int w = tex.width, h = tex.height;
            byte[] bgr = new byte[w * h * 3];
            // Color32 顺序: RGBA8888; ArcFace 要 BGR24
            for (int i = 0, j = 0; i < pixels.Length; i++, j += 3)
            {
                bgr[j]     = pixels[i].b;  // B
                bgr[j + 1] = pixels[i].g;  // G
                bgr[j + 2] = pixels[i].r;  // R
            }
            return DetectAndExtractBGR(bgr, w, h);
        }

        // ====== 真实 SDK 调用 ======
        private static byte[] RealExtract(byte[] bgr, int width, int height)
        {
            if (_engine == IntPtr.Zero) return null;

            IntPtr imgDataPtr = IntPtr.Zero;
            IntPtr faceInfoPtr = IntPtr.Zero;
            IntPtr featurePtr = IntPtr.Zero;
            GCHandle pinBgr = GCHandle.Alloc(bgr, GCHandleType.Pinned);

            try
            {
                int stride = width * 3;
                IntPtr plane0 = pinBgr.AddrOfPinnedObject();

                // ASF_ImageData: 16 字节(4 int) + 4 IntPtr(ppu8Plane) + 4 int(pi32Pitch) = 48 字节
                byte[] imgDataBuf = new byte[48];
                WriteInt32(imgDataBuf, 0, width);
                WriteInt32(imgDataBuf, 4, height);
                WriteInt32(imgDataBuf, 8, ASVL_PAF_RGB24_B8G8R8);
                WriteInt32(imgDataBuf, 12, 0);
                WriteIntPtr(imgDataBuf, 16, plane0);  // ppu8Plane[0]
                WriteInt32(imgDataBuf, 48 - 16, stride);  // pi32Pitch[0]  (偏移 32 = 16+16)
                // 注: ppu8Plane 占 4*8=32 字节在 64 位上,pi32Pitch 跟在后面
                // 实际偏移需按目标平台指针大小调整
                imgDataPtr = Marshal.AllocHGlobal(48);
                Marshal.Copy(imgDataBuf, 0, imgDataPtr, 48);

                // ASF_MultiFaceInfo: nFace(4) + reserved(4) + rcFace[10]*MRECT(16*10=160) + lfaceOrient[10]*4(40) = 208
                faceInfoPtr = Marshal.AllocHGlobal(208);
                Marshal.WriteInt32(faceInfoPtr, 0, 0);

                int rc = ASFDetectFaces(_engine, imgDataPtr, faceInfoPtr);
                if (rc != 0)
                {
                    Debug.LogWarning($"[ArcFace] ASFDetectFaces rc={rc}");
                    return null;
                }

                int nFace = Marshal.ReadInt32(faceInfoPtr, 0);
                if (nFace <= 0) return null;

                // 读第 0 张脸的 MRECT faceRect (16 字节, MRECT = {left, top, right, bottom} 各 4 字节)
                int rectOffset = 8;  // 跳过 nFace + reserved
                int left   = Marshal.ReadInt32(faceInfoPtr, rectOffset + 0);
                int top    = Marshal.ReadInt32(faceInfoPtr, rectOffset + 4);
                int right  = Marshal.ReadInt32(faceInfoPtr, rectOffset + 8);
                int bottom = Marshal.ReadInt32(faceInfoPtr, rectOffset + 12);

                // ASF_SingleFaceInfo: MRECT(16) + lfaceOrient(4) + reserved(4) = 24 字节
                IntPtr singlePtr = Marshal.AllocHGlobal(24);
                Marshal.WriteInt32(singlePtr, 0, left);
                Marshal.WriteInt32(singlePtr, 4, top);
                Marshal.WriteInt32(singlePtr, 8, right);
                Marshal.WriteInt32(singlePtr, 12, bottom);
                Marshal.WriteInt32(singlePtr, 16, Marshal.ReadInt32(faceInfoPtr, 8 + 160));  // lfaceOrient[0]

                // ASF_FaceFeature: feature(IntPtr 8) + lFeatureSize(4) = 16 字节
                featurePtr = Marshal.AllocHGlobal(16);
                Marshal.WriteIntPtr(featurePtr, IntPtr.Zero);
                Marshal.WriteInt32(featurePtr, 8, FEATURE_SIZE);

                rc = ASFFaceFeatureExtract(_engine, imgDataPtr, singlePtr, featurePtr);
                Marshal.FreeHGlobal(singlePtr);

                if (rc != 0)
                {
                    Debug.LogWarning($"[ArcFace] ASFFaceFeatureExtract rc={rc}");
                    return null;
                }

                // 读出特征: 258 个 float = 1032 字节
                IntPtr featureBuf = Marshal.ReadIntPtr(featurePtr, 0);
                if (featureBuf == IntPtr.Zero) return null;

                byte[] result = new byte[FEATURE_SIZE];
                Marshal.Copy(featureBuf, result, 0, FEATURE_SIZE);
                return result;
            }
            catch (Exception e)
            {
                Debug.LogError("[ArcFace] RealExtract 异常: " + e);
                return null;
            }
            finally
            {
                if (pinBgr.IsAllocated) pinBgr.Free();
                if (imgDataPtr != IntPtr.Zero) Marshal.FreeHGlobal(imgDataPtr);
                if (faceInfoPtr != IntPtr.Zero) Marshal.FreeHGlobal(faceInfoPtr);
                if (featurePtr != IntPtr.Zero) Marshal.FreeHGlobal(featurePtr);
            }
        }

        // ====== Mock 实现 ======
        private static byte[] MockExtractFromHash(byte[] data)
        {
            using var md5 = MD5.Create();
            byte[] hash = md5.ComputeHash(data);
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

        public static void Release()
        {
            if (_engine != IntPtr.Zero)
            {
                try { ASFUninitEngine(_engine); } catch { }
                _engine = IntPtr.Zero;
            }
            _initialized = false;
        }

        // ====== 工具方法 ======
        private static byte[] StringToBytes(string s)
        {
            // 虹软 SDK 要求 LPBYTE = ANSI 单字节字符串
            return System.Text.Encoding.ASCII.GetBytes(s);
        }

        private static void WriteInt32(byte[] buf, int offset, int v)
        {
            buf[offset]     = (byte)(v & 0xFF);
            buf[offset + 1] = (byte)((v >> 8) & 0xFF);
            buf[offset + 2] = (byte)((v >> 16) & 0xFF);
            buf[offset + 3] = (byte)((v >> 24) & 0xFF);
        }

        private static void WriteIntPtr(byte[] buf, int offset, IntPtr p)
        {
            if (IntPtr.Size == 8)
            {
                long v = p.ToInt64();
                for (int i = 0; i < 8; i++) buf[offset + i] = (byte)((v >> (i * 8)) & 0xFF);
            }
            else
            {
                int v = p.ToInt32();
                WriteInt32(buf, offset, v);
            }
        }
    }
}
