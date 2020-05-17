define(['util','dialog','template','jsonview'],function (util,dialog,template) {
    var redishelp = {};
    var apis = {
        connNames:'/file/manager/simpleConfigNames',
        createConn:'/file/manager/writeConfig',
        detail:'/file/manager/readConfig',
        serializes:'/zk/serializes',

        classloaders:'/classloader/classloaders',
        loadedClasses:'/classloader/loadedClasses',
        uploadClasses:'/classloader/uploadClasses',

        redisNodes:'/redis/redisNodes',
        scan:'/redis/scan',
        data:'/redis/data'
    };

    var modul = 'redis';

    redishelp.init = function () {
        bindEvent();
        loadClassloaders();
        loadSerializes();
        loadConns(function () {
            $('#connect>.dropdown-menu>li:first').click();
            $('#connect>.dropdown-menu').dropdown('toggle');
        });
    }

    function bindEvent() {
        var events = [{parent:'#connect>.dropdown-menu',selector:'li',types:['click'],handler:switchConn},
            {selector:'#newconnbtn',types:['click'],handler:newconn},
            {parent:'#data',selector:'input[name=searchkeys]',types:['keydown'],handler:keydownSearch},
            {parent:'#data',selector:'button[name=searchkeys]',types:['click'],handler:clickSearch},
            {parent:'#config',selector:'input[type=file]',types:['change'],handler:uploadClasses},
            {parent:'#config',selector:'select[name=classloaders]',types:['change'],handler:switchClassloader},
            {parent:'#data',selector:'table>tbody>tr',types:['click'],handler:loadData},
            {selector:'#rightbox>.close-box',types:['click'],handler:closeBox}];

        util.regPageEvents(events);

        function closeBox() {
            $(this).parent().hide();
        }
        /**
         * 加载当前 key 的数据
         */
        function loadData() {
            let $tr = $(this);
            let key = $tr.attr('key');
            let serializable = $('#config').find('select[name=serializable]').val();
            let loader = $('#config').find('select[name=classloaders]').val();

            util.requestData(apis.data,{connName:redishelp.conn,index:0,key:key,serializable:serializable,classloaderName:loader},function (data) {
                $('#rightbox').show();
                if(typeof data == 'object'){
                    // $('#rightbox>.content').text(JSON.stringify(data));
                    $('#rightbox>.content').JSONView(JSON.stringify(data));
                }else{
                    $('#rightbox>.content').text(data);
                }

            });
        }

        /**
         * 获取当前类加载器加载的类
         */
        function switchClassloader() {
            let loader = $(this).val();
            util.requestData(apis.loadedClasses,{name:loader},function (classes) {
                if(classes && classes.length >= 0){
                    let htmlCode = [];
                    for (let i = 0; i < classes.length; i++) {
                        htmlCode.push('<li class="list-group-item">'+classes[i]+'</li>');
                    }
                    $('#loadclasses').empty().append(htmlCode.join(''))
                }
            });
        }

        /**
         * 上传类加载器
         */
        function uploadClasses() {
            let files = this.files;
            if(files.length <=0)return ;

            let formData = new FormData();
            let fileName = files[0].name;
            formData.append('fileItem',files[0],fileName);
            formData.append('title',fileName+'-loader');

            util.postFile(apis.uploadClasses,formData,function () {
                loadClassloaders();
            });
        }

        function keydownSearch(event) {
            var event = event || window.event;
            if(event.keyCode == 13) {
                var $tabpane = $(this).closest('.tab-pane');
                search($tabpane, 0);
            }
        }

        function clickSearch() {
            var $tabpane = $(this).closest('.tab-pane');
            search($tabpane,0);
        }

        /**
         * 切换连接
         */
        function switchConn() {
            var conn = $(this).data('value');
            redishelp.conn = conn;

            $('#connect>button>span:eq(0)').text(conn);
            util.requestData(apis.detail,{modul:modul,baseName:conn},function (address) {
                $('#connect').next('input').val(address);
            });
            $('#connect>.dropdown-menu').dropdown('toggle');

            // 加载 redis 拓扑结构信息
            util.requestData(apis.redisNodes,{connName:conn},function (nodes) {
               let htmlCode = template('topologyTemplate',{nodes:nodes})
                $('#topology').find('table>tbody').html(htmlCode);
            });
        }

        /**
         * 新连接
         */
        function newconn() {
            dialog.create('新连接')
                .setContent($('#newconn'))
                .setWidthHeight('60%','300px')
                .addBtn({type:'yes',text:'确定',handler:function(index, layero){
                        var params = util.serialize2Json($('#newconn>form').serialize());
                        if(!params.name || !params.connectStrings){
                            layer.msg('请将信息填写完整');
                            return ;
                        }
                        params.modul = modul;
                        params.baseName = params.name;
                        params.content = JSON.stringify({connectStrings:params.connectStrings,auth:params.auth});
                        util.requestData(apis.createConn,params,function () {
                            layer.close(index);

                            loadConns(function (conns) {
                                if(conns){
                                    //请求最后一个连接,并选中
                                    $('#connect>.dropdown-menu>li[name='+params.name+']').click();
                                    $('#connect>.dropdown-menu').dropdown('toggle');
                                }
                            });
                        });
                    }})
                .build();
        }
    }

        /**
     * 加载所有序列化工具
     */
    function loadSerializes() {
        util.requestData(apis.serializes,function (serializes) {
            var $serializes =  $('#config').find('select[name=serializable]').empty();
            for(var i=0;i<serializes.length;i++){
                $serializes.append('<option value="'+serializes[i]+'">'+serializes[i]+'</option>')
            }
            //选中第一个
            $serializes.val(serializes[0]);
        });
    }

    /**
     * 加载所有的连接
     * @param callback
     */
    function loadConns(callback) {
        util.requestData(apis.connNames,{modul:modul},function (conns) {
            var $menu = $('#connect>ul.dropdown-menu').empty();
            if(conns){
                for(var i=0;i<conns.length;i++){
                    var $item = $('<li name="'+conns[i]+'"><a href="javascript:void(0);">'+conns[i]+'</a></li>').appendTo($menu);
                    $item.data('value',conns[i]);
                }
                if(callback){
                    callback(conns);
                }
            }
        });
    }

    /**
     * 加载所有的类加载器
     */
    function loadClassloaders(callback) {
        util.requestData(apis.classloaders,function (loaders) {
            if(loaders && loaders.length > 0){
                let htmlCode = [];
                for (let i = 0; i < loaders.length; i++) {
                    htmlCode.push('<option value="'+loaders[i]+'">'+loaders[i]+'</option>');
                }
                let $loader = $('#config').find('select[name=classloaders]');
                $loader.append(htmlCode.join(''));

                $loader.val(loaders[0]).change();
            }

        });
    }

    /**
     * 搜索和下一页合一； 初始搜索 cursor 写 0
     * @param $tabpane
     * @param cursor
     */
    function search($tabpane,cursor) {
        var pattern = $tabpane.find('input[name=searchkeys]').val().trim();
        util.requestData(apis.scan,{connName:redishelp.conn,index:0,pattern:pattern,cursor:cursor,limit:10},function (keys) {
            var htmlCode = template('keysTemplate',{redisKeyResults:keys});
            $tabpane.find('tbody').html(htmlCode);
        });
    }

    return redishelp.init();
});