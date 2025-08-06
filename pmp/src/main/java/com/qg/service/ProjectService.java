package com.qg.service;

import com.qg.domain.Project;
import com.qg.domain.Result;

public interface ProjectService {
     Result addProject(Project project);

     Result updateProject(Project project);

     Result deleteProject(String uuid);

     Result getProject(String uuid);

     Result getProjectList();

     Result getPersonalPublicProject(Long userId);

     Result getPersonalUnpublicProject(Long userId);
}
