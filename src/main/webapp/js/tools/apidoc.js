define(['util'],function (util) {
   let apidoc = {};
   let apis = {};
   let module = 'swaggerui';

   apidoc.init = function () {
       bindEvents();
   }

   function bindEvents() {
       // var events = [{parent:'#examples>.dropdown-menu',selector:'li',types:['click'],handler:switchRegex}];
       // util.regPageEvents(events);
   }

   return apidoc.init();
});