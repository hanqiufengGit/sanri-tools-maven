define(['util'],function (util) {
   // 用于导入所有 html 片段，扫描所有 include 的类，加载进来
    $('.include').each(function () {
        var src = $(this).attr('src');
        $(this).load(src);
    });
});