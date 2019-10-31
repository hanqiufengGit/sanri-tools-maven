package ${config.packageConfig.service};

import ${config.packageConfig.mapper}.*;
import ${config.packageConfig.entity}.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ContentManagerService {
    #foreach( $mapperClass in ${generatedInfo.mappers} )
    @Autowired
    private $mapperClass $StringUtils.uncapitalize($mapperClass);
    #end

    #foreach( $entityClass in ${generatedInfo.entitys} )
    #set ($mapper = ${generatedInfo.entityMapper.get($entityClass)})
    #set ($lowerMapper = $StringUtils.uncapitalize($mapper))

    public void insert$entityClass($entityClass $StringUtils.uncapitalize($entityClass)) {
        ${lowerMapper} .insert( $StringUtils.uncapitalize($entityClass) );
    }

    public void delete$entityClass(Object primaryKey) {
        ${lowerMapper} .deleteByPrimaryKey(primaryKey);
    }
    public void modify$entityClass($entityClass $StringUtils.uncapitalize($entityClass)) {
        ${lowerMapper} .updateByPrimaryKeySelective( $StringUtils.uncapitalize($entityClass) );
    }

    public $entityClass query$entityClass(Object primaryKey) {
        return ${lowerMapper} .selectByPrimaryKey(primaryKey);
    }
    #end

  #foreach( $entityClass in ${generatedInfo.entitys} )
            #set ($mapper = ${generatedInfo.entityMapper.get($entityClass)})
            #set ($lowerMapper = $StringUtils.uncapitalize($mapper))
    public List<$entityClass> queryPage$entityClass(Map<String, String> params) {
        Example example = new Example($entityClass .class);
        Example.Criteria criteria = example.createCriteria();
        if(params !=null && !params.isEmpty()){
            params.forEach((key,value) ->{
                criteria.andLike(key,value);
            });
        }
        return ${lowerMapper} .selectByExample(example);
    }
    #end
}
