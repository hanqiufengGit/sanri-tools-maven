/**
 * Created by sanri on 2017/4/15.
 */
require.config({
   baseUrl:'/sanritools/js',
   paths:{
       'layer':'../plugins/layer/layer',      /*相对于 baseUrl 路径*/
       'jquery':'jquery.1.9.1.min',
       'chosen':'../plugins/chosen/chosen.jquery.min',
       'icheck':'../plugins/icheck/icheck.min',
       'datetimepicker':'../plugins/datetimepicker/bootstrap-datetimepicker.min',
       'bootstrap':'../js/bootstrap.min',
       'scrolltabs':'../plugins/scrolltabs/scrolltabs',
       'codemirror/lib':'../plugins/codemirror',
       'contextMenu':'../plugins/contextMenu/jquery.contextMenu.min',
       'position':'../plugins/contextMenu/jquery.ui.position.min',
       'ztree':'../plugins/ztree/jquery.ztree.all',
       'zclip':'../js/jquery.zclip',
       'datatable':'../plugins/datatable/jquery.dataTables.min',
       'jsonview':'../plugins/jsonview/jquery.jsonview.min',
       'jsoneditor':'../plugins/jsoneditor/jsoneditor',
       'echarts':'../plugins/echarts/echarts.min',
       'steps':'../plugins/steps/jquery.steps',
       'template':'../plugins/template',
       'autocomplete':'../plugins/autocomplete/jquery.autocomplete.min',
       'storage':'jquery.storageapi.min',
       'migrate':'jquery.migrate',					//修复 jquery 升级后废弃的 api
       'formvalidate':'../plugins/validate/messages_zh',
       'textdiff':'../plugins/textdiff/jquery.pretty-text-diff.min',
       'diffmatch':'../plugins/textdiff/diff_match_patch',
       'fancybox':'../plugins/fancybox/jquery.fancybox',
       'nouislider':'../plugins/nouislider/jquery.nouislider.min',
       'ionRangeSlider':'../plugins/ionRangeSlider/ion.rangeSlider.min',
   },
   shim:{
       'icheck':{
           deps:['jquery'],
           exports:'icheck'
       },
       'layer':{
           deps:['jquery'],
           exports:'layer'
       },
       'bootstrap':{
           deps:['jquery']
       },
//       'scrolltabs':{deps:['util']},
       'ztree':{deps:['jquery']},
       'zclip':{deps:['jquery']},
       'chosen':{deps:['jquery']},
       'jsonview':{deps:['jquery']},
       // 'jsoneditor':{deps:['jquery']},
       'steps':{deps:['jquery']},
       'contextMenu':{deps:['jquery','position']},
       'autocomplete':{deps:['migrate']},
       'migrate':{deps:['jquery']},
       'textdiff':{deps:['jquery','diffmatch']},
       'fancybox':{deps:['jquery']},
       'nouislider':{deps:['jquery']},
       'ionRangeSlider':{deps:['jquery']},
   },
    packages:[{
        name: 'cryptojs',
        location: '../plugins/crypto-js',
        main: '../plugins/crypto-js/core'
    },{
       name:'hl',
       location: '../plugins/syntaxhighlighter/brush',
       main:'../shCore'
    }]

});

///**
// * 扩展 jquery 的 $.browser 以免引起插件引用低版本的 jquery 去掉的方法而出错
//*/
//require(['jquery'],function($){
//	$.fn.browser = {
//			opera:false
//	};
//})
