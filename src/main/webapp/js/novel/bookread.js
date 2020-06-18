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
                $('#fun>a:first').attr('link',data.prev);
                $('#fun>a:last').attr('link',data.next);

                $('#fun>a').bind('click',function () {
                    let url = new URL(window.location)
                    url.search = 'link='+$(this).attr('link')+'&title='+bookcontent.params.title
                    window.location.href = url.toString();
                });
            });
            layer.close(index);
        }catch (e){
            layer.close(index);
        }

        return this;
    };

    return bookcontent.init();
});