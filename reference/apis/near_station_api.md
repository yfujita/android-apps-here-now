最寄駅情報取得 API
ご指定の場所 （緯度、経度） の最寄駅の情報の一覧を取得する API です。

リクエスト URL
フォーマット	URL
XML 形式	https://express.heartrails.com/api/xml?method=getStations
JSON(P) 形式	https://express.heartrails.com/api/json?method=getStations
リクエストパラメータ
パラメータ	値	説明
method	getStations （固定）	メソッド名
x	double （必須）	最寄駅の情報を取得したい場所の経度 （世界測地系）
y	double （必須）	最寄駅の情報を取得したい場所の緯度 （世界測地系）
jsonp	string （オプション）	
JSON 形式のデータを受け取るためのコールバック関数名
JSON 形式のリクエスト URL にのみ対応
レスポンスフィールド
フィールド	説明
response	最寄駅の情報の一覧
station	最寄駅の情報
name	最寄駅名
prev	前の駅名 （始発駅の場合は null）
next	次の駅名 （終着駅の場合は null）
x	最寄駅の経度 （世界測地系）
y	最寄駅の緯度 （世界測地系）
distance	指定の場所から最寄駅までの距離 （精度は 10 ｍ）
postal	最寄駅の郵便番号
prefecture	最寄駅の存在する都道府県名
line	最寄駅の存在する路線名
サンプルレスポンス
https://express.heartrails.com/api/json?method=getStations&x=135.0&y=35.0