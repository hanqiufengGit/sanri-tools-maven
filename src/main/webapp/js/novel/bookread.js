define(['util'],function (util) {
    var bookcontent = {};
    bookcontent.init = function () {
        var parseUrl = util.parseUrl();
        bookcontent.params = parseUrl.params;

        $('#chaptername').text(parseUrl.params.title);

        //请求参数
        var index = layer.load(1, {
            shade: [0.1,'#fff']
        });
        try{
            util.requestData('/novel/content',{link:parseUrl.params.link},function (data) {
                $('#contentHtml').html(data.content);
            });
            layer.close(index);
        }catch (e){
            layer.close(index);
        }

        return this;
    };

    return bookcontent.init();
});