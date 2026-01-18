ご指定の場所 （経度、緯度） 付近の町域の情報の一覧を取得する API です。

リクエスト URL
フォーマット	URL
XML 形式	https://geoapi.heartrails.com/api/xml?method=searchByGeoLocation
JSON(P) 形式	https://geoapi.heartrails.com/api/json?method=searchByGeoLocation
リクエストパラメータ
パラメータ	値	説明
method	searchByGeolocation （固定）	メソッド名
x	double （必須）	付近の町域の情報を取得したい場所の経度 （世界測地系）
y	double （必須）	付近の町域の情報を取得したい場所の緯度 （世界測地系）
jsonp	string （オプション）	形式のデータを受け取るためのコールバック関数名
形式のリクエスト URL にのみ対応
レスポンスフィールド
フィールド	説明
response	町域の情報の一覧
prefecture	町域の存在する都道府県名
city	町域の存在する市区町村名
city-kana	町域の存在する市区町村名の読み仮名 （平仮名）
town	町域名
town-kana	町域名の読み仮名 （平仮名）
x	町域の経度 （世界測地系）
y	町域の緯度 （世界測地系）
postal	町域の郵便番号
サンプルレスポンス
経度 「135.0」、緯度 「35.0」 の場所付近の町域の情報の一覧
https://geoapi.heartrails.com/api/xml?method=searchByGeoLocation&x=135.0&y=35.0