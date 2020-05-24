define(['util'],function (util) {
   let apidoc = {};
   let apis = {};
   let module = 'swaggerui';

   apidoc.init = function () {
       bindEvents();

       // 加载 api 文档
      util.requestData('/swagger/html',{url:'http://localhost:8080/v2/api-docs'},function (html) {
         $('#doc').html(html);
      });
   }

   function bindEvents() {
       // var events = [{parent:'#examples>.dropdown-menu',selector:'li',types:['click'],handler:switchRegex}];
       // util.regPageEvents(events);
   }

   return apidoc.init();
});