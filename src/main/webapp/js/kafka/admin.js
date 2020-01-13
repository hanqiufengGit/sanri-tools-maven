define(['util','dialog','jsoneditor','icheck','jsonview'],function (util,dialog,JSONEditor) {
    var kafkaAdmin = {};
    var apis = {
        topics:'/kafka/topics',
        logSizes:'/kafka/logSizes',
        create:'/kafka/createTopic',
        drop:'/kafka/deleteTopic',
        lastDatas: '/kafka/lastDatas',
        nearbyDatas:'/kafka/nearbyDatas',
        serializes:'/zk/serializes',
        sendKafkaData:'/kafka/sendJsonData'
    }
    kafkaAdmin.init = function () {
        bindEvents();
        $('#data input[type=checkbox]').iCheck({
            checkboxClass: 'icheckbox_square-green'
        });
        kafkaAdmin.conn = util.parseUrl().params.conn;
        $('#adminconn>a>span').text(kafkaAdmin.conn).attr('conn',kafkaAdmin.conn);

        loadTopics();

        //加载序列化工具列表
        util.requestData(apis.serializes,function (serializes) {
            $('#serializeTools').empty();
            for (var i = 0; i < serializes.length; i++) {
                $('#serializeTools').append('<option value="' + serializes[i] + '">' + serializes[i] + '</option>');
            }
        });

        // 初始化 json 编辑器
        var container = document.getElementById('jsondataformat');
        var options = {
            mode: 'tree',
            onError: function (err) {
                alert(err.toString());
            }
        };
        kafkaAdmin.jsonEditor = new JSONEditor(container, options, null);
        //全局文本域自动高度
        $('textarea[autoHeight]').autoHeight();

        return this;
    }

    function loadTopics() {
        var index = layer.load(1, {
          shade: [0.1,'#fff']
        });
        util.requestData(apis.topics,{clusterName:kafkaAdmin.conn},function (topics) {
            layer.close(index);

            var $topics = $('#topics').empty();

            for(var topic in topics){
                var $topic = $('<div class="list-group-item" topic="'+topic+'">'+topic+' <a href="javascript:void(0);" class=" pull-right">删除</a></div>').appendTo($topics);
                $topic.data('partitions',topics[topic]);
            }
        });
    }

    function openDataDialog(partition,offset,btnName) {
        //调用接口选择
        var switchApi = apis.nearbyDatas;
        if(btnName == 'lastdata'){
            switchApi = apis.lastDatas;
        }
        var topic = $('#topicname').data('topic');
        var serialize = $('#serializeTools').val();
        util.requestData(switchApi,{clusterName:kafkaAdmin.conn,topic:topic,partition:partition,offset:offset,serialize:serialize},function (datas) {
            var $tbody = $('#datadetail').find('tbody').empty();
            // for(var offset in datas){
            for(var i=0;i<datas.length;i++){
                var offset = datas[i].offset;
                var data = datas[i].data;
                var btn = '<button type="button" class="btn btn-sm btn-primary"><i class="fa fa-book"></i> JSON </button>';
                $tbody.append('<tr offset="'+offset+'"><td>'+btn+'</td><td>'+offset+'</td><td>'+data+'</td></tr>');
            }

            var buildDialog = dialog.create('显示topic['+topic+']partition['+partition+']offset['+offset+']附近['+btnName+']的数据')
                .setWidthHeight('90%','90%')
                .setContent($('#showdataDialog'));
            buildDialog.build();
        });
    }

    function bindEvents() {
        var events = [{selector:'#createTopic',types:['click'],handler:createTopic},
            {parent:'#topics',selector:'.list-group-item',types:['click'],handler:showTopicDetail},
            {parent:'#topics',selector:'.list-group-item>a',types:['click'],handler:deleteTopic},
            {selector:'#refreshlogsize',types:['click'],handler:refreshLogSize},
            {selector:'#createdata',types:['click'],handler:createData},
            {parent:'#topicdetail',selector:'button[name=lastdata],button[name=nearbyData]',types:['click'],handler:showdata},
            {selector:'#serializeTools',types:['change'],handler:changeSerialize},
            {parent:'#datadetail',selector:'button',types:['click'],handler:jsonView},
            {selector:'#validJson',types:['click'],handler:validJson},
            {selector:'#compactJson',types:['click'],handler:compactJson},
            {selector:'#sendData',types:['click'],handler:sendKafkaData}];

        util.regPageEvents(events);

        // 发送数据时验证 json 是否正确
        function validJson() {
            var json = $('#jsondata').val().trim();
            kafkaAdmin.jsonEditor.setText(json);
        }
        function compactJson() {
            var text = kafkaAdmin.jsonEditor.getText();
            $('#jsondata').val(text);
        }
        // 发送数据到 kafka
        function sendKafkaData() {
            var partition = $('#datajson').find('input[name=partition]').val().trim();
            partition = partition ? partition:undefined;
            var json = $('#jsondata').val().trim();
            var topic = $('#topicname').data('topic');
            util.requestData(apis.sendKafkaData,{topic:topic,clusterName:kafkaAdmin.conn,key:partition,data:json},function () {
               layer.msg('发送成功');
            });
        }

        // 查看 json 视图数据
        function jsonView() {
            var offset = $(this).closest('tr').attr('offset');
            var json = $(this).parent().siblings('td:last').text();
            // $('#jsonViewLoad').text(json);

            dialog.create('offset:'+offset +' 的数据')
                .setWidthHeight('500px','500px')
                .setContent($('#jsonView'))
                .onOpen(loadJsonData)
                .build();

            function loadJsonData() {
                require(['jsonview'],function () {
                    $('#jsonViewLoad').JSONView(json);
                });

            }
        }

        function changeSerialize() {
            openDataDialog(openDataDialog.partition,0,openDataDialog.btnName);
        }
        function showdata() {
            var partition = $(this).closest('tr').attr('partition');
            var offset = 0;
            var btnName = $(this).attr('name');
            openDataDialog.partition = partition;
            openDataDialog.btnName = btnName;
            openDataDialog(partition,offset,btnName);
        }
        
        function refreshLogSize() {
            var topic = $('#topics').find('div.list-group-item.active').attr('topic');
            renderTopicPartitions(topic);
        }

        function createTopic() {
            dialog.create('创建主题')
                .setContent($('#createTopicDialog'))
                .setWidthHeight('40%','45%')
                .addBtn({type:'yes',text:'确定',handler:function(index, layero){
                        var params = util.serialize2Json($('#createTopicDialog').find('form').serialize());
                        if(!params.topic || !params.partitions || !params.replication){
                            layer.msg('请将信息填写完整');
                            return ;
                        }
                        params.name = kafkaAdmin.conn;
                        util.requestData(apis.create,params,function () {
                            loadTopics(params.topic);
                            layer.close(index);
                        })
                    }}).build();
        }
        
        function showTopicDetail() {
            $(this).addClass('active').find('a').addClass('text-whitesmoke');
            $(this).siblings().removeClass('active').find('a').removeClass('text-whitesmoke');

            var topicName = $(this).attr('topic');
            $('#topicname').text(topicName);
            $('#topicname').data('topic',topicName);

            $('#topicdetail>tbody').empty();    // 点击新主题时,清空 logSize 列表,避免计算出错
            renderTopicPartitions(topicName);
        }
        function deleteTopic() {
            var topicName = $(this).parent().attr('topic');
            layer.confirm('确定删除主题:'+topicName,function (r) {
                if(r){
                    util.requestData(apis.drop,{topic:topicName,name:kafkaAdmin.conn},function () {
                        layer.msg('删除成功');
                        loadTopics(); //不管用,因为删除的也加载了
                    });
                }
            });
        }
        
        function createData() {
            
        }
        
        function renderTopicPartitions(topicName) {
            var index = layer.load(1, {
                shade: [0.1,'#fff']
            });
            // 记住之前的数据,与之后变化的数据进行对比,可以比较出哪一个是有新加消息的并且加了多少
            var $tbody = $('#topicdetail>tbody');
            var lastLogSize = undefined;
            // 不知道怎么判断是否还有节点，暂时用获取 td 的个数来判断
            if($tbody.find('td').size() != 0){
                lastLogSize = [];
                $tbody.find('tr>td:nth-child(2)').each(function (i) {
                    lastLogSize[i+''] = parseInt($(this).text());
                });
            }
            util.requestData(apis.logSizes, {clusterName:kafkaAdmin.conn,topic: topicName}, function (logSizes) {
                // 写入最近一次刷新时间
                var dateFormat = util.FormatUtil.dateFormat(new Date().getTime(), 'yyyy-MM-dd HH:mm:ss');
                $('#refreshlogsize').next('time').text(dateFormat);

                $tbody.empty();
                var htmlCode = [];
                var partitions = Object.keys(logSizes);
                $('#topicname').data('partitions', partitions);

                var $btnGroup = '<div class="btn-group btn-group-sm"><button class="btn btn-sm btn-success" name="nearbyData">附近数据</button><button class="btn btn-sm btn-warning" name="lastdata">尾部数据</button></div>';
                for (var key in logSizes) {
                    var change = lastLogSize ? '+'+(logSizes[key] - lastLogSize[key]):logSizes[key];
                    htmlCode.push('<tr partition="'+key+'"><td>' + key + '</td><td>' + logSizes[key] + '</td><td>'+change+'</td><td>'+$btnGroup+'</td></tr>')
                }
                $tbody.append(htmlCode.join(''));

                layer.close(index);
            });
        }
    }
    return kafkaAdmin.init();
});