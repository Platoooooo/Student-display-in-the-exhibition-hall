package com.school.exhibition.modules.face;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.school.exhibition.common.exception.BusinessException;
import com.school.exhibition.modules.face.dto.FaceRegisterRequest;
import com.school.exhibition.modules.face.entity.FaceFeature;
import com.school.exhibition.modules.face.mapper.FaceFeatureMapper;
import com.school.exhibition.modules.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Base64;

@Service
@RequiredArgsConstructor
public class FaceRegisterService {

    private final FaceFeatureMapper faceFeatureMapper;
    private final UserService userService;

    @Transactional
    public void register(FaceRegisterRequest req) {
        Long uid = userService.currentUserId();
        byte[] data = Base64.getDecoder().decode(req.getFeatureBase64());
        if (data.length == 0) throw new BusinessException("特征数据为空");

        FaceFeature exist = faceFeatureMapper.selectOne(
                Wrappers.<FaceFeature>lambdaQuery().eq(FaceFeature::getUserId, uid));
        if (exist == null) {
            FaceFeature f = new FaceFeature();
            f.setUserId(uid);
            f.setFeatureData(data);
            f.setFeatureVersion("arcface_v4");
            f.setFaceImageUrl(req.getFaceImageUrl());
            faceFeatureMapper.insert(f);
        } else {
            exist.setFeatureData(data);
            exist.setFaceImageUrl(req.getFaceImageUrl());
            faceFeatureMapper.updateById(exist);
        }
    }

    public boolean hasRegistered(Long userId) {
        return faceFeatureMapper.selectCount(
                Wrappers.<FaceFeature>lambdaQuery().eq(FaceFeature::getUserId, userId)) > 0;
    }
}
