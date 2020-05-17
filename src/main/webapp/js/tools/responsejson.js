define(['util','jsoneditor'],function (util,JSONEditor) {
    let responsejson = {};

    var apis = {
        classloaders:'/classloader/classloaders',
        loadedClasses:'/classloader/loadedClasses',
        uploadClasses:'/classloader/uploadClasses',

        randomList:'/random/randomListData',
        randomObject:'/random/randomData'
    };

    responsejson.init = function () {
        bindEvent();
        loadClassloaders();

        // 初始化 json 编辑器
        var container = document.getElementById('jsonview');
        var options = {
            mode: 'tree',
            onError: function (err) {
                alert(err.toString());
            }
        };
        responsejson.jsonEditor = new JSONEditor(container, options, null);
        //全局文本域自动高度
        $('textarea[autoHeight]').autoHeight();
    }

    function bindEvent() {
        var events = [
            {parent:'#config',selector:'input[type=file]',types:['change'],handler:uploadClasses},
            {parent:'#config',selector:'select[name=classloaders]',types:['change'],handler:switchClassloader},
            {parent:'#loadclasses',selector:'li',types:['click'],handler:randomData}];

        util.regPageEvents(events);

        /**
         * 随机数据生成
         */
        function randomData() {
            // 获取配置项
            let isList = true;
            // let api =
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

    return responsejson.init();
});