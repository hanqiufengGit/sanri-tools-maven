${h2} 文档基本信息
本文档来自 ${swaggerURL}

名称: ${doc.info.title}

版本: ${doc.info.version}

联系-邮箱: $!{doc.info.contact.email}

联系-姓名: $!{doc.info.contact.name}

联系-URL: $!{doc.info.contact.url}


-------

${h2} 文档接口信息(当前接口数量: ${doc.tables.size()})

#foreach($table in $doc.tables)
${h3} $velocityCount. ${table.tag}
${table.description}

${table.requestType} ${table.url}

Content-Type:${table.requestForm}

**请求数据**

|参数名|数据类型|参数类型|是否必填|说明|
|----|-------|-------|-------|----|
#foreach($request in $table.requestList)
|$request.name|$request.type|$request.paramType|$request.require|$request.remark|
#end

**响应数据**

#end