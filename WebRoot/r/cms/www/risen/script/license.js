var licenseInfo = [{"SerialNumber":"@SerialNumber","LicenseInfo":"NJoJBy16JSNKGM2w1Hi7jAab3ngS0RS6QJS0iBV2t9sHKLYgg1eBeSj64LaL1nqnHiePALBPIwSjdlsAQBHGkJlQu3GZnEwTSukQajC4Pa6MF5RAIWdsAw8y9piTuvfG5G45v8zxJCyD1M1JP8VlEcCuFUpuyvEFnH8btBqfeLRfCHlcvPyl3m8BSBj2Fi/7D2jhaH8+jgONpUDZYgz4aj18/kx7odX58VUlCxI3pvG2umZQLCk+jvTyy7Ts1hM9KL4MsgCZLsBMqsKiWzjA15cmSFkZ4tomgc0cHs8wR3x0cBKjwoVhVkDD4UdVsqmCX5vIR3xhHqFnF9g4Ixd4PvMcocvSKKOzhbh51w/falx1tDMPpEVCy7VISKSWkDOl4BbuqATpMyoUn4FvRW1X3kihejlUV0gEUS5H3OpfDBY+fBaJSlqGoI8yGZcXVdXSjMEnjuiTTxNdjDMkv8yERd44DHiGYwR5jL6UeQn01KbCV9S6CtO2bVteleyNsMyS","CustomerName":"\u8d63\u5dde\u5e02\u5de5\u4e1a\u548c\u4fe1\u606f\u5316\u59d4\u5458\u4f1a","AuthVersion":"\u6807\u51c6\u7248","AuthSite":"\u5355\u7ad9\u70b9","SiteDomain":"","ExpiredTime":"@ExpiredTime","VersionNumber":"@VersionNumber"}];

function checkLicense(){var a=licenseInfo[0];if(!a){alert("\u8f6f\u4ef6\u6388\u6743\u8bb8\u53ef\u4fe1\u606f\u5f02\u5e38\uff0c\u8bf7\u68c0\u67e5\u8bb8\u53ef\u4fe1\u606f\u0021");return false}if(Date.parse(a.ExpiredTime.replace(/\-/g,"/"))<Date.parse((new Date()).format("yyyy-MM-dd hh:mm:ss").replace(/\-/g,"/"))){alert("\u8f6f\u4ef6\u6388\u6743\u8bb8\u53ef\u4fe1\u606f\u5f02\u5e38\u6216\u6ce8\u518c\u7801\u5df2\u8fc7\u671f\uff0c\u8bf7\u68c0\u67e5\u8bb8\u53ef\u4fe1\u606f\u0021");return false}return true};

$(function(){checkLicense();});

Date.prototype.format=function(a){var d={"M+":this.getMonth()+1,"d+":this.getDate(),"h+":this.getHours()%12==0?12:this.getHours()%12,"H+":this.getHours(),"m+":this.getMinutes(),"s+":this.getSeconds(),"q+":Math.floor((this.getMonth()+3)/3),S:this.getMilliseconds()};var c={"0":"\u65e5","1":"\u4e00","2":"\u4e8c","3":"\u4e09","4":"\u56db","5":"\u4e94","6":"\u516d"};if(/(y+)/.test(a)){a=a.replace(RegExp.$1,(this.getFullYear()+"").substr(4-RegExp.$1.length))}if(/(E+)/.test(a)){a=a.replace(RegExp.$1,((RegExp.$1.length>1)?(RegExp.$1.length>2?"\u661f\u671f":"\u5468"):"")+c[this.getDay()+""])}for(var b in d){if(new RegExp("("+b+")").test(a)){a=a.replace(RegExp.$1,(RegExp.$1.length==1)?(d[b]):(("00"+d[b]).substr((""+d[b]).length)))}}return a};