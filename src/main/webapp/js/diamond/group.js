define(['util','diamond/ConfigSeeDialog'],function (util,ConfigSeeDialog) {
    var group = {};
    var apis = {
        groups:'/data/db/groups',
        dataIds:'/data/db/dataIds',
        content:'/data/db/content',
        latestUse:'/data/db/latestUse',

        conns:'/sqlclient/connections',
        detail:'/sqlclient/connectionInfo'
    };

    group.init = function() {
        bindEvent();
        loadConns(function () {
            $('#connect>.dropdown-menu>li:first').click();
            $('#connect>.dropdown-menu').dropdown('toggle');
        });
        //加载最近使用
        util.requestData(apis.latestUse,function (latestUses) {
            var $latestUse = $('#latestUse>.list-group').empty();
            for(var i=0;i<latestUses.length;i++){
                $latestUse.append('<li class="list-group-item" groupDataId = "'+latestUses[i]+'">'+latestUses[i]+'</li>')
            }
        });
    }

    function bindEvent() {
        var events = [{parent:'#connect>.dropdown-menu',selector:'li',types:['click'],handler:switchConn},
            {parent:'#groups>.list-group',selector:'.list-group-item',types:['click'],handler:clickGroup},
            {parent:'#latestUse',selector:'.list-group-item',types:['click'],handler:clickLatestUse}];

        util.regPageEvents(events);

        /**
         * 切换连接
         */
        function switchConn() {
            var conn = $(this).data('value');
            group.conn = conn;

            $('#connect>button>span:eq(0)').text(conn);
            util.requestData(apis.detail,{name:conn},function (detail) {
                $('#connect').next('input').val('jdbc://'+detail.host+':'+detail.port+'/nacos');
            });
            $('#connect>.dropdown-menu').dropdown('toggle');

            //加载当前所有分组
            util.requestData(apis.groups,{connName:conn},function (groups) {
                var $groups = $('#groups').find('.list-group').empty();
                $('#groups>.panel-heading>span').text(groups.length);
                for (var i=0;i<groups.length;i++){
                    var group = groups[i];
                    $groups.append('<li group="'+group+'" class="list-group-item">'+group+'</li>')
                }
            });
        }

        /**
         * 点击某个组
         */
        function clickGroup() {
            var $item = $(this);
            var group = $item.attr('group');
            util.go('dataIds.html',{group:group,connName:group.conn}) ;
        }

        /**
         * 加载最近使用的配置
         */
        function clickLatestUse() {
            var groupDataId = $(this).attr('groupDataId');
            var connName = groupDataId.split('@')[0];
            var group = groupDataId.split('@')[1];
            var dataId = groupDataId.split('@')[2];
            util.requestData(apis.content, {connName:connName,group: group, dataId: dataId}, function (content) {
                $('#content>textarea').val(content);

                $('#content').data('group',group);
                $('#content').data('dataId',dataId);

                ConfigSeeDialog.buildDialog(group,dataId);

            });
        }
    }

    function loadConns(callback) {
        util.requestData(apis.conns,function (conns) {
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

    return group.init();
});