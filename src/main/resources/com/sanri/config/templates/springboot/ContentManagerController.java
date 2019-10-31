package ${config.packageConfig.controller};

import ${config.packageConfig.service}.ContentManagerService;
import ${config.packageConfig.entity}.*;

import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.validation.annotation.Validated;

import com.sanri.web.dto.PageParam;
import com.sanri.web.dto.PageResponseDto;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/content")
public class ContentManagerController {
    @Autowired
    private ContentManagerService contentManagerService;

    #foreach( $entityClass in ${generatedInfo.entitys} )
            #set ($mapper = ${generatedInfo.entityMapper.get($entityClass)})
            #set ($lowerMapper = $StringUtils.uncapitalize($mapper))
    @PostMapping("/$StringUtils.uncapitalize($entityClass)/insert")
    public void insert$entityClass(@Validated $entityClass $StringUtils.uncapitalize($entityClass)) {
        contentManagerService .insert$entityClass( $StringUtils.uncapitalize($entityClass) );
    }
    @PostMapping("/$StringUtils.uncapitalize($entityClass)/json/insert")
    public void insert${entityClass}Json(@RequestBody @Validated $entityClass $StringUtils.uncapitalize($entityClass)) {
        contentManagerService .insert$entityClass( $StringUtils.uncapitalize($entityClass) );
    }
    @PostMapping("/$StringUtils.uncapitalize($entityClass)/delete")
    public void delete$entityClass(String primaryKey) {
        contentManagerService .delete$entityClass(primaryKey);
    }
    @PostMapping("/$StringUtils.uncapitalize($entityClass)/modify")
    public void modify$entityClass(@Validated $entityClass $StringUtils.uncapitalize($entityClass)) {
        contentManagerService .modify$entityClass( $StringUtils.uncapitalize($entityClass) );
    }
    @PostMapping("/$StringUtils.uncapitalize($entityClass)/json/modify")
    public void modify${entityClass}Json(@RequestBody @Validated $entityClass $StringUtils.uncapitalize($entityClass)) {
        contentManagerService .modify$entityClass( $StringUtils.uncapitalize($entityClass) );
    }
    #end

 #foreach( $entityClass in ${generatedInfo.entitys} )
            #set ($mapper = ${generatedInfo.entityMapper.get($entityClass)})
            #set ($lowerMapper = $StringUtils.uncapitalize($mapper))
    @GetMapping("/$StringUtils.uncapitalize($entityClass)/query")
    public $entityClass query$entityClass(String primaryKey) {
        return contentManagerService.query$entityClass(primaryKey);
    }
    #end

 #foreach( $entityClass in ${generatedInfo.entitys} )
            #set ($mapper = ${generatedInfo.entityMapper.get($entityClass)})
            #set ($lowerMapper = $StringUtils.uncapitalize($mapper))

    @GetMapping("/$StringUtils.uncapitalize($entityClass)/query/page")
    public PageResponseDto queryPage$entityClass( Map<String,String> params){
        String pageNo = params.getOrDefault("pageNo", "1");
        String pageSize = params.getOrDefault("pageSize", "10");
        params.remove("pageNo");params.remove("pageSize");

        PageHelper.startPage(NumberUtils.toInt(pageNo),NumberUtils.toInt(pageSize));
        Page page = (Page) contentManagerService.queryPage$entityClass(params);
        PageResponseDto pageResponseDto = new PageResponseDto();
        pageResponseDto.setRows(page.getResult());
        pageResponseDto.setTotal((int) page.getTotal());
        return pageResponseDto;
    }
    #end
}
