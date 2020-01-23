define(['util','dialog','contextMenu','javabrush','xmlbrush','zclip'],function (util,dialog) {
    var tablehelp = {
        connName:undefined,
        schemaName:undefined
    };
    var apis = {
        conns:'/sqlclient/connections',
        schemas:'/sqlclient/schemas',
        search:'/sqlclient/searchTables',
        columns:'/sqlclient/refreshTable',
        codeConvertPreview:'/code/codeConvertPreview',
        templateNames:'/file/manager/simpleConfigNames',
        writeConfig:'/file/manager/writeConfig',
        readConfig:'/file/manager/readConfig',
        templateConvert:'/code/templateConvert',
        downloadPath:'/file/manager/downloadPath',
        multiTableSchemaConvert:'/code/multiTableSchemaConvert',
        tablesCode:'/mybatis/code/tablesCode',
        projectBuild:'/mybatis/code/projectBuild',
        refreshConnection:'/sqlclient/refreshConnection',
        refreshSchema:'/sqlclient/refreshSchema',
        plusConn:'/sqlclient/createConnection'
    };
    var modul = 'tableTemplate';
    var codeSchemaModul = 'codeSchema';

    tablehelp.init = function () {
      initConns();

      bindEvents();

      createRightMenu();
      return this;
    };

    /**
     *  加载所有连接
     */
    function initConns() {
        util.requestData(apis.conns,function (conns) {
            var htmlCode = [];
            for (var i=0;i<conns.length;i++){
                htmlCode.push('<option value="'+conns[i]+'">'+conns[i]+'</option>');
            }
            $('#conns').empty().append(htmlCode.join(''));

            if(conns.length != 0){
                // 启动的时候不加载当前连接,切换数据库来加载
                $('#conns').change();
            }
        });
    }

    /**
     * 创建右键菜单
     */
    function createRightMenu() {
        $.contextMenu({
            selector: '#tables li',
            zIndex: 4,
            items:{
                templateCode:{name:'模板代码...',icon:'copy',callback:templateCode},
                tablesCode:{name:'tkmybatis 模板生成',icon:'copy',callback:tablesCode},
                columns:{name:'属性列',icon:'cut',callback:tableColumns},
                steps:'------',
                tableInfo:{name:'表格属性',icon:'copy',callback:tableInfo}
            }
        });

        /**
         * 使用 tk.mybatis mbg 生成代码
         */
        function tablesCode(key,opts) {
            var tableName = currentTable(opts);
            var tableComments = opts.$trigger.attr('tableComments');
            dialog.create('tk.mybatis 代码['+tableName+']')
                .setContent($('#tkmybatisCodeGenDialog'))
                .setWidthHeight('90%', '90%')
                .addBtn({type:'yes',text:'单模块生成',handler:genAndDownCode})
                .onOpen(initDialog)
                .build();

            function initDialog() {
               util.icheck($('#tkmybatisCodeGenDialog'));
               $('#selectTableList').empty().append(
                   '<li class="list-group-item" tablename="'+tableName+'" title="'+tableComments+'">'+tableName+' <a class=" pull-right"><i class="fa fa-trash "></i> 删除</a></li>'
               );
            }

            function genAndDownCode(index) {
                var configs = util.serialize2Json($('#tkmybatisCodeGenDialog>form').serialize());
                var connName = $('#conns').val();
                var schemaName = $('#schemas').val();
                var tableNames = [];
                $('#selectTableList').find('li').each(function () {
                    tableNames.push($(this).attr('tablename'));
                });
                var data = {
                    baseMapper:configs.baseMapper,
                    connectionConfig:{
                        connName:connName,
                        schemaName:schemaName,
                        tableNames:tableNames
                    },
                    packageConfig:{
                        base:configs.base,
                        mapper:configs.mapperPackage,
                        entity:configs.entityPackage
                    },
                    entityConfig:{
                        baseEntity:configs.baseEntity,
                        interfaces:[configs.interfaces],
                        excludeColumns:[configs.excludeColumns],
                        supports:configs.supports.split(','),
                        idColumn:configs.idColumn,
                        sqlStatement:configs.sqlStatement
                    }
                }

                util.requestData(apis.tablesCode,{codeGeneratorConfig:data},function (filePath) {
                    util.downFile(apis.downloadPath,{modul:'mybatisCode',baseName:filePath},1000,function () {
                        layer.close(index);
                    });
                });
            }
        }

        /**
         * 查看表信息
         */
        function tableInfo(key,opts) {
            var connName = $('#conns').val();
            var schemaName = $('#schemas').val();
            var tableName = currentTable(opts);
            layer.alert('<ul class="">' +
                '<li class="">'+connName+'</li>' +
                '<li class="">'+schemaName+'</li>' +
                '<li class="">'+tableName+'</li>' +
                '</ul>');
        }

        /**
         * 查看当前表的所有列
         * @param key
         * @param opts
         */
        function tableColumns(key,opts) {
            var connName = $('#conns').val();
            var schemaName = $('#schemas').val();
            var tableName = currentTable(opts);
            util.requestData(apis.columns,{tableName:tableName,connName:connName,schemaName:schemaName},function (columns) {
                var columnNames = columns.map(column => {return column.columnName});
                layer.alert(columnNames.join(','));
            });
        }

        /**
         * 使用表来创建模板代码
         */
        function templateCode(key,opts) {
            var tableName = currentTable(opts);
            loadTemplates();
            //记录数据在对话框上
            $('#templatecodeconfig').data('tableName',tableName);

            dialog.create('模板代码['+tableName+']')
                .setContent($('#templatecodeconfig'))
                .setWidthHeight('90%', '90%')
                .addBtn({type:'yes',text:'生成代码',handler:writeCode})
                .addBtn({type:'button',text:'下载代码',handler:downloadCode})
                .build();

            /**
             * 生成本次模板代码
             */
            function writeCode() {
                var connName = $('#conns').val();
                var schemaName = $('#schemas').val();
                var ticket = $('#templatecodeconfig').data('ticket');
                var templateName = $('#templates').val();

                util.requestData(apis.templateConvert,{ticket:ticket,templateName:templateName,connName:connName,schemaName:schemaName,tableName:tableName},function (_ticket) {
                    layer.msg("代码生成成功,入场券是:"+_ticket);
                    $('#templatecodeconfig').data('ticket',_ticket);
                });
            }

            /**
             * 下载生成的所有模板代码
             * @param index
             */
            function downloadCode(index) {
                var ticket = $('#templatecodeconfig').data('ticket');
                if(!ticket){
                    layer.msg('请先用模板生成代码先');
                    return ;
                }
                //tableTemplateCodePath generate
                util.downFile(apis.downloadPath,{modul:'generate',baseName: 'tableTemplateCodePath/'+ticket},1000,function () {
                    layer.close(index);
                });
            }
        }

        function loadTemplates() {
            util.requestData(apis.templateNames,{modul:modul},function (templateNames) {
               var $template = $('#templates').empty();
               for (var i=0;i<templateNames.length;i++){
                   $template.append('<option value="'+templateNames[i]+'">'+templateNames[i]+'</option>');
               }
                $template.change();
            });
        }

        function currentTable(opts) {
            var tableName = opts.$trigger.attr('tableName');
            return tableName;
        }
    }

    /**
     * 从后台请求表格数据
     * @param keyword
     * @param callback
     */
    function searchRequest(keyword, callback) {
        var index = layer.load(1, {
            shade: [0.1,'#fff']
        });
        util.requestData(apis.search,{connName:tablehelp.connName,schemaName:tablehelp.schemaName,keyword:keyword},function (tables) {
            callback(tables);
            layer.close(index);
        },function () {
            layer.close(index);
        });
    }

    function renderTables(tables) {
        var htmlCode = [];
        for (var i = 0; i < tables.length; i++) {
            if (!tables[i].tableName) continue;
            htmlCode.push('<li class="list-group-item" tableName = "' + tables[i].tableName + '" tableComments="' + tables[i].comments + '"> <i class="fa fa-table"></i> ' + tables[i].tableName + '(' + (tables[i].comments || '未说明') + ')</li>')
        }
        return htmlCode;
    }

    /**
     * 搜索相匹配的结果,有可能数据表未初始化,需要做加载进度条
     * @param keyword
     */
    function search(keyword) {
        $('#columns>tbody').empty();
        searchRequest(keyword,function (tables) {
            var htmlCode = renderTables(tables);
            $('#tables').empty().html(htmlCode.join(''));

            $('#tables>li:first').addClass('active').click();
        });
    }

    function bindEvents() {
        var mybatisCodeHandler = {};            // mybatis 代码生成事件
        var refreshEvents = {};                 // 数据刷新事件
        var newConnEvents = {};                 // 新连接相关事件
        var quickCreateTableEvents = {};        // 快速建表相关事件

        /** tk.mybatis 自动生成代码事件 */
        mybatisCodeHandler.addTable = function () {
            /**
             * 选择表确认后操作
             */
            function confirmTables(index) {
                var selectedTables = [];
                $('#multitableschemadialog').find('tbody').find(':checkbox').each(function () {
                    var $tr = $(this).closest('tr');
                    if($(this).is(':checked')){
                        selectedTables.push($tr.attr('tablename'));
                    }
                });

                var existTables = [] ;
                $('#selectTableList>li').each(function () {
                   existTables.push($(this).attr('tablename'));
                });
                //合并选择的表信息
                selectedTables.concat(existTables);
                util.CollectionUtils.uniqueSimpleArray(selectedTables);

                $('#selectTableList').empty();
                for (var i=0;i<selectedTables.length;i++){
                    var tableName = selectedTables[i];
                    $('#selectTableList').append(
                        '<li class="list-group-item" tablename="'+tableName+'" title="'+tableName+'">'+tableName+' <a class=" pull-right"><i class="fa fa-trash "></i> 删除</a></li>'
                    );
                }

                layer.close(index);
            }

            //记录所有选中表的信息(初次记录,加载外部搜索条件,所有搜索到的表选中)
            var keyword = $('#search').val().trim();
            $('#multisearch').val(keyword);
            searchRequest(keyword,function (tables) {
                var tableNames = [];
                for (var i=0;i<tables.length;i++){
                    tableNames.push(tables[i].tableName);
                }
                $('#multitableschemadialog').data('selectedTables',tableNames);
                $('#multitableschemadialog').data('allTables',tableNames);

                dialog.create('选择用于代码生成的表')
                    .setContent($('#multitableschemadialog'))
                    .setWidthHeight('90%', '90%')
                    .addBtn({type:'yes',text:'确定',handler:confirmTables})
                    .build();

                //触发表搜索
                $('#multisearchBtn').click();
            });
        }
        mybatisCodeHandler.deleteTable = function () {
            $(this).closest('li').remove();
        }
        mybatisCodeHandler.autoFillPackage = function () {
            var base = $(this).val();
            $('#tkmybatisCodeGenDialog').find('input[name=entityPackage]').val(base+'.dao.entity');
            $('#tkmybatisCodeGenDialog').find('input[name=mapperPackage]').val(base+'.dao.mapper');
            $('#tkmybatisCodeGenDialog').find('input[name=entityPackage]').val(base+'.dao.entity');
            $('#tkmybatisCodeGenDialog').find('input[name=controllerPackage]').val(base+'.web.controller');
            $('#tkmybatisCodeGenDialog').find('input[name=servicePackage]').val(base+'.service');
            $('#tkmybatisCodeGenDialog').find('input[name=voPackage]').val(base+'.web.dto.vo');
            $('#tkmybatisCodeGenDialog').find('input[name=dtoPackage]').val(base+'.web.dto');
            $('#tkmybatisCodeGenDialog').find('input[name=paramPackage]').val(base+'.web.param');

            //填写 groupId 和 artifactId 还有项目名字
            $('#tkmybatisCodeGenDialog').find('input[name=groupId]').val(base);
            var projectName = base;
            var split = base.split('.');
            if(split.length > 0){
                projectName = split[split.length - 1];
            }
            $('#tkmybatisCodeGenDialog').find('input[name=groupId]').val(base);
            $('#tkmybatisCodeGenDialog').find('input[name=artifactId]').val(projectName);
            $('#tkmybatisCodeGenDialog').find('input[name=projectName]').val(projectName);

        }

        /** 刷新连接和刷新表格事件*/
        refreshEvents.refreshConnection = function () {
            var connName = $('#conns').val();
            util.requestData(apis.refreshConnection,{connName:connName},function () {
               // 重新加载 schema
                $('#conns').change();
            });
        }
        refreshEvents.refreshSchema = function () {
            var connName = $('#conns').val();
            var schemaName = $('#schemas').val();
            util.requestData(apis.refreshSchema,{connName:connName,schemaName:schemaName},function () {
                // 重新加载数据表
                $('#schemas').change();
            });
        }

        /** 新连接事件 */
        newConnEvents.plusConn = function () {
            var defaultValues = {mysql:{port:3306,database:'mysql',username:'root',name:'mysqlLocal'},
                postgresql:{port:5432,database: 'postgres',username:'postgres',name:'postgresqlLocal'},
                oracle:{port:1521,database:'orcl',username:'sanri',name:'oracleLocal'}};

            var build=dialog.create('新连接')
                .setContent($('#plusConnDialog'))
                .setWidthHeight('500px','500px')
                .addBtn({type:'yes',text:'确定',handler:function(index, layero){
                        var data = util.serialize2Json($('#plusConnDialog>form').serialize());
                        util.requestData(apis.plusConn,{connectionInfo:data},function(ret){
                            layer.close(index);
                            if(ret == ""){
                                layer.msg('新建连接失败,已存在连接名称');
                                return ;
                            }
                            //如果加成功了，将连接保存在 localStorage 中，方便下次添加
                            var preConns = localStorage.getItem('conns') || JSON.stringify(defaultValues);
                            var preConnsJson = JSON.parse(preConns);
                            preConnsJson[data.dbType] = data;
                            localStorage.setItem('conns',JSON.stringify(preConnsJson));
                        });
                    }})
                .build();

            //初始化单选框
            require(['icheck'],function(){
                $(':radio','#plusConnDialog').iCheck({
                    checkboxClass: 'icheckbox_square-green',
                    radioClass: 'iradio_square-green'
                });
                $(':radio','#plusConnDialog').on('ifChecked',switchDbType);
            });

            /**
             * 新建连接时,不同的数据库类型有不同的初始化参数
             */
            function switchDbType(event) {
                var value = $(this).val();

                // 从 localStorage 中加载以前保存的数据
                var preConns = localStorage.getItem('conns') || JSON.stringify(defaultValues);
                var preConnsJson = JSON.parse(preConns);

                $('#plusConnDialog').find('input').each(function () {
                    var elname = $(this).attr('name');
                    if(preConnsJson[value][elname]){
                        $(this).val(preConnsJson[value][elname]);
                    }
                });
            }
        }

        /** 快速建表相关事件 */
        quickCreateTableEvents.data = {};
        quickCreateTableEvents.search = function(keyword){
            searchRequest(keyword,function (tables) {
                var htmlCode = renderTables(tables);
                $('#contextTables').empty().html(htmlCode.join(''));

                $('#contextTables>li:first').addClass('active').click();
            });
        }
        quickCreateTableEvents.keyupSearch = function () {
            var keyword = $(this).val().trim();
            if(keyword.length < 10)return ;
            if(keyword.endsWith(':'))return ;

           quickCreateTableEvents.search(keyword);
        }
        quickCreateTableEvents.clickSearch = function (event) {
            var event = event || window.event;
            if(event.keyCode != 13)return ;

            var keyword = $(this).val().trim();
            quickCreateTableEvents.search(keyword);
        }
        quickCreateTableEvents.checkOriginTableColumnSelect = function(rightTableName){
            // 对比两边的列，将已经勾选的列选中
            var rightTable = quickCreateTableEvents.data[rightTableName];
            var leftColumns = [],rightColumns = rightTable? rightTable.columns : [];
            $('#contextColumns>tbody').find('tr').each(function () {
                leftColumns.push($(this).attr('columnname'));
                // 先要取消全部选中
                if($(this).find('input').iCheck) {
                    $(this).find('input').iCheck('uncheck');
                }
            });
            for(var i=0;i<rightColumns.length;i++){
                let lowercase = rightColumns[i].columnName.toLowerCase();
                let uppercase = rightColumns[i].columnName.toUpperCase();
                if($.inArray(uppercase,leftColumns) != -1 || $.inArray(lowercase,leftColumns) != -1){
                    var columnName = rightColumns[i].columnName;
                    $('#contextColumns>tbody').find('tr[columnname='+columnName+']').iCheck('check');
                }
            }
        }
        quickCreateTableEvents.choseTable = function (event) {
            var tableName = $(this).attr('tableName');
            $('#contextTables>li').removeClass('active');$(this).addClass('active');
            util.requestData(apis.columns,{connName:tablehelp.connName,schemaName:tablehelp.schemaName,tableName:tableName},function (columns) {
                var htmlCode = [];
                for(var i=0;i<columns.length;i++){
                    htmlCode.push('<tr columnname="'+columns[i].columnName+'">');
                    htmlCode.push('<td><input type="checkbox" /></td>');
                    htmlCode.push('<td>'+columns[i].columnName+'</td>');
                    htmlCode.push('<td>'+columns[i].columnType.dataType+'</td>');
                    htmlCode.push('<td>'+columns[i].comments+'</td>');
                    htmlCode.push('</tr>');
                }
                $('#contextColumns>tbody').empty().append(htmlCode.join(''));

                // 渲染所有的复选框组件
                util.icheck('#contextColumns');

                // 选中右边表格已经有的列
                var rightTableName = $('#newtables>li.list-group-item.active').attr('tablename');
                quickCreateTableEvents.checkOriginTableColumnSelect(rightTableName);
            });
        }
        quickCreateTableEvents.plusNewTable = function (event) {
            dialog.create('创建数据表')
                .setContent($('#plusTableDialog'))
                .setWidthHeight('400px', '300px')
                .addBtn({type:'yes',text:'创建',handler:newTable})
                .build();

            function newTable(index) {
                var data = util.serialize2Json($('#plusTableDialog').find('form').serialize());
                if(data.tableName in quickCreateTableEvents.data){layer.msg('已经存在表');return;}

                $('#newtables').append('<li class="list-group-item" tableName="'+data.tableName+'">'+data.tableName+'<i class="fa fa-trash text-gold"></i></li>');
                quickCreateTableEvents.data[data.tableName] = $.extend({columns:[{columnName:data.key}]},data);

                $('#newtables>li:last').click();

                layer.close(index);
            }
        }
        quickCreateTableEvents.loadNewTableConfig = function(event){
            $(this).addClass('active');
            $(this).siblings().removeClass('active');

            var tableName = $(this).attr('tableName');
            $('#newTableColumns').closest('.panel').find('.panel-heading').text(tableName);
            var columns = quickCreateTableEvents.data[tableName].columns;
            $('#newTableColumns>tbody').empty();

            var htmlCode = [];
            for (var i=0;i<columns.length;i++){
                htmlCode.push('<tr contenteditable="true" columnName="'+columns[i].columnName+'"><td contenteditable="false">'+(i+1)+'</td><td>'+columns[i].columnName+'</td><td>'+(columns[i].typeName || '')+'</td><td>'+(columns[i].comment || '')+'</td><td contenteditable="false"><a href="javascript:void(0);">删除</a></td></tr>');
            }
            $('#newTableColumns>tbody').append(htmlCode.join(''));

            // 右边加载完列后，需要判断是否左边的表格有选中的相似列
            quickCreateTableEvents.checkOriginTableColumnSelect(tableName);
        }
        quickCreateTableEvents.refreshTables = function () {
            $('#newtables').empty();

        }
        editNewTableColumn.editNewTableColumn = function () {
            var newValue = $(this).val().trim();
            var tableName = $('#newtables').find('li.list-group-item.active').attr('tablename');
            // 找到编辑了的表格进行列更新 TODO
            // 执行更新
            quickCreateTableEvents.checkOriginTableColumnSelect(tableName);
        }

        var events = [{selector:'#conns',types:['change'],handler:switchConn},
            {selector:'#schemas',types:['change'],handler:switchSchema},
            {selector:'#conns+.input-group-btn.refresh>button',types:['click'],handler:refreshEvents.refreshConnection},
            {selector:'#conns+.refresh+.plusconn>button',types:['click'],handler:newConnEvents.plusConn},
            {selector:'#schemas+.input-group-btn>button',types:['click'],handler:refreshEvents.refreshSchema},
            {selector:'#search',types:['keyup'],handler:keyupSearch},
            {selector:'#btnsearch',types:['click'],handler:clickSearch},
            {selector:'#multisearch',types:['keyup'],handler:multiKeyupSearch},
            {selector:'#multisearchBtn',types:['click'],handler:multiClickSearch},
            {selector:'#seevars',types:['click'],handler:seeVars},
            {selector:'#plustemplate',types:['click'],handler:plusTemplate},
            {parent:'#tables',selector:'li',types:['click'],handler:columnsView},
            {selector:'#templates',types:['change'],handler:switchTemplate},
            {selector:'#codeschema',types:['click'],handler:codeSchemaDialog},
            {selector:'#copyCode',types:['click'],handler:copyCurrentCode},
            {parent:'#codeSchemaDialog>ul.list-group',selector:'li',types:['click'],handler:makeCodeFromSchema},
            {selector:'#multiTableSchemaCode',types:['click'],handler:multiTableSchemaCode},
            {parent:'#multitableschemadialog ul.list-group',selector:'li',types:['click'],handler:switchCodeSchema},
            {selector:'#quickCreateTable',types:['click'],handler:quickCreateTable},
            {selector:'#search',types:['keydown'],handler:function (event) {
                    var event = event || window.event;
                    if(event.keyCode == 13){
                        clickSearch();
                    }
                }
            },{selector:'#multisearch',types:['keydown'],handler:function (event) {
                    var event = event || window.event;
                    if(event.keyCode == 13){
                        multiClickSearch();
                    }
            }},

            // 代码生成的相关事件
            {selector:'#mybatiscodeaddtable',types:['click'],handler:mybatisCodeHandler.addTable},
            {parent:'#selectTableList',selector:'>li>a',types:['click'],handler:mybatisCodeHandler.deleteTable},
            {selector:'#projectBuild',types:['click'],handler:projectBuild},
            {parent:'#tkmybatisCodeGenDialog',selector:'input[name=base]',types:['keyup'],handler:mybatisCodeHandler.autoFillPackage},
            {parent:'#quickCreateTableDialog',selector:'input[name=keyword]',types:['keyup'],handler:quickCreateTableEvents.keyupSearch},
            {parent:'#quickCreateTableDialog',selector:'input[name=keyword]',types:['keydown'],handler:quickCreateTableEvents.clickSearch},
            {parent:'#contextTables',selector:'li',types:['click'],handler:quickCreateTableEvents.choseTable},
            {parent:'#quickCreateTableDialog',selector:'button[name=newTable]',types:['click'],handler:quickCreateTableEvents.plusNewTable},
            {parent:'#newtables',selector:'li',types:['click'],handler:quickCreateTableEvents.loadNewTableConfig},
            {parent:'#newTableColumns',selector:'td',types:['blur'],handler:quickCreateTableEvents.editNewTableColumn}];

        /**
         * 项目构建
         */
        function projectBuild() {
            dialog.create('项目生成')
                .setContent($('#tkmybatisCodeGenDialog'))
                .setWidthHeight('90%', '90%')
                .addBtn({type:'yes',text:'整项目生成',handler:projectBuildSendData})
                .onOpen(initDialog)
                .build();

            function initDialog() {
                util.icheck($('#tkmybatisCodeGenDialog'));

                //获取到搜索的所有表
                var keyword = $('#search').val().trim();
                searchRequest(keyword,function (tables) {
                    var tableNames = [];$('#selectTableList').empty();
                    for (var i = 0; i < tables.length; i++) {
                        $('#selectTableList') .append(
                            '<li class="list-group-item" tablename="'+tables[i].tableName+'" title="'+tables[i].tableComments+'">'+tables[i].tableName+' <a class=" pull-right"><i class="fa fa-trash "></i> 删除</a></li>'
                        );
                    }

                });
            }

            function projectBuildSendData(index) {
                var configs = util.serialize2Json($('#tkmybatisCodeGenDialog>form').serialize());
                var connName = $('#conns').val();
                var schemaName = $('#schemas').val();
                var tableNames = [];
                $('#selectTableList').find('li').each(function () {
                    tableNames.push($(this).attr('tablename'));
                });
                var data = {
                    baseMapper:configs.baseMapper,
                    projectName:configs.projectName,
                    connectionConfig:{
                        connName:connName,
                        schemaName:schemaName,
                        tableNames:tableNames
                    },
                    packageConfig:{
                        base:configs.base,
                        mapper:configs.mapperPackage,
                        entity:configs.entityPackage,
                        service:configs.servicePackage,
                        controller:configs.controllerPackage,
                        vo:configs.dtoPackage,
                        dto:configs.dtoPackage,
                        param:configs.paramPackage
                    },
                    entityConfig:{
                        baseEntity:configs.baseEntity,
                        interfaces:[configs.interfaces],
                        excludeColumns:[configs.excludeColumns],
                        supports:configs.supports.split(','),
                        idColumn:configs.idColumn,
                        sqlStatement:configs.sqlStatement
                    },
                    mavenConfig:{
                        groupId:configs.groupId,
                        artifactId:configs.artifactId,
                        version:configs.version,
                        springBootVersion:configs.springBootVersion
                    }
                }

                var mask = layer.load(1, {
                    shade: [0.1,'#fff']
                });
                util.requestData(apis.projectBuild,{codeGeneratorConfig:data},function (filePath) {
                    layer.close(mask);
                    util.downFile(apis.downloadPath,{modul:'projectCode',baseName:filePath},1000,function () {
                        layer.close(index);
                    });
                },function () {
                    layer.close(mask);
                });
            }
        }

        function trimValue($el,enableEmptyLine){
            var value = $el.val().trim();
            var array = value.split('\n');
            var newLines = [];
            for (var i = 0; i < array.length; i++) {
                var currentLine = array[i].trim();
                if(!enableEmptyLine && !currentLine){continue;}
                newLines.push(currentLine);
            }
            return newLines.join('\n');
        }

        /**
         * 打开快速建表对话框
         */
        function quickCreateTable() {
            var build = dialog.create('快速建表')
                .setContent($('#quickCreateTableDialog'))
                .setWidthHeight('100%','90%')
                .addBtn({type:'button',text:'执行',handler:executeCreateTable})
                .build();

            // 加载主界面搜索的表
            quickCreateTableEvents.search($('#search').val().trim());

            function executeCreateTable(index) {
               util.requestData('/sqlclient/executor',{
                   connName:tablehelp.connName,
                   schemaName:tablehelp.schemaName,
                   ddl:$('#sqlview').text()
               },function () {
                   layer.msg('建表成功');
                   layer.close(index);
               })
            }


        }

        /**
         * 用于复制当前代码
         */
        function copyCurrentCode(){
            var formatCode = $('#templatecodeconfig>input:hidden[name=currentcode]').val();
            $('#dataDialog>textarea').val(formatCode);
            dialog.create('代码查看')
                .setContent($('#dataDialog'))
                .setWidthHeight('90%','90%')
                .build();
        }

        //绑定全选/不选的复选事件
        $('#multitableschemadialog').find('thead').on('ifChanged','input:checkbox',function () {
            var $items =  $('#multitableschemadialog').find('tbody').find('input:checkbox');
            if($(this).is(':checked')){
                $items.iCheck('check');
            }else{
                $items.iCheck('uncheck');
            }
        });

        function multiSearch(keyword) {
            var $tbody = $('#multitableschemadialog').find('tbody').empty();
            var selectedTables = $('#multitableschemadialog').data('selectedTables');
            searchRequest(keyword,function (tables) {
                //标记表是否被选中
                for(var i=0;i<tables.length;i++){
                    if($.inArray(tables[i].tableName,selectedTables) != -1){
                        tables[i].selected = true;
                    }
                }
               require(['template','icheck'],function (template) {
                   var htmlCode = template('generatetablestemplate',{tables:tables});
                   $tbody.html(htmlCode);
                   $tbody.closest('table').find('input:checkbox').iCheck({
                       checkboxClass: 'icheckbox_square-green'
                   });
               })
            });
        }

        function multiKeyupSearch() {
            var keyword = $(this).val().trim();
            if(keyword.length > 10){
                if(keyword.endsWith(':'))return ;
                multiSearch(keyword);
            }
        }

        function multiClickSearch() {
            var keyword = $('#multisearch').val().trim();
            if(keyword.endsWith(':') )return ;
            multiSearch(keyword);
        }

        function switchCodeSchema() {
            $(this).siblings().removeClass('active');
            $(this).addClass('active');
            $(this).closest('ul').data('value',$(this).attr('value'));
        }

        /**
         * 多表方案代码生成，数据加载
         */
        function multiTableSchemaCode() {
            //加载所有模板信息和表信息
            var $codeSchemaUl = $('#multitableschemadialog').find('ul.list-group').empty();
            util.requestData(apis.templateNames,{modul:codeSchemaModul},function (codeSchemas) {
                for(var i=0;i<codeSchemas.length;i++){
                    var clazz = (i==0 ? 'list-group-item active':'list-group-item');
                    $codeSchemaUl.append('<li class="'+clazz+'" value="'+codeSchemas[i]+'">'+codeSchemas[i]+'</li>')
                }
            });

            //记录所有选中表的信息(初次记录,加载外部搜索条件,所有搜索到的表选中)
            var keyword = $('#search').val().trim();
            $('#multisearch').val(keyword);
            searchRequest(keyword,function (tables) {
                var tableNames = [];
                for (var i=0;i<tables.length;i++){
                    tableNames.push(tables[i].tableName);
                }
                $('#multitableschemadialog').data('selectedTables',tableNames);
                $('#multitableschemadialog').data('allTables',tableNames);

                dialog.create('多表使用方案生成代码')
                    .setContent($('#multitableschemadialog'))
                    .setWidthHeight('90%', '90%')
                    .addBtn({type:'yes',text:'确定',handler:requestCodeTicket})
                    .build();

                //触发表搜索
                $('#multisearchBtn').click();
            });

            function requestCodeTicket(index) {
                var selectedTables = [];
                $('#multitableschemadialog').find('tbody').find(':checkbox').each(function () {
                   var $tr = $(this).closest('tr');
                   if($(this).is(':checked')){
                       selectedTables.push($tr.attr('tablename'));
                   }
                });
                var codeSchemaName = $codeSchemaUl.find('li.list-group-item.active').attr('value');
                util.requestData(apis.multiTableSchemaConvert,{connName:tablehelp.connName,schemaName:tablehelp.schemaName,tableNames:selectedTables,codeSchemaName:codeSchemaName},function (ticket) {
                    util.downFile(apis.downloadPath,{modul:'generate',baseName: 'tableTemplateCodePath/'+ticket},1000,function () {
                        layer.close(index);
                    });
                });

            }
        }

        /**
         * 使用方案生成代码
         */
        function makeCodeFromSchema() {
            var connName = $('#codeSchemaDialog').data('connName');
            var schemaName= $('#codeSchemaDialog').data('schemaName');
            var tableName = $('#codeSchemaDialog').data('tableName');
            var codeSchemas = $(this).data('codeSchemas');

            //打开一相进度框
            $('#codeSchemaProcessDialog').find('ul.list-group').empty();
            dialog.create('代码生成进度')
                .setContent($('#codeSchemaProcessDialog'))
                .setWidthHeight('400px', '400px')
                .build();


            var ticket = '';
            var $process = $('#codeSchemaProcessDialog',$(top.document)).find('ul.list-group');
            for(var i=0;i<codeSchemas.length;i++){
                util.requestData(apis.templateConvert,{ticket:ticket,connName:connName,schemaName:schemaName,tableName:tableName,templateName:codeSchemas[i],sync:true},function (_ticket) {
                    $process.append('<li class="list-group-item list-group-item-success">'+tableName+' -> '+codeSchemas[i]+' completed </li>');
                    ticket = _ticket;
                });
            }

            //开始下载
            util.downFile(apis.downloadPath,{modul:'generate',baseName: 'tableTemplateCodePath/'+ticket},1000,function () {

            });
        }

        /**
         * 打开方案对话框
         */
        function codeSchemaDialog() {
            //加载代码方案
            util.requestData(apis.templateNames,{modul:codeSchemaModul},function (codeSchemas) {
               var $listGroup = $('#codeSchemaDialog>ul.list-group').empty();
               for(var i=0;i<codeSchemas.length;i++){
                   var $li = $('<li class="list-group-item">'+codeSchemas[i]+'</li>');
                   $li.data('codeSchemas',codeSchemas[i].split('+'));
                   $listGroup.append($li);
               }
            });

            //记录数据
            var tableName = $('#templatecodeconfig').data('tableName');
            var connName = $('#conns').val();
            var schemaName = $('#schemas').val();
            $('#codeSchemaDialog').data('connName',connName);
            $('#codeSchemaDialog').data('schemaName',schemaName);
            $('#codeSchemaDialog').data('tableName',tableName);
            dialog.create('使用方案生成')
                .setContent($('#codeSchemaDialog'))
                .setWidthHeight('700px', '90%')
                .build();
        }
        /**
         * 模板切换时切换预览
         */
        function switchTemplate() {
            var template = $(this).val();
            var tableName = $('#templatecodeconfig').data('tableName');
            var connName = $('#conns').val();
            var schemaName = $('#schemas').val();

            //获取模板代码
            util.requestData(apis.readConfig,{modul:modul,baseName:template},function (templateConfig) {
                //简单判断下文件类型
                var fileType = 'java';
                if(templateConfig.startsWith('<?xml')){
                    fileType = 'xml';
                }
                $('#templatePreview').empty();
                $('#templatePreview').append('<pre class="brush:\''+fileType+'\';"></pre>');
                $('#templatePreview>pre').text(templateConfig);

                //打开代码预览
                util.requestData(apis.codeConvertPreview,{templateName:template,connName:connName,schemaName:schemaName,tableName:tableName},function (formatCode) {
                    $('#codepreview').empty();
                    $('#codepreview').append('<pre class="brush:\''+fileType+'\';"></pre>');
                    $('#codepreview>pre').text(formatCode);
                    //记录当时生成的代码
                    $('#templatecodeconfig>input:hidden[name=currentcode]').val(formatCode);
                    SyntaxHighlighter.highlight();
                });
            })
        }

        /**
         * 添加模板
         */
        function plusTemplate() {
            dialog.create('添加模板')
                .setContent($('#plustemplatedialog'))
                .setWidthHeight('700px', '90%')
                .addBtn({type:'yes',text:'确定',handler:saveTemplate})
                .build();

            /**
             * 保存模板
             */
            function saveTemplate(index) {
                var data = util.serialize2Json($('#plustemplatedialog form').serialize());
                data.modul = modul;
                util.requestData(apis.writeConfig,data,function () {
                    layer.close(index);
                })
            }
        }

        /**
         * 查看可用变量
         */
        function seeVars() {
            dialog.create('可用变量名列表')
                .setContent($('#varslist'))
                .setWidthHeight('500px', '90%')
                .build();
        }

        function columnsView() {
            var tableName = $(this).attr('tableName');
            $('#tables>li').removeClass('active');$(this).addClass('active');
            util.requestData(apis.columns,{connName:tablehelp.connName,schemaName:tablehelp.schemaName,tableName:tableName},function (columns) {
               var htmlCode = [];
               for(var i=0;i<columns.length;i++){
                   htmlCode.push('<tr>');
                   htmlCode.push('<td>'+(i+1)+'</td>');
                   htmlCode.push('<td>'+columns[i].columnName+'</td>');
                   htmlCode.push('<td>'+columns[i].columnType.dataType+'</td>');
                   htmlCode.push('<td>'+columns[i].comments+'</td>');
                   htmlCode.push('</tr>');
               }
               $('#columns>tbody').empty().append(htmlCode.join(''));
            });
        }

        function clickSearch() {
            var keyword = $('#search').val().trim();
            if(keyword.endsWith(':') )return ;
            search(keyword);
        }
        function keyupSearch() {
            var keyword = $(this).val().trim();
            if(keyword.length > 10){
                if(keyword.endsWith(':'))return ;
                search(keyword);
            }
        }

        /**
         * 切换数据源
         */
        function switchConn(){
            tablehelp.connName  = $(this).val();

            loadSchemas(tablehelp.connName );
        }

        /**
         * 切换数据库
         */
        function switchSchema(){
            tablehelp.schemaName = $(this).val();

            //重新发起搜索
            $('#btnsearch').click();
        }

        util.regPageEvents(events);
    }


    /**
     * 切换数据源时加载所有的数据库
     * @param connName
     */
    function loadSchemas(connName) {
        util.requestData(apis.schemas,{connName:connName},function (schemas) {
            var htmlCode = [];
            for (var i=0;i<schemas.length;i++){
                htmlCode.push('<option value="'+schemas[i].schemaName+'">'+schemas[i].schemaName+'</option>');
            }
            $('#schemas').empty().append(htmlCode.join(''));

            $('#schemas').change();
        });

    }

    return tablehelp.init();
});