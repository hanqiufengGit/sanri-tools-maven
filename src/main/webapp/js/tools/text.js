define(["util",'autoheight'], function (util) {
    var textTool = {};

    // 常用的正则数据提取
    var regexs = {
        'java属性':{pattern:/(private|protected)\s+\w+\s+(\w+);/,index:2},
        'java 属性值':{pattern:/(private|protected|public).*?=\s*(.+?);/,index:2},
        'java 公共属性':{pattern:/public.*?\s(\w+)\s=/,index:1},
        '文档注释':{pattern:/\* (.*?)\n/,index:1},
        '末尾注释':{pattern:/\/\/(.*)/,index:1},
        '末尾注释前面的':{pattern:/(.*)\/\//,index:1},
        '属性列':{pattern: /property="(\w+)"/,index:1},
        '数据库列':{pattern:/column="(\w+)"/,index:1},
        '表列':{pattern:/"(\w+)"\s.*/,index:1},
        '表列类型':{pattern:/"(\w+)"\s(\w+).*/,index:2},
        '表列注释':{pattern:/\'(.+)\';/,index:1},
        'hibernatesql':{pattern:/"(.+)"/,index:1},
        'aliassql':{pattern:/(\w+),/,index:1},
        '注释提取':{pattern:/value\s+=\s+"(.+)"/,index:1},
        '类型提取':{pattern:/(private|protected)\s+(\w+)/,index:2}
    };

    textTool.init = function () {
        loadDefaultRegexDistill();

        bindEvents();

        //全局文本域自动高度
        $('textarea[autoHeight]').autoHeight();
    }

    /**
     * 加载默认的正则数据提取
     * 渲染到界面
     */
    function loadDefaultRegexDistill() {

    }

    function bindEvents() {
        var charsetEvents = {};             // 字符集事件，用于处理字符集编码 unicode utf8 gbk gb2312
        var encodeEvents = {};              // 字符编码(公开的可解码) escape,base64,hex,html编码
        var digestEvents = {};              // 消息签名事件 md5,sha1
        var textEvents = {};                // 文本处理，使用正则提取文本数据，下划线/驼峰 相互转化

        // unicode 转中文，用于属性文件中 unicode 问题
        charsetEvents.unicodeToChinese = function () {
            let val = getVal(getSource());
            getTarget().val(eval('\''+val+'\''));
        };
        charsetEvents.chineseToUnicode = function () {
            let str = getVal(getSource());
            var resultStr = "";
            for (var i = 0; i < str.length; i++) {
                var char = str.charAt(i);
                // 只有是中文才转换，字母和数字不转换
                if (/^[\u4e00-\u9fa5]$/.test(char)) {
                    var code = str.charCodeAt(i);
                    var code16 = code.toString(16);
                    var ustr = "\\u" + code16;
                    resultStr += ustr;
                } else {
                    resultStr += char;
                }
            }
            getTarget().val(resultStr);
        }
        // base64 图片和文本处理
        encodeEvents.imageBase64 = function () {

        }
        encodeEvents.textBase64 = function(){

        }
        encodeEvents.base64ToImage = function () {

        }
        encodeEvents.base64ToText = function(){

        }

        // hex 转换工具类
        encodeEvents.hexToText = function () {

        }
        encodeEvents.textToHex = function () {

        }

        // URL 转码工具 ; 需要弄清楚 encodeURL,encodeURLComponent,escape 的区别
        encodeEvents.encodeURL = function(){

        }
        encodeEvents.encodeURLComponent = function(){

        }
        encodeEvents.decodeURL = function(){

        }
        encodeEvents.decodeURLComponent = function(){

        }


        // md5 工具类，包含处理文件和文本的 md5 值
        digestEvents.fileMd5 = function () {

        }
        digestEvents.textMd5 = function () {

        }

        // 驼峰转下划线，下划线转驼峰
        textEvents.camelToUnderscore = function () {

        }
        textEvents.underscoreTocamel = function () {

        }

        var events = [{parent:'#Unicode',selector:'button[name=unicodeToChinese]',types:['click'],handler:charsetEvents.unicodeToChinese},
            {parent:'#Unicode',selector:'button[name=chineseToUnicode]',types:['click'],handler:charsetEvents.chineseToUnicode}];

        util.regPageEvents(events);
        
        function getSource() {
            return $('.tab-pane.active').find('textarea.source');
        }
        function getVal($el) {
            if($el != null)return $el.val().trim();
        }
        
        function getTarget() {
            return $('.tab-pane.active').find('textarea.target');
        }
    }

    return textTool.init();
});