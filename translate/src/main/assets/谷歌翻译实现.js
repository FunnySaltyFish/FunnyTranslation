/*FunnyTranslation JS Engine Start*/
let FunnyJS = {
    // 插件基本配置
    // 插件作者
    "author":"官方",
    // 插件描述。如果该描述是Markdown类型，请在开头添加：[Markdown]
    "description":"在谷歌翻译暂时无法使用时提供谷歌翻译能力。",
    // 插件版本。插件的更新依赖于此
    "version":3,
    // 最低支持的插件引擎版本
    "minSupportVersion":4,
    // 适合的插件引擎版本
    "targetVersion":4,
    // 最高支持的插件引擎版本(已弃用)
    "maxSupportVersion":999,
    // 插件名称
    "name":"谷歌翻译[补]",
    // 是否开启Debug模式
    "debugMode":true,
    // 插件是否为离线插件
    "isOffline":false,

    // 下面是插件需要回调的方法
    // 详见 https://www.yuque.com/funnysaltyfish/vzmuud
    "madeURL":function(){return "https://translate.google.cn/_/TranslateWebserverUi/data/batchexecute"},
	"getBasicText":function(url){
    	let headers=objToHeaders(apiHeaders);
        let from=languages[sourceLanguage.id];
        let to=languages[targetLanguage.id];
        let rpc=get_rpc(sourceString,from,to);
        //log(rpc);
        let data=new java.util.HashMap();
        data["f.req"]=rpc;
        
        let text=funny.post(url+"?f.req="+data["f.req"],data,headers);
        //log(text);
    	return text;
    },
	"getFormattedResult":function(text){
    	let obj=eval(text.substr(6));
        
        let trans_data = eval(obj[0][2]);
        //log(trans_data);
        let trans="";
        let details=trans_data[1][0][0][5];
        for(let i in details){
        	//log(each);
        	trans+=details[i][0]+"\n";
        }
        trans=trans.substr(0,trans.length-1);
        result.setBasicResult(trans);
     }
}
/*FunnyTranslation JS Engine End*/

//语言常量列表
let languages = [
	"auto","zh-CN","en","ja","ko","fr","ru","de","zh-CN","th","pt"
]


let apiHeaders = {
	'Origin': 'https://translate.google.cn', 
    'Referer': 'https://translate.google.cn', 
    'X-Requested-With': 'XMLHttpRequest', 
    'Content-Type': 'application/x-www-form-urlencoded; charset=UTF-8', 
    'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/55.0.2883.87 Safari/537.36',
    "Cookie": "NID=511=H0P6mXmLFb1J05Xi9i4dDvC5YsFQ6zQnVof4iO8FTafTDYJjC4urjWggpPIiwoDnYcnZCAQa4NPPti52tGtQVe5DQGfjBtg-Ip_IYaNqZZf4IH4nQmxqXlKSDdPhCeFSlSzXwrt-X0vHnxxSPBsKCHL6v17z_8TWCv3aHq6nNdA"
}  

function get_rpc(query_text,from,to){
	let param = JSON.stringify([[query_text, from, to, true], [1]]);
    let rpc=JSON.stringify([[["MkEWBc", param, null, "generic"]]]);
    return rpc;//{"f.req":rpc};
}

function objToHeaders(obj){
	let map=new java.util.HashMap();
	for(key in obj){
    	//funny.log(key);
        map[key]=obj[key];
    }
    return map;
}

function log(obj){funny.log(obj);}
