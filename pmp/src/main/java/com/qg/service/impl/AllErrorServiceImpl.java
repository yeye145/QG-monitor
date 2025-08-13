package com.qg.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.qg.domain.*;
import com.qg.domain.Module;
import com.qg.domain.MobileError;
import com.qg.mapper.*;
import com.qg.parse.SourceMapService;
import com.qg.service.AllErrorService;
import com.qg.vo.BackendResponsibilityVO;
import com.qg.vo.FrontendErrorSourceCodeVO;
import com.qg.vo.FrontendResponsibilityVO;
import com.qg.vo.MobileResponsibilityVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

import static com.qg.domain.Code.*;

/**
 * @Description: 所有错误应用类  // 类说明
 * @ClassName: AllErrorServiceimpl    // 类名
 * @Author: lrt          // 创建者
 * @Date: 2025/8/8 11:00   // 时间
 * @Version: 1.0     // 版本
 */
@Service
@Slf4j
public class AllErrorServiceImpl implements AllErrorService {
    @Autowired
    private BackendErrorMapper backendErrorMapper;

    @Autowired
    private FrontendErrorMapper frontendErrorMapper;

    @Autowired
    private MobileErrorMapper mobileErrorMapper;

    @Autowired
    private ModuleMapper moduleMapper;

    @Autowired
    private ResponsibilityMapper responsibilityMapper;

    @Autowired
    private UsersMapper usersMapper;

    @Autowired
    private SourcemapFilesMapper sourcemapFilesMapper;



@Override
public Result selectByCondition(String projectId, Long moduleId, String type) {
    // 参数校验
    if (projectId == null || projectId.isEmpty()) {
        return new Result(BAD_REQUEST, "项目ID不能为空");
    }

    try {
        // 查询条件构建
        LambdaQueryWrapper<BackendError> backendQueryWrapper = new LambdaQueryWrapper<>();
        LambdaQueryWrapper<FrontendError> frontendQueryWrapper = new LambdaQueryWrapper<>();
        LambdaQueryWrapper<MobileError> mobileQueryWrapper = new LambdaQueryWrapper<>();

        // 处理模块条件
        if (moduleId != null) {
            Module module = moduleMapper.selectById(moduleId);
            if (module != null) {
                backendQueryWrapper.eq(BackendError::getModule, module.getModuleName());
            }
        }

        // 添加项目ID条件
        backendQueryWrapper.eq(BackendError::getProjectId, projectId);
        frontendQueryWrapper.eq(FrontendError::getProjectId, projectId);
        mobileQueryWrapper.eq(MobileError::getProjectId, projectId);

        // 添加错误类型条件
        if (type != null && !type.isEmpty()) {
            backendQueryWrapper.eq(BackendError::getErrorType, type);
            frontendQueryWrapper.eq(FrontendError::getErrorType, type);
            mobileQueryWrapper.eq(MobileError::getErrorType, type);
        }

        // 执行查询
        List<BackendError> backendErrors = backendErrorMapper.selectList(backendQueryWrapper);
        List<FrontendError> frontendErrors = frontendErrorMapper.selectList(frontendQueryWrapper);
        List<MobileError> mobileErrors = mobileErrorMapper.selectList(mobileQueryWrapper);

        // 查询责任人信息（一次性查询，避免在循环中多次查询）
        List<Responsibility> allResponsibilities = responsibilityMapper.selectList(
                new LambdaQueryWrapper<Responsibility>().eq(Responsibility::getProjectId, projectId)
        );

        // 分类处理责任人信息
        Map<String, List<Responsibility>> responsibilityByPlatform = allResponsibilities.stream()
                .collect(Collectors.groupingBy(Responsibility::getPlatform));

        List<Responsibility> backendResponsibilities = responsibilityByPlatform.getOrDefault("backend", new ArrayList<>());
        List<Responsibility> frontendResponsibilities = responsibilityByPlatform.getOrDefault("frontend", new ArrayList<>());
        List<Responsibility> mobileResponsibilities = responsibilityByPlatform.getOrDefault("mobile", new ArrayList<>());

        // 获取所有相关的用户信息（一次性查询）
        Set<Long> userIds = allResponsibilities.stream()
                .filter(r -> r.getResponsibleId() != null)
                .map(Responsibility::getResponsibleId)
                .collect(Collectors.toSet());

        Map<Long, Users> userMap = new HashMap<>();
        if (!userIds.isEmpty()) {
            List<Users> usersList = usersMapper.selectBatchIds(userIds);
            userMap = usersList.stream().collect(Collectors.toMap(Users::getId, u -> u));
        }

        // 处理后端错误数据
        List<BackendResponsibilityVO> backendResponsibilityVOList = processBackendErrors(backendErrors, backendResponsibilities, userMap);

        // 处理前端错误数据
        List<FrontendResponsibilityVO> frontendResponsibilityVOList = processFrontendErrors(frontendErrors, frontendResponsibilities, userMap);

        // 处理移动端错误数据
        List<MobileResponsibilityVO> mobileResponsibilityVOList = processMobileErrors(mobileErrors, mobileResponsibilities, userMap);

        return new Result(Code.SUCCESS,
                Arrays.asList(backendResponsibilityVOList, frontendResponsibilityVOList, mobileResponsibilityVOList),
                "查询成功");

    } catch (Exception e) {
        log.error("查询错误信息时发生异常: projectId={}, moduleId={}, type={}", projectId, moduleId, type, e);
        return new Result(INTERNAL_ERROR, "查询失败: " + e.getMessage());
    }
}

    /**
     * 处理后端错误数据
     */
    private List<BackendResponsibilityVO> processBackendErrors(List<BackendError> errors,
                                                               List<Responsibility> responsibilities, Map<Long, Users> userMap) {

        Map<String, Responsibility> responsibilityMap = responsibilities.stream()
                .collect(Collectors.toMap(Responsibility::getErrorType, r -> r, (r1, r2) -> r1));

        return errors.stream().map(error -> {
            BackendResponsibilityVO vo = new BackendResponsibilityVO();
            BeanUtils.copyProperties(error, vo);
            vo.setId(error.getId());

            Responsibility responsibility = responsibilityMap.get(error.getErrorType());
            if (responsibility != null && responsibility.getResponsibleId() != null && responsibility.getDelegatorId() != null) {
                vo.setDelegatorId(responsibility.getDelegatorId());
                Users responsibleUser = userMap.get(responsibility.getResponsibleId());
                if (responsibleUser != null) {
                    vo.setName(responsibleUser.getUsername());
                    vo.setAvatarUrl(responsibleUser.getAvatar());
                }
            }
            return vo;
        }).collect(Collectors.toList());
    }

    /**
     * 处理前端错误数据
     */
    private List<FrontendResponsibilityVO> processFrontendErrors(List<FrontendError> errors,
                                                                 List<Responsibility> responsibilities, Map<Long, Users> userMap) {

        Map<String, Responsibility> responsibilityMap = responsibilities.stream()
                .collect(Collectors.toMap(Responsibility::getErrorType, r -> r, (r1, r2) -> r1));

        return errors.stream().map(error -> {
            FrontendResponsibilityVO vo = new FrontendResponsibilityVO();
            BeanUtils.copyProperties(error, vo);
            vo.setId(error.getId());

            Responsibility responsibility = responsibilityMap.get(error.getErrorType());
            if (responsibility != null && responsibility.getResponsibleId() != null && responsibility.getDelegatorId() != null) {
                vo.setDelegatorId(responsibility.getDelegatorId());
                Users responsibleUser = userMap.get(responsibility.getResponsibleId());
                if (responsibleUser != null) {
                    vo.setName(responsibleUser.getUsername());
                    vo.setAvatarUrl(responsibleUser.getAvatar());
                }
            }
            return vo;
        }).collect(Collectors.toList());
    }

    /**
     * 处理移动端错误数据
     */
    private List<MobileResponsibilityVO> processMobileErrors(List<MobileError> errors,
                                                             List<Responsibility> responsibilities, Map<Long, Users> userMap) {

        Map<String, Responsibility> responsibilityMap = responsibilities.stream()
                .collect(Collectors.toMap(Responsibility::getErrorType, r -> r, (r1, r2) -> r1));

        return errors.stream().map(error -> {
            MobileResponsibilityVO vo = new MobileResponsibilityVO();
            BeanUtils.copyProperties(error, vo);
            vo.setId(error.getId());

            Responsibility responsibility = responsibilityMap.get(error.getErrorType());
            if (responsibility != null && responsibility.getResponsibleId() != null && responsibility.getDelegatorId() != null) {
                vo.setDelegatorId(responsibility.getDelegatorId());
                Users responsibleUser = userMap.get(responsibility.getResponsibleId());
                if (responsibleUser != null) {
                    vo.setName(responsibleUser.getUsername());
                    vo.setAvatarUrl(responsibleUser.getAvatar());
                }
            }
            return vo;
        }).collect(Collectors.toList());
    }


    @Override
    public Result selectById(Long id) {
        LambdaQueryWrapper<BackendError> backendQueryWrapper = new LambdaQueryWrapper<>();
        LambdaQueryWrapper<FrontendError> frontendQueryWrapper = new LambdaQueryWrapper<>();
        LambdaQueryWrapper<MobileError> mobileQueryWrapper = new LambdaQueryWrapper<>();

        if (id == null) {
            return new Result(400, "参数错误");
        }

        backendQueryWrapper.eq(BackendError::getId, id);
        frontendQueryWrapper.eq(FrontendError::getId, id);
        mobileQueryWrapper.eq(MobileError::getId, id);

        BackendError backendError = backendErrorMapper.selectOne(backendQueryWrapper);
        FrontendError frontendError = frontendErrorMapper.selectOne(frontendQueryWrapper);
        MobileError mobileError = mobileErrorMapper.selectOne(mobileQueryWrapper);

        return new Result(200,
                List.of(backendError, frontendError, mobileError),
                "查询成功");
    }

    @Override
    public Result selectErrorDetail(Long errorId, String platform) {
        if (errorId == null || platform == null || platform.isEmpty()) {
            return new Result(BAD_REQUEST, "参数错误");
        }

        try {
            return switch (platform) {
                case "backend" -> {
                    BackendError backendError = backendErrorMapper.selectById(errorId);
                    yield new Result(SUCCESS, backendError, "查询成功");
                }
                case "frontend" -> {
                    FrontendError frontendError = frontendErrorMapper.selectById(errorId);

                    // 获取错误源码
                    String jsFilename = frontendError.getJsFilename();
                    if (jsFilename == null || jsFilename.isEmpty()) {
                        yield new Result(SUCCESS, frontendError, "查询成功");
                    }
                    // 获取行列号
                    Integer lineno = frontendError.getLineno();
                    Integer colno = frontendError.getColno();
                    if (lineno == null || colno == null) {
                        log.warn("前端错误缺少行列号信息: errorId={}", errorId);
                        yield new Result(SUCCESS, frontendError, "查询成功");
                    }
                    // 获取sourcemap文件路径
                    LambdaQueryWrapper<SourcemapFiles> sourcemapQueryWrapper = new LambdaQueryWrapper<>();
                    sourcemapQueryWrapper.eq(SourcemapFiles::getJsFilename, jsFilename);
                    SourcemapFiles sourcemapFiles = sourcemapFilesMapper.selectOne(sourcemapQueryWrapper);
                    String mapFilePath = sourcemapFiles.getFilePath();

                    SourceMapService service = new SourceMapService();
                    SourceMapService.OriginalSourcePosition position = service.resolveSourcePosition(
                            mapFilePath, lineno, colno);

                    FrontendErrorSourceCodeVO vo = new FrontendErrorSourceCodeVO();
                    BeanUtils.copyProperties(frontendError, vo);
                    // 获取上下文源码
                    vo.setSourceCode(position.getFormattedContextCode());

                    yield new Result(SUCCESS, vo, "查询成功");
                }
                case "mobile" -> {
                    MobileError mobileError = mobileErrorMapper.selectById(errorId);
                    yield new Result(SUCCESS, mobileError, "查询成功");
                }
                default -> new Result(BAD_REQUEST, "不支持的平台类型");
            };
        } catch (Exception e) {
            log.error("查询错误详情失败");
            return new Result(INTERNAL_ERROR, "查询错误详情失败");
        }
    }
}
