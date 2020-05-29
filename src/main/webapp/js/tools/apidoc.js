define(['util'],function (util) {
   let apidoc = {};
   let apis = {
      doc:'/swagger/html',
      word:'/swagger/word',
      markdown:'/swagger/markdown',

      connNames:'/file/manager/simpleConfigNames',
      createConn:'/file/manager/writeConfig',
      detail:'/file/manager/readConfig'
   };
   let module = 'swaggerui';

   apidoc.init = function () {
       bindEvents();

       // 加载所有的链接
      let $history = $('#navbar').find('ul[name=history]').empty();
      util.requestData(apis.connNames,{modul:module},function (links) {
         for (var i = 0; i < links.length; i++) {
            $history.append('<li link = "'+links[i]+'"><a href="javascript:void(0);">'+links[i]+'</a></li>');
         }
         $history.find('li:first').click();
         $history.dropdown('toggle');
      });
   }

   function bindEvents() {
       var events = [{parent:'#navbar',selector:'.navbar-form button[name=search]',types:['click'],handler:clickSearch},
          {parent:'#navbar',selector:'.navbar-form input[name=search]',types:['keydown'],handler:keydownSearch},
          {parent:'#navbar ul[name=history]',selector:'li',types:['click'],handler:dropdownSearch},
          {parent:'#navbar',selector:'button',types:['click'],handler:download}];

       util.regPageEvents(events);

      /**
       * 点击搜索
       */
      function clickSearch() {
         let url = $(this).closest('.input-group').find('input[name=search]').val().trim();
         search(url);

         newConn(url);
      }

      /**
       * 回车搜索
       */
      function keydownSearch(event) {
         var event = event || window.event;
         if(event.keyCode == 13){
            let url = $(this).val().trim();
            search(url);

            newConn(url);
         }
      }

      /**
       * 下载选中时搜索
       */
      function dropdownSearch() {
         let link = $(this).attr('link');
         let $history = $(this).parent().siblings('button');
         $history.find('span:first').text(link);
         $history.dropdown('toggle');

         util.requestData(apis.detail,{modul:module,baseName:link},function (url) {
            // 填入信息
            $('#navbar').find('input[name=search]').val(url);
            search(url);
         });
      }

      /**
       * 下载接口文档
       */
      function download() {
         let btnName = $(this).attr('name');
         let url = $('#navbar').find('input[name=search]').val().trim();
         let urlObj = new URL(url);
         let port = urlObj.port ? urlObj.port : (urlObj.protocol === 'http'? 80:443);
         let finalSwaggerV2URL = urlObj.protocol+'//'+urlObj.hostname+':'+port+'/v2/api-docs';

         if(urlObj.pathname.endsWith('api-docs')){
            finalSwaggerV2URL = url;
         }
         util.downFile(apis[btnName],{url:finalSwaggerV2URL},10000);
      }
   }

   /**
    * 建立新连接
    */
   function newConn(url) {
      let urlObj = new URL(url);
      let port = urlObj.port ? urlObj.port : (urlObj.protocol === 'http'? 80:443);
      let link = urlObj.hostname+'_'+port;

      util.requestData(apis.createConn,{modul:module,baseName:link,content:url},function () {});
   }

   /**
    * 发起搜索,查询当前 swagger 文档
    */
   function search(url) {
      if(!url){
         layer.msg('需要填入 swagger 地址');
         return ;
      }

      // 在当前输入框写入文档地址
      $('#navbar').find('input[name=search]').val(url);

      let urlObj = new URL(url);
      let port = urlObj.port ? urlObj.port : (urlObj.protocol === 'http'? 80:443);
      let finalSwaggerV2URL = urlObj.protocol+'//'+urlObj.hostname+':'+port+'/v2/api-docs';
      if(urlObj.pathname.endsWith('api-docs')){
         finalSwaggerV2URL = url;
      }

      // 加载 api 文档
      var index = layer.load(1, {
         shade: [0.1,'#fff']
      });
      util.requestData(apis.doc,{url:finalSwaggerV2URL},function (html) {
         $('#doc').html(html);
         layer.close(index);
      },function () {
         layer.close(index);
      });
   }

   return apidoc.init();
});