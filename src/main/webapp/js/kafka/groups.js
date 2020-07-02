define(['util','dialog'],function (util,dialog) {
    var groupsPage = {};
    var modul = 'kafka';

    var apis = {
        groups:'/kafka/groups',
        delGroup:'/kafka/deleteGroup',
        detail:'/kafka/readConfig',
        connNames:'/file/manager/simpleConfigNames',
        setThirdpartTool:'/kafka/setThirdpartTool',
        createConn:'/kafka/writeConfig',
        groupSubscribeTopics:'/kafka/groupSubscribeTopics',
        zkConns:'/file/manager/simpleConfigNames',
        brokers:'/kafka/brokers',
        writeJaasFile:'/kafka/writeJaasFile',
        readConfig:'/file/manager/readConfig',
        monitor:'/kafka/brokerMonitor'
    }

    groups.init = function () {
        bindEvents();
        loadConns(function (conns) {
            $('#connect>.dropdown-menu>li:first').click();
            $('#connect>.dropdown-menu').dropdown('toggle');
        });
        return this;
    }

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

    function loadGroups() {
        var index = layer.load(1, {
          shade: [0.1,'#fff']
        });
        util.requestData(apis.groups,{clusterName:groupsPage.conn},function (groups) {
            var $groups = $('#groups>.list-group').empty();
            for (var i=0;i<groups.length;i++){
                var group = groups[i];
                util.ajax({url:apis.groupSubscribeTopics,data:{group:group,clusterName:groupsPage.conn},async:false},function(topics){
                    $('<a class="list-group-item group" group='+group+'> <span>'+group+'</span> <b class="pull-right text-danger margin-left">删除</b> <span class="badge list-group-item-success"> '+topics.length+' </span> </a>').appendTo($groups);
                });
            }
            layer.close(index);
        },function () {
            layer.close(index);
        });
    }

    function bindEvents(){
        var events = [{selector:'#newconnbtn',types:['click'],handler:newconn},
            {selector:'#thirdpart',types:['click'],handler:thirdpart},
            {parent:'#connect>.dropdown-menu',selector:'li',types:['click'],handler:switchConn},
            {parent:'#groups>.list-group',selector:'a',types:['click'],handler:subscribeTopicsPage},
            {parent:'#groups>.list-group',selector:'b',types:['click'],handler:deleteGroup},
            {selector:'#admin',types:['click'],handler:adminPage},
            {selector:'#monitor',types:['click'],handler:brokerMonitor},
            {selector:'#brokerMonitor button[name=refresh]',types:['click'],handler:refreshBrokerMonitor}];
        util.regPageEvents(events);

        /**
         * 集群监控
         */
        function brokerMonitor() {
            dialog.create('集群监控')
                .setContent($('#brokerMonitor'))
                .setWidthHeight('70%', '50%')
                .build();
            refreshBrokerMonitor();
        }

        function refreshBrokerMonitor() {
            util.requestData(apis.monitor,{clusterName:groupsPage.conn},function (data) {
                let htmlCode = [];
                for (let i=0;i<data.length;i++){
                    let item = data[i];
                    item.meanRate = parseFloat(item.meanRate).toFixed(2);
                    item.oneMinute = parseFloat(item.oneMinute).toFixed(2);
                    item.fiveMinute = parseFloat(item.fiveMinute).toFixed(2);
                    item.fifteenMinute = parseFloat(item.fifteenMinute).toFixed(2);
                    htmlCode.push('<tr><td>'+item.mBean+'</td><td>'+item.meanRate+'</td><td>'+item.oneMinute+'</td><td>'+item.fiveMinute+'</td><td>'+item.fifteenMinute+'</td></tr>')
                }
                $('#brokerMonitor').find('tbody').html(htmlCode.join(''));
            });
        }

        /**
         * 删除消费组
         */
        function deleteGroup() {
            var group = $(this).closest('a').attr('group');
            layer.confirm('确定删除消费组:'+group,function (r) {
                if(r){
                    util.requestData(apis.delGroup,{clusterName:groupsPage.conn,group:group},function () {
                        layer.msg('删除成功');
                        $(this).closest('a.list-group-item').remove();
                    })
                }
            });

        }
        
        function subscribeTopicsPage() {
            var group = $(this).attr('group');
            util.tab('/app/kafka/subscribeTopics.html',{group:group,name:groupsPage.conn});
        }
        function adminPage() {
            util.tab('/app/kafka/admin.html',{conn:groupsPage.conn});
        }

        function switchConn() {
            var conn = $(this).data('value');
            groupsPage.conn = conn;

            $('#connect>button>span:eq(0)').text(conn);
            util.requestData(apis.detail,{clusterName:conn},function (connInfo) {
                $('#connect').next('input').val(JSON.stringify(connInfo));
                // 获取 brokers 信息
                util.requestData(apis.brokers,{clusterName:groupsPage.conn},function (brokers) {
                    $('#brokers').text(brokers.join(','));
                })
            });
            $('#connect>.dropdown-menu').dropdown('toggle');

            //加载当前连接分组信息
            loadGroups();
        }

        function newconn() {
            dialog.create('创建新连接')
                .setContent($('#newconn'))
                .setWidthHeight('600px','550px')
                .addBtn({type:'yes',text:'添加',handler:createConn})
                .build();
            //加载所有 zk 连接
            util.requestData(apis.zkConns,{modul:'zookeeper'},function (conns) {
                $('#conns').empty();
                for(var i=0;i<conns.length;i++){
                    $('#conns').append('<option value="'+conns[i]+'">'+conns[i]+'</option>');
                }
            });

            function createConn(index) {
                var params = util.serialize2Json($('#newconn>form').serialize());
                // 获取 ssl 属性
                params.ssl = {};
                for (var key in params){
                    if(key.startsWith('ssl')){
                        params.ssl[key.substring(3)] = params[key];
                        delete  params[key];
                    }
                }
                var sendData = {
                    zkConn:params.zkConn,
                    kafkaConnInfo:params
                }

                util.requestData(apis.createConn,sendData,function () {
                    layer.close(index);
                    loadConns(function () {
                        $('#connect>.dropdown-menu>li[name='+params.name+']').click();
                        $('#connect>.dropdown-menu').dropdown('toggle');
                    })
                });
            }

        }

        function thirdpart() {
            dialog.create('设置第三方监控')
                .setContent($('#setthirdpart'))
                .setWidthHeight('60%','25%')
                .addBtn({type:'yes',text:'设置',handler:setThirdpart})
                .build();

            //加载详情,如果有第三方监控设置,则加载
            util.requestData(apis.detail,{clusterName:groupsPage.conn},function (connInfo) {
                if(connInfo.thirdpartTool){
                    $('#setthirdpart').find('input').val(connInfo.thirdpartTool);
                }
            });

            function setThirdpart(index) {
                var thirdpart = $('#setthirdpart').find('input').val().trim();
                util.requestData(apis.setThirdpartTool,{clusterName:groupsPage.conn,address:thirdpart},function () {
                    layer.close(index);
                })
            }
        }
    }

    return groups.init();
});