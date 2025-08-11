package com.qg.service;

import com.qg.domain.Project;
import com.qg.domain.Result;
import com.qg.dto.InviteDto;
import com.qg.vo.PersonalProjectVO;

public interface ProjectService {
     Result addProject(PersonalProjectVO personalProjectVO);

     Result updateProject(Project project);

     Result deleteProject(String uuid);

     Result getProject(String uuid);

     Result getProjectList();

     Result getPersonalPublicProject(Long userId);

     Result getPersonalUnpublicProject(Long userId);

    Result getInviteDCode(String projectId);

     Result joinProject(InviteDto inviteDto);

    Result selectProjectByName(String name);
}
