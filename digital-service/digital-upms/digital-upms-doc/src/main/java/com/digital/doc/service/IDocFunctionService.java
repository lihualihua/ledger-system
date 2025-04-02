package com.digital.doc.service;

import com.digital.model.doc.dto.DocFunctionDto;
import com.digital.model.doc.entity.DocFunctionEntity;

import java.util.List;

public interface IDocFunctionService {
    void saveFunction(DocFunctionDto functionDto);

    void deleteFunction(Long functionId);

    void bindingFunction(List<Long> functionIds);

    List<DocFunctionEntity> findAllFunction();

    List<DocFunctionEntity> findMyFunction();
}
